package CompletableFuture;

import utils.Page;
import utils.Pages;
import utils.Words;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static utils.Utils.isValidWord;
import static utils.Utils.printElapseTime;
import static utils.Utils.printMemoryUsage;
import static utils.Utils.printNrOfEntriesInConcurrentHashMap;
import static utils.Utils.printTopNwordsInConcurrentHashMap;

// todo mudar para concurrenthashmap

public class CompletableFutureApproach {

    static final int maxPages = 100000;
    static final String fileName = "src/main/resources/enwiki.xml";
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
        List<List<Page>> batches = partitionList(pages, NUM_THREADS);

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS); // todo NUM_THREADS OU THRESHOLD??
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (List<Page> batch : batches) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> processBatch(batch), executor);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        System.out.println("Processed pages: " + processedPages);
        printElapseTime(start);
        printTopNwordsInConcurrentHashMap(TOP_N, counts);
        printMemoryUsage(memoryBefore);
        printNrOfEntriesInConcurrentHashMap(counts);

    }


    public static void processBatch(List<Page> batch) {
        for (Page page : batch) {
            for (String word : new Words(page.getText())) {
                word = word.toLowerCase();
                if (isValidWord(word)) {
                    counts.merge(word, 1, Integer::sum);
                }
            }
            processedPages.incrementAndGet();
        }
    }

    public static List<Page> getListOfPages(Iterable<Page> pagesIterable) {
        List<Page> pages = new ArrayList<>();
        for (Page page : pagesIterable) {
            if (page == null) break;
            pages.add(page);
        }
        return pages;
    }

    //todo por logica igual ao threadpools para sriar sublists
    public static List<List<Page>> partitionList(List<Page> list, int batchSize) {
        List<List<Page>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            partitions.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return partitions;
    }

}
