package org.ender.updater;

public interface IUpdaterListener {
   void log(String var1);

   void fisnished();

   void progress(long var1, long var3);
}
