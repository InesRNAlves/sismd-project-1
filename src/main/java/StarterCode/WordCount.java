package StarterCode;

import utils.Page;
import utils.Pages;
import utils.Words;

import java.util.HashMap;

import static utils.Utils.isValidWord;
import static utils.Utils.printElapseTime;
import static utils.Utils.printMemoryUsage;
import static utils.Utils.printNrOfEntriesInHashMap;
import static utils.Utils.printTopNwordsInHashMap;

/**
 * This class uses a simple approach to process pages sequentially.
 * It counts the occurrences of words in the text of each page and stores the results in a HashMap.
 * The main method initializes the processing and prints the results.
 */
public class WordCount {

    static final int maxPages = 100000;
    //static final String fileName = "src/main/resources/enwiki.xml";
    static final String fileName = "src/main/resources/enwiki3.xml";
    //static final String fileName = "enwiki3.xml"; // For running the benchmark script
    static final int TOP_N = 3;

    private static final HashMap<String, Integer> counts =
            new HashMap<String, Integer>();

    public static void main(String[] args) throws Exception {

        long start = System.currentTimeMillis();
        long memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        Iterable<Page> pages = new Pages(maxPages, fileName);
        int processedPages = 0;
        for (Page page : pages) {
            if (page == null)
                break;
            Iterable<String> words = new Words(page.getText());

            for (String word : words){
                word = word.toLowerCase();
                if (isValidWord(word)) {
                    countWord(word);
                }
            }
            ++processedPages;
        }

        // Print Metrics
        System.out.println("Processed pages: " + processedPages);
        printElapseTime(start);
        printTopNwordsInHashMap(TOP_N, counts);
        printMemoryUsage(memoryBefore);
        printNrOfEntriesInHashMap(counts);

        long elapsed = System.currentTimeMillis() - start;
        long memoryUsedMB = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);

        // CSV output
        System.out.println("BATCH_SIZE,TIME_MS,MEMORY_MB,PAGES");
        System.out.printf("%d,%d\n", elapsed, memoryUsedMB);

    }

    /**
     * Counts the occurrences of a word in the text of a page.
     * If the word is not already in the map, it adds it with a count of 1.
     * If the word is already in the map, it increments the count by 1.
     *
     * @param word The word to count
     */
    private static void countWord(String word) {
        Integer currentCount = counts.get(word);
        if (currentCount == null){
            counts.put(word, 1);
        }else{
            counts.put(word, currentCount + 1);
        }
    }


}