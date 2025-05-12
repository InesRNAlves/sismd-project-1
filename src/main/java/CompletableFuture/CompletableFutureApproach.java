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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static utils.Utils.isValidWord;
import static utils.Utils.partitionList;
import static utils.Utils.printElapseTime;
import static utils.Utils.printMemoryUsage;
import static utils.Utils.printNrOfEntriesInConcurrentHashMap;
import static utils.Utils.printTopNwordsInConcurrentHashMap;
import static utils.Utils.getListOfPages;

// todo mudar para concurrenthashmap

/*
 * This class uses CompletableFuture to process batches of pages concurrently.
 * It counts the occurrences of words in the text of each page and stores the results in a ConcurrentHashMap.
 * The main method initializes the processing and prints the results.
 */
public class CompletableFutureApproach {

    static final int maxPages = 100000;
    //static final String fileName = "src/main/resources/enwiki.xml";
    static final String fileName = "src/main/resources/enwiki3.xml";
    static final int TOP_N = 3; // fui eu que adicionei
    static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    static final int THRESHOLD = 1500;

    private static final ConcurrentHashMap<String, Integer> counts =
            new ConcurrentHashMap<String, Integer>();
    static final AtomicInteger processedPages = new AtomicInteger();

    public static void main(String[] args) throws Exception {

        //List<CompletableFuture<Void>> futures = new ArrayList<>();
        long start = System.currentTimeMillis();
        long memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(); // fui eu que adicionei
        //Iterable<Page> pages = new Pages(maxPages, fileName);

        Iterable<Page> pagesIterable = new Pages(maxPages, fileName);
        List<Page> pages = getListOfPages(pagesIterable);

        // Divide into batches
        int batchSize = (int) Math.ceil((double) pages.size() / (NUM_THREADS * 4));
        List<List<Page>> batches = partitionList(pages, batchSize); // todo NUM_THREADS OU THRESHOLD??

        //ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS-1);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (List<Page> batch : batches) {
            // Run each batch in a separate CompletableFuture
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> processBatch(batch))
                    .exceptionally(
                            ex -> {
                                System.err.println("Error processing batch: " + ex.getMessage());
                                return null;
                            }
                            ); //, executor)
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        //executor.shutdown();

        // Print Metrics
        System.out.println("Processed pages: " + processedPages);
        printElapseTime(start);
        printTopNwordsInConcurrentHashMap(TOP_N, counts);
        printMemoryUsage(memoryBefore);
        printNrOfEntriesInConcurrentHashMap(counts);

    }

    /*
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
