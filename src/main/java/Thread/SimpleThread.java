package Thread;

import utils.Page;
import utils.Pages;
import utils.Utils;
import utils.Words;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static utils.Utils.isValidWord;
import static utils.Utils.printElapseTime;
import static utils.Utils.printMemoryUsage;
import static utils.Utils.printNrOfEntriesInConcurrentHashMap;

public class SimpleThread {
    static final int maxPages = 100000;
    static final String fileName= "src/main/resources/enwiki.xml";
    static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    static final int TOP_N = 3;

    private static final ConcurrentHashMap<String, Integer> counts = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        System.out.println("NUM_THREADS: " + NUM_THREADS);
        long before = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        Iterable<Page> pagesIterable = new Pages(maxPages, fileName);

        // Get List of pages and divide into chunks according to the  number of threads
        List<Page>  pages = getListOfPages(pagesIterable);
        int chunkSize = (int) Math.ceil((double) pages.size() / NUM_THREADS);

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < NUM_THREADS; i++) {

            // Get sublist of pages for each thread to process
            int startIdx = i * chunkSize;
            int endIdx = Math.min(startIdx + chunkSize, pages.size());
            List<Page> subList = pages.subList(startIdx, endIdx);

            Thread thread = new Thread(() -> processPages(subList));
            threads.add(thread);
            thread.start();
        }

        // Wait for all threads to finish
        for (Thread t : threads) {
            t.join();
        }

        // Print metrics
        System.out.println("Processed pages: " + pages.size());
        printElapseTime(start);
        Utils.printTopNwordsInConcurrentHashMap(TOP_N, counts);
        printMemoryUsage(before);
        printNrOfEntriesInConcurrentHashMap(counts);

    }

    private static void processPages(List<Page> pages) {
        for (Page page : pages) {
            if (page == null)
                break;
            Iterable<String> words = new Words(page.getText());
            for (String word : words){
                word = word.toLowerCase();
                if (isValidWord(word)){
                    countWord(word);
                }
            }
        }
    }

    private static void countWord(String word) {
        Integer currentCount = counts.get(word);
        if (currentCount == null)
            counts.put(word, 1);
        else
            counts.put(word, currentCount + 1);
    }

    public static List<Page> getListOfPages(Iterable<Page> pagesIterable) {
        List<Page> pages = new ArrayList<>();
        for (Page page : pagesIterable) {
            if (page == null) break;
            pages.add(page);
        }
        return pages;
    }

}
