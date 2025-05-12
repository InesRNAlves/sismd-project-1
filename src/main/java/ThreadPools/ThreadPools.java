package ThreadPools;

import utils.Page;
import utils.Pages;
import utils.Words;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static utils.Utils.getListOfPages;
import static utils.Utils.isValidWord;
import static utils.Utils.partitionList;
import static utils.Utils.printElapseTime;
import static utils.Utils.printMemoryUsage;
import static utils.Utils.printNrOfEntriesInConcurrentHashMap;
import static utils.Utils.printTopNwordsInConcurrentHashMap;

/**
 * This class uses a thread pools to process pages concurrently.
 * It counts the occurrences of words in the text of each page and stores the results in a ConcurrentHashMap.
 * The main method initializes the processing and prints the results.
 */
public class ThreadPools {
    static final int maxPages = 100000;
    //static final String fileName = "src/main/resources/enwiki.xml";
    //static final String fileName = "enwiki3.xml"; // For running the benchmark script
    static final String fileName = "src/main/resources/enwiki3.xml";
    static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    static final int TOP_N = 3;

    private static final ConcurrentHashMap<String, Integer> counts = new ConcurrentHashMap<>();
    static final AtomicInteger processedPages = new AtomicInteger();

    public static void main(String[] args) throws Exception {

        long start = System.currentTimeMillis();
        long before = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        Iterable<Page> pagesIterable = new Pages(maxPages, fileName);
        List<Page> pages = getListOfPages(pagesIterable);

        int batchSize = 300;
        List<List<Page>> batches = partitionList(pages, batchSize);

        CountDownLatch latch = new CountDownLatch(batches.size());
        for (List<Page> batch : batches) {
            executor.submit(() -> {
                Map<String, Integer> localCounts = new HashMap<>();
                for (Page page : batch) {
                    for (String word : new Words(page.getText())) {
                        word = word.toLowerCase();
                        if (isValidWord(word)) {
                            localCounts.merge(word, 1, Integer::sum);
                        }
                    }
                    processedPages.incrementAndGet();
                }

                // Merge local counts into global counts
                localCounts.forEach((word, count) ->
                        counts.merge(word, count, Integer::sum)
                );

                latch.countDown();
            });
        }

        latch.await(); // wait for all threads to finish
        executor.shutdown();

        // Print metrics
        System.out.println("Processed pages: " + processedPages);
        printElapseTime(start);
        printTopNwordsInConcurrentHashMap(TOP_N, counts);
        printMemoryUsage(before);
        printNrOfEntriesInConcurrentHashMap(counts);

        long elapsed = System.currentTimeMillis() - start;
        long memoryUsedMB = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);

        // CSV output
        System.out.println("BATCH_SIZE,TIME_MS,MEMORY_MB,PAGES");
        System.out.printf("%d,%d\n", elapsed, memoryUsedMB);

    }

}
