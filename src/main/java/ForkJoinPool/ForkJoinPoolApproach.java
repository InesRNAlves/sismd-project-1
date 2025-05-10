package ForkJoinPool;


import utils.Page;
import utils.Pages;
import utils.Words;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

import static utils.Utils.isValidWord;
import static utils.Utils.printElapseTime;
import static utils.Utils.printMemoryUsage;
import static utils.Utils.printNrOfEntriesInConcurrentHashMap;
import static utils.Utils.printTopNwordsInConcurrentHashMap;

// todo Adicionar threshold e testar com e sem
// todo Mudar para concurrenthashmap


public class ForkJoinPoolApproach {

    static final int maxPages = 100000;
    static final String fileName = "src/main/resources/enwiki.xml";
    static final int TOP_N = 3; // fui eu que adicionei
    static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    static final int THRESHOLD = 1500; // fui eu que adicionei

    private static final ConcurrentHashMap<String, Integer> counts =
            new ConcurrentHashMap<>();
    static AtomicInteger pageCount = new AtomicInteger();

    public static void main(String[] args) throws Exception {

        long start = System.currentTimeMillis();
        long memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(); // fui eu que adicionei

        Iterable<Page> pagesIterable = new Pages(maxPages, fileName);
        List<Page>  pages = getListOfPages(pagesIterable);

        ForkJoinPool pool = new ForkJoinPool();
        PageProcessor pageProcessor = new PageProcessor(pages, 0, pages.size());
        pool.invoke(pageProcessor);
        //System.out.println("Pages processed with fork join: " + pageProcessor.join().getPagesProcessed());
        pool.shutdown();

        System.out.println("Processed pages: " + pageCount);
        //System.out.println("Distinct words with fork join" + pageProcessor.join().getCounts().size());
        printElapseTime(start);
        printTopNwordsInConcurrentHashMap(TOP_N, counts);
        printMemoryUsage(memoryBefore); // fui eu que adicionei
        printNrOfEntriesInConcurrentHashMap(counts);

    }

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
            int count = 0;
            if (end - start <= THRESHOLD) {
                for (int i = start; i < end; i++) {
                    Page page = pages.get(i);
                    for (String word : new Words(page.getText())) {
                        word = word.toLowerCase();
                        if (isValidWord(word)) {
                            counts.merge(word, 1, Integer::sum);
                        }
                    }
                    pageCount.incrementAndGet();
                    count++;
                }
            } else {
                int mid = (start + end) / 2;
                PageProcessor left = new PageProcessor(pages, start, mid);
                PageProcessor right = new PageProcessor(pages, mid, end);
                List<PageProcessor> tasks = new ArrayList<>();
                tasks.add(left);
                tasks.add(right);
                left.fork();
                right.fork();

                for (PageProcessor task : tasks) {
                    //count += task.join();
                    task.join();
                }

                //invokeAll(left, right); // fork + join automatically
            }
            return count;
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
}
