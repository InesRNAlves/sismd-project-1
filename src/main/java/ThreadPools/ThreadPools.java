package ThreadPools;

import utils.Page;
import utils.Pages;
import utils.Utils;
import utils.Words;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static utils.Utils.isValidWord;
import static utils.Utils.printElapseTime;
import static utils.Utils.printMemoryUsage;
import static utils.Utils.printNrOfEntriesInConcurrentHashMap;

public class ThreadPools {
    static final int maxPages = 100000;
    static final String fileName= "src/main/resources/enwiki.xml";
    static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    static final int TOP_N = 3; // fui eu que adicionei

    private static final ConcurrentHashMap<String, Integer> wordCounts = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {

        long start = System.currentTimeMillis();
        System.out.println("NUM_THREADS: " + NUM_THREADS);
        //System.gc(); // Encourage garbage collection
        long before = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        List<Future<ConcurrentHashMap<String, Integer>>> futures = new ArrayList<>(); // no need if concurrenhashmap
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        //int pageCount = 0;
        AtomicInteger pageCount = new AtomicInteger();
        try {
            Iterable<Page> pagesIterable = new Pages(maxPages, fileName);
            for (Page page : pagesIterable) {
                futures.add(executor.submit(() -> {
                if (page == null) {
                    return null;
                }
                ConcurrentHashMap<String, Integer> localWordCounts = new ConcurrentHashMap<>();

                for (String word : new Words(page.getText())) {
                    word = word.toLowerCase();
                    if (isValidWord(word)) {
                        Integer currentCount = localWordCounts.get(word);
                        /*if (currentCount == null) {
                            localWordCounts.put(word, 1);
                            wordCounts.put(word, 1);
                        } else {
                            localWordCounts.put(word, currentCount + 1); // explorar esta opção
                            wordCounts.put(word, currentCount + 1);
                        }*/
                        wordCounts.merge(word, 1, Integer::sum); // this is the best option
                    }
                }
                pageCount.incrementAndGet();
                return localWordCounts;
                }));
            }

            for (Future<ConcurrentHashMap<String, Integer>> future : futures) {
                future.get();
                //pageCount++;
            }

        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }

        // Print metrics
        System.out.println("Processed pages: " + pageCount);
        printElapseTime(start);
        Utils.printTopNwordsInConcurrentHashMap(TOP_N, wordCounts);
        printMemoryUsage(before);
        printNrOfEntriesInConcurrentHashMap(wordCounts);

    }

    public static void countWord(String word) {
        Integer currentCount = wordCounts.get(word);
        if (currentCount == null)
            wordCounts.put(word, 1);
        else
            wordCounts.put(word, currentCount + 1);
    }
}
