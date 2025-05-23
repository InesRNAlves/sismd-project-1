package ForkJoinPool;


import utils.Page;
import utils.Pages;
import utils.Words;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

import static utils.Utils.getListOfPages;
import static utils.Utils.isValidWord;
import static utils.Utils.printElapseTime;
import static utils.Utils.printMemoryUsage;
import static utils.Utils.printNrOfEntriesInConcurrentHashMap;
import static utils.Utils.printTopNwordsInConcurrentHashMap;


/**
 * This class uses ForkJoinPool to process pages concurrently.
 * It counts the occurrences of words in the text of each page and stores the results in a ConcurrentHashMap.
 * The main method initializes the processing and prints the results.
 */
public class ForkJoinPoolApproach {

    static final int maxPages = 100000;
    //static final String fileName = "src/main/resources/enwiki.xml";
    static final String fileName = "src/main/resources/enwiki3.xml";
    //static final String fileName= "enwiki2.xml"; // For running the benchmark script
    //static final String fileName= "enwiki3.xml";// For running the benchmark script
    static final int TOP_N = 3;
    static int THRESHOLD = 300;

    private static final ConcurrentHashMap<String, Integer> counts =
            new ConcurrentHashMap<>();
    static AtomicInteger pageCount = new AtomicInteger();

    public static void main(String[] args) throws Exception {

        long start = System.currentTimeMillis();
        long memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        Iterable<Page> pagesIterable = new Pages(maxPages, fileName);
        List<Page>  pages = getListOfPages(pagesIterable);

        ForkJoinPool pool = new ForkJoinPool();
        PageProcessor pageProcessor = new PageProcessor(pages, 0, pages.size());
        pool.submit(pageProcessor); // submit over invoke because we are not using the result and its non-blocking
        pageProcessor.join(); // wait for the task to finish
        pool.shutdown();

        // Print Metrics
        System.out.println("Processed pages: " + pageCount);
        printElapseTime(start);
        printTopNwordsInConcurrentHashMap(TOP_N, counts);
        printMemoryUsage(memoryBefore);
        printNrOfEntriesInConcurrentHashMap(counts);

        long elapsed = System.currentTimeMillis() - start;
        long memoryUsedMB = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() - memoryBefore) / (1024 * 1024);
        System.out.printf("%d,%d,\n", elapsed, memoryUsedMB);

    }

    /**
     * This class extends a RecursiveTask to process a list of pages.
     * The task is divided into smaller subtasks if the size exceeds a threshold.
     */
    static class PageProcessor extends RecursiveTask<Integer> {
        private final List<Page> pages;
        private final int start, end;

        PageProcessor(List<Page> pages, int start, int end) {
            this.pages = pages;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Integer compute() {
            // Sequential processing if the size is below the threshold
            if (end - start <= THRESHOLD) {
                for (int i = start; i < end; i++) {
                    Page page = pages.get(i);
                    Map<String, Integer> localCounts = new HashMap<>();
                    for (String word : new Words(page.getText())) {
                        word = word.toLowerCase();
                        if (isValidWord(word)) {
                            localCounts.merge(word, 1, Integer::sum);
                        }
                    }

                    // Merge local counts into global counts
                    localCounts.forEach((word, wordCount) ->
                            counts.merge(word, wordCount, Integer::sum)
                    );

                    pageCount.incrementAndGet();
                }
            } else {
                // Divide the task into subtasks
                int mid = (start + end) / 2;
                PageProcessor left = new PageProcessor(pages, start, mid);
                PageProcessor right = new PageProcessor(pages, mid, end);
                List<PageProcessor> tasks = new ArrayList<>();
                tasks.add(left);
                tasks.add(right);
                left.fork();
                right.fork();

                for (PageProcessor task : tasks) {
                    task.join();
                }
            }
            return 1;
        }
    }
}
