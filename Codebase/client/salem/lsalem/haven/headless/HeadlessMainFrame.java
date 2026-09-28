package haven.headless;

import haven.Config;
import haven.Console;
import haven.HackThread;
import haven.MainFrame;
import haven.RemoteUI;
import haven.ResCache;
import haven.Resource;
import haven.Session;
import haven.UI;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class HeadlessMainFrame implements Runnable, Console.Directory {
   public static HeadlessMainFrame instance;
   HeadlessHavenPanel p;
   private final ThreadGroup g;
   public final Thread mt;

   @Override
   public Map<String, Console.Command> findcmds() {
      return MainFrame.cmdmap;
   }

   public HeadlessMainFrame() {
      instance = this;
      this.g = new ThreadGroup(HackThread.tg(), "Haven client");
      this.mt = new HackThread(this.g, this, "Haven main thread");
      this.p = new HeadlessHavenPanel(this);
   }

   @Override
   public void run() {
      boolean played = false;
      if (Thread.currentThread() != this.mt) {
         throw new RuntimeException("HeadlessMainFrame is being run from an invalid context");
      } else {
         Thread ui = new HackThread(this.p, "Haven Main thread");
         ui.start();
         Config.headless = true;

         try {
            Session sess = null;

            while (true) {
               while (sess != null) {
                  UI.Runner fun = new RemoteUI(sess);
                  fun.run(this.p.newheadlessui(sess, this));
                  System.exit(0);
               }

               UI.Runner fun = new HeadlessBootstrap(Config.defserv, Config.mainport);
               sess = fun.run(this.p.newheadlessui(sess, this));
            }
         } catch (InterruptedException var8) {
         } finally {
            ui.interrupt();
         }
      }
   }

   public static void setupres() {
      if (ResCache.global != null) {
         Resource.addcache(ResCache.global);
      }

      if (Config.resurl != null) {
         Resource.addurl(Config.resurl);
      }

      if (ResCache.global != null) {
         try {
            Resource.loadlist(ResCache.global.fetch("tmp/allused"), -10);
         } catch (IOException var2) {
         }
      }

      if (!Config.nopreload) {
         try {
            InputStream pls = Resource.class.getResourceAsStream("res-preload");
            if (pls != null) {
               Resource.loadlist(pls, -5);
            }

            pls = Resource.class.getResourceAsStream("res-bgload");
            if (pls != null) {
               Resource.loadlist(pls, -10);
            }
         } catch (IOException var1) {
            throw new Error(var1);
         }
      }
   }

   private static void main2(String[] args) {
      Config.cmdline(args);
      setupres();
      HeadlessMainFrame f = new HeadlessMainFrame();
      f.mt.start();

      try {
         f.mt.join();
      } catch (InterruptedException var3) {
         f.g.interrupt();
      }
   }

   public static void main(final String[] args) {
      ThreadGroup g = new ThreadGroup("Haven main group");
      Thread main = new HackThread(g, new Runnable() {
         @Override
         public void run() {
            HeadlessMainFrame.main2(args);
         }
      }, "Haven main thread");
      main.start();
   }
}
