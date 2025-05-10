package utils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Utils {

    public static void printMemoryUsage(long before) {
        long after = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(); // fui eu que adicionei
        System.out.println("Memory used: " + (after - before) / (1024 * 1024) + " MB"); // fui eu que adicionei
        //System.out.printf("Memory used: %.2f MB%n", (after - before) / (1024.0 * 1024.0));
    }

    public static void printTopNwordsInHashMap(int top_n, HashMap<String, Integer> counts) {
        LinkedHashMap<String, Integer> commonWords = new LinkedHashMap<>();
        counts.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).forEachOrdered(x -> commonWords.put(x.getKey(), x.getValue()));
        commonWords.entrySet().stream().limit(top_n).collect(Collectors.toList()).forEach(x -> System.out.println("Word: \'" + x.getKey() + "\' with total " + x.getValue() + " occurrences!"));
    }

    public static void printTopNwordsInConcurrentHashMap(int top_n, ConcurrentHashMap<String, Integer> counts) {
        LinkedHashMap<String, Integer> commonWords = new LinkedHashMap<>();
        counts.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).forEachOrdered(x -> commonWords.put(x.getKey(), x.getValue()));
        commonWords.entrySet().stream().limit(top_n).collect(Collectors.toList()).forEach(x -> System.out.println("Word: \'" + x.getKey() + "\' with total " + x.getValue() + " occurrences!"));
    }

    public static void printNrOfEntriesInHashMap(HashMap<String, Integer> wordCounts) {
        System.out.println("Number of distinct words: " + wordCounts.size()); // fui eu que adicionei
    }

    public static void printNrOfEntriesInConcurrentHashMap(ConcurrentHashMap<String, Integer> wordCounts) {
        System.out.println("Number of distinct words: " + wordCounts.size()); // fui eu que adicionei
    }

    public static void printElapseTime(long start) {
        long end = System.currentTimeMillis();
        System.out.println("Elapsed time: " + (end - start) + "ms");
    }

    public static boolean isValidWord(String word) {
        int len = word.length();
        if (len == 0 || len > 45) return false;

        if (len == 1 && !(word.equals("a") || word.equals("i"))) return false;

        for (int i = 0; i < len; i++) {
            char c = word.charAt(i);
            if (c < 'a' || c > 'z') return false;
        }

        return true;
    }

}
