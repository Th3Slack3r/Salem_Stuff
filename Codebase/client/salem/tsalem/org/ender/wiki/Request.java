package org.ender.wiki;

public class Request {
  String name;
  
  Callback callback = null;
  
  Type type = Type.ITEM;
  
  public Request(String name, Callback callback, Type type) {
    this.name = name;
    this.callback = callback;
    this.type = type;
  }
  
  public enum Type {
    ITEM, SEARCH;
  }
  
  public static interface Callback {
    void wiki_item_ready(Item param1Item);
  }
}
