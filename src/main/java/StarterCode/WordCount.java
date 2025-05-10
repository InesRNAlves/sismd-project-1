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

public class WordCount {

    static final int maxPages = 100000;
    static final String fileName = "src/main/resources/enwiki.xml";
    static final int TOP_N = 3; // fui eu que adicionei

    private static final HashMap<String, Integer> counts =
            new HashMap<String, Integer>();

    public static void main(String[] args) throws Exception {

        long start = System.currentTimeMillis();
        long memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(); // fui eu que adicionei

        Iterable<Page> pages = new Pages(maxPages, fileName);
        int processedPages = 0;
        for (Page page : pages) {
            if (page == null)
                break;
            Iterable<String> words = new Words(page.getText());

            for (String word : words){
                word = word.toLowerCase(); // fui eu que adicionei
                if (isValidWord(word)) { // fui eu que adicionei
                    countWord(word);
                  countWord(word);
                }
            }
            ++processedPages;
        }

        System.out.println("Processed pages: " + processedPages);
        printElapseTime(start);
        printTopNwordsInHashMap(TOP_N, counts);
        printMemoryUsage(memoryBefore); // fui eu que adicionei
        printNrOfEntriesInHashMap(counts);

    }

    private static void countWord(String word) {
        Integer currentCount = counts.get(word);
        if (currentCount == null){
            counts.put(word, 1);
        }else{
            counts.put(word, currentCount + 1);
        }
    }


}