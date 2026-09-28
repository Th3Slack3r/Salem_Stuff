package haven.test;

import haven.Audio;
import haven.Resource;

public abstract class BaseTest implements Runnable {
   public ThreadGroup tg = new ThreadGroup("Test process");
   public Thread me;

   public BaseTest() {
      Resource.loadergroup = this.tg;
      Audio.enabled = false;
      Runtime.getRuntime().addShutdownHook(new Thread() {
         @Override
         public void run() {
            BaseTest.printf("Terminating test upon JVM shutdown...");
            BaseTest.this.stop();

            try {
               BaseTest.this.me.join();
               BaseTest.printf("Shut down cleanly");
            } catch (InterruptedException var2) {
               BaseTest.printf("Termination handler interrupted");
            }
         }
      });
   }

   public static void printf(String fmt, Object... args) {
      System.out.println(String.format(fmt, args));
   }

   public void start() {
      this.me = new Thread(this.tg, this, "Test controller");
      this.me.start();
   }

   public void stop() {
      this.me.interrupt();
   }
}
