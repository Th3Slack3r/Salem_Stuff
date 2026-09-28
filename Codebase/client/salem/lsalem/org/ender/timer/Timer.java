package org.ender.timer;

import haven.Audio;
import haven.Config;
import haven.Coord;
import haven.Label;
import haven.TimerPanel;
import haven.UI;
import haven.Window;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class Timer {
   private static final int SERVER_RATIO = 3;
   public static long server;
   public static long local;
   private long start;
   private long duration;
   private String name;
   private transient long remaining;
   public transient Timer.Callback updater;

   public void setDuration(long duration) {
      this.duration = duration;
   }

   private InputStream FileInputStream(String string) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean isWorking() {
      return this.start != 0L;
   }

   public void stop() {
      this.start = 0L;
      if (this.updater != null) {
         this.updater.run(this);
      }

      TimerController.getInstance().save();
   }

   public void start() {
      this.start = server + 3L * (System.currentTimeMillis() - local);
      TimerController.getInstance().save();
   }

   public synchronized boolean update() {
      long now = System.currentTimeMillis();
      this.remaining = this.duration - now + local - (server - this.start) / 3L;
      if (this.remaining <= 0L) {
         Window wnd = new Window(new Coord(250, 100), Coord.z, UI.instance.root, this.name);
         String str;
         if (this.remaining < -1500L) {
            str = String.format("%s elapsed since timer named \"%s\"  finished it's work", this.toString(), this.name);
         } else {
            str = String.format("Timer named \"%s\" just finished it's work", this.name);
         }

         new Label(Coord.z, wnd, str);
         wnd.justclose = true;
         wnd.pack();
         if (!TimerPanel.isSilenced()) {
            InputStream file = null;

            try {
               var7 = new FileInputStream(Config.userhome + "/timer.wav");
            } catch (FileNotFoundException var5) {
               var7 = Timer.class.getResourceAsStream("/timer.wav");
            }

            Audio.play((InputStream)var7, 1.0, 1.0);
         }

         return true;
      } else {
         if (this.updater != null) {
            this.updater.run(this);
         }

         return false;
      }
   }

   public synchronized long getStart() {
      return this.start;
   }

   public synchronized void setStart(long start) {
      this.start = start;
   }

   public synchronized String getName() {
      return this.name;
   }

   public synchronized void setName(String name) {
      this.name = name;
   }

   public synchronized long getDuration() {
      return this.duration;
   }

   public synchronized long getFinishDate() {
      return this.duration + local - (server - this.start) / 3L;
   }

   @Override
   public String toString() {
      long t = Math.abs(this.isWorking() ? this.remaining : this.duration) / 1000L;
      int h = (int)(t / 3600L);
      int m = (int)(t % 3600L / 60L);
      int s = (int)(t % 60L);
      if (h >= 24) {
         int d = h / 24;
         h %= 24;
         return String.format("%d:%02d:%02d:%02d", d, h, m, s);
      } else {
         return String.format("%d:%02d:%02d", h, m, s);
      }
   }

   public void destroy() {
      TimerController.getInstance().remove(this);
      this.updater = null;
   }

   public interface Callback {
      void run(Timer var1);
   }
}
