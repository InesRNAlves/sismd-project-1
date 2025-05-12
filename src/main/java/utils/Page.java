package utils;

/**
 * This class represents a page with a title and text.
 * It provides methods to get the title and text of the page.
 */
public class Page {
  private String title;
  private String text;

  public Page(String title, String text) { this.title = title; this.text = text; }

  public String getTitle() { return title; }
  public String getText() { return text; }
}
