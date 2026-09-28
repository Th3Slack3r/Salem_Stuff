package org.ender.timer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import haven.Config;
import haven.Utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

public class TimerController extends Thread {
   private static TimerController instance;
   private static File config;
   public List<Timer> timers;
   public final Object lock = new Object();

   public static TimerController getInstance() {
      return instance;
   }

   public TimerController() {
      super("Timer Thread");
      this.load();
      this.setDaemon(true);
      this.start();
   }

   public static void init(String server) {
      config = Config.getFile(String.format("timer_%s.cfg", server));
      instance = new TimerController();
   }

   @Override
   public void run() {
      while (true) {
         synchronized (this.lock) {
            for (Timer timer : this.timers) {
               if (timer.isWorking() && timer.update()) {
                  timer.stop();
               }
            }
         }

         try {
            sleep(1000L);
         } catch (InterruptedException var5) {
         }
      }
   }

   public void add(Timer timer) {
      synchronized (this.lock) {
         this.timers.add(timer);
         this.save();
      }
   }

   public void remove(Timer timer) {
      synchronized (this.lock) {
         this.timers.remove(timer);
      }
   }

   private void load() {
      try {
         Gson gson = new GsonBuilder().create();
         InputStream is = new FileInputStream(config);
         this.timers = gson.fromJson(Utils.stream2str(is), (new TypeToken<List<Timer>>() {}).getType());
      } catch (Exception var3) {
      }

      if (this.timers == null) {
         this.timers = new LinkedList<>();
      }
   }

   public void save() {
      Gson gson = new GsonBuilder().create();
      String data = gson.toJson(this.timers);
      boolean exists = config.exists();
      if (!exists) {
         try {
            new File(config.getParent()).mkdirs();
            exists = config.createNewFile();
         } catch (IOException var11) {
         }
      }

      if (exists && config.canWrite()) {
         PrintWriter out = null;

         try {
            out = new PrintWriter(config);
            out.print(data);
         } catch (FileNotFoundException var10) {
         } finally {
            if (out != null) {
               out.close();
            }
         }
      }
   }
}
