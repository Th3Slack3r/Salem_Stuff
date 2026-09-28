package org.ender.wiki;

public class Request {
   String name;
   Request.Callback callback = null;
   Request.Type type = Request.Type.ITEM;

   public Request(String name, Request.Callback callback, Request.Type type) {
      this.name = name;
      this.callback = callback;
      this.type = type;
   }

   public interface Callback {
      void wiki_item_ready(Item var1);
   }

   public static enum Type {
      ITEM,
      SEARCH;
   }
}
