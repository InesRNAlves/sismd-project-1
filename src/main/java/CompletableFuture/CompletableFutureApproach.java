package CompletableFuture;

import utils.Page;
import utils.Pages;
import utils.Words;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static utils.Utils.isValidWord;
import static utils.Utils.partitionList;
import static utils.Utils.printElapseTime;
import static utils.Utils.printMemoryUsage;
import static utils.Utils.printNrOfEntriesInConcurrentHashMap;
import static utils.Utils.printTopNwordsInConcurrentHashMap;
import static utils.Utils.getListOfPages;


/**
 * This class uses CompletableFuture to process batches of pages concurrently.
 * It counts the occurrences of words in the text of each page and stores the results in a ConcurrentHashMap.
 * The main method initializes the processing and prints the results.
 */
public class CompletableFutureApproach {

    static final int maxPages = 100000;
    //static final String fileName = "src/main/resources/enwiki.xml";
    static final String fileName = "src/main/resources/enwiki3.xml";
    //static final String fileName = "enwiki3.xml"; // For running the benchmark script
    static final int TOP_N = 3;

    private static final ConcurrentHashMap<String, Integer> counts =
            new ConcurrentHashMap<String, Integer>();
    static final AtomicInteger processedPages = new AtomicInteger();

    public static void main(String[] args) throws Exception {

        long start = System.currentTimeMillis();
        long memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        Iterable<Page> pagesIterable = new Pages(maxPages, fileName);
        List<Page> pages = getListOfPages(pagesIterable);

        // Divide into batches
        int batchSize = (int) Math.ceil((double) pages.size() / (300));
        List<List<Page>> batches = partitionList(pages, batchSize);

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (List<Page> batch : batches) {
            // Run each batch in a separate CompletableFuture
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> processBatch(batch))
                    .exceptionally(
                            ex -> {
                                System.err.println("Error processing batch: " + ex.getMessage());
                                return null;
                            }
                            );
            futures.add(future);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Print Metrics
        System.out.println("Processed pages: " + processedPages);
        printElapseTime(start);
        printTopNwordsInConcurrentHashMap(TOP_N, counts);
        printMemoryUsage(memoryBefore);
        printNrOfEntriesInConcurrentHashMap(counts);

        long elapsed = System.currentTimeMillis() - start;
        long memoryUsedMB = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() - memoryBefore) / (1024 * 1024);
        System.out.printf("%d,%d,\n", elapsed, memoryUsedMB);

    }

    /**
     * This method processes a batch of pages, counting the occurrences of words in the text of each page.
     * It updates the counts in a thread-safe manner using a ConcurrentHashMap.
     * Also increments the processedPages counter.
     */
    public static void processBatch(List<Page> batch) {
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

    }

}
