package Thread;

import utils.Page;
import utils.Pages;
import utils.Utils;
import utils.Words;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import static utils.Utils.isValidWord;
import static utils.Utils.printElapseTime;
import static utils.Utils.printMemoryUsage;
import static utils.Utils.printNrOfEntriesInConcurrentHashMap;
import static utils.Utils.getListOfPages;

/*
 * This class uses a simple thread approach to process pages concurrently.
 * It counts the occurrences of words in the text of each page and stores the results in a ConcurrentHashMap.
 * The main method initializes the processing and prints the results.
 */
public class SimpleThread {
    static final int maxPages = 100000;
    //static final String fileName= "src/main/resources/enwiki.xml";
    static final String fileName= "src/main/resources/enwiki3.xml";
    static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    static final int TOP_N = 3;

    private static final ConcurrentHashMap<String, Integer> counts = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        long before = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        Iterable<Page> pagesIterable = new Pages(maxPages, fileName);

        // Get List of pages and divide into batches according to the number of threads
        List<Page>  pages = getListOfPages(pagesIterable);
        int batchSize = (int) Math.ceil((double) pages.size() / NUM_THREADS);

        // CountDownLatch to wait for all threads to finish without using ExecutorService
        // or having to loop through the threads to join them
        // todo verificar esta justificação
        CountDownLatch latch = new CountDownLatch(NUM_THREADS);
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < NUM_THREADS; i++) {

            // Get sublist of pages for each thread to process
            int startIdx = i * batchSize;
            int endIdx = Math.min(startIdx + batchSize, pages.size());
            List<Page> subList = pages.subList(startIdx, endIdx);

            Thread thread = new Thread(() -> {
                    processPages(subList);
                    latch.countDown();
                });
            thread.start();
        }

        // Wait for all threads to finish
        latch.await();

        // Print Metrics
        System.out.println("Processed pages: " + pages.size());
        printElapseTime(start);
        Utils.printTopNwordsInConcurrentHashMap(TOP_N, counts);
        printMemoryUsage(before);
        printNrOfEntriesInConcurrentHashMap(counts);

    }

    /*
     * Process a list of pages and count the occurrences of words in the text of each page.
     * The results are stored in a ConcurrentHashMap.
     */
    private static void processPages(List<Page> pages) {
        for (Page page : pages) {
            if (page == null)
                break;
            Iterable<String> words = new Words(page.getText());
            Map<String, Integer> localCounts = new HashMap<>();
            for (String word : words){
                word = word.toLowerCase();
                if (isValidWord(word)){
                    localCounts.merge(word, 1, Integer::sum);
                }
            }

            // Merge local counts into global counts
            localCounts.forEach((word, count) ->
                    counts.merge(word, count, Integer::sum)
            );

        }
    }
}
