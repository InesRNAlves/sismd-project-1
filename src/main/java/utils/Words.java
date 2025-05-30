package utils;

import java.text.BreakIterator;
import java.util.Iterator;

/**
 * This class is used to iterate over words in a given text.
 * It implements the Iterable interface to allow iteration over the words.
 */
public class Words implements Iterable<String> {

  private final String text;

  public Words(String text) {
    this.text = text;
  }

  private class WordIterator implements Iterator<String> {

    private final BreakIterator wordBoundary;
    private int start;
    private int end;

    public WordIterator() {
      wordBoundary = BreakIterator.getWordInstance();
      wordBoundary.setText(text);
      start = wordBoundary.first();
      end = wordBoundary.next();
    }

    public boolean hasNext() { return end != BreakIterator.DONE; }

    public String next() {
      String s = text.substring(start, end);
      start = end;
      end = wordBoundary.next();
      return s;
    }

    public void remove() { throw new UnsupportedOperationException(); }
  }

  public Iterator<String> iterator() {
    return new WordIterator();
  }
}