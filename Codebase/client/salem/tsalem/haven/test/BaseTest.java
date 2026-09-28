package haven.test;

import haven.Audio;
import haven.Resource;

public abstract class BaseTest implements Runnable {
  public ThreadGroup tg;
  
  public Thread me;
  
  public BaseTest() {
    this.tg = new ThreadGroup("Test process");
    Resource.loadergroup = this.tg;
    Audio.enabled = false;
    Runtime.getRuntime().addShutdownHook(new Thread() {
          public void run() {
            BaseTest.printf("Terminating test upon JVM shutdown...", new Object[0]);
            BaseTest.this.stop();
            try {
              BaseTest.this.me.join();
              BaseTest.printf("Shut down cleanly", new Object[0]);
            } catch (InterruptedException e) {
              BaseTest.printf("Termination handler interrupted", new Object[0]);
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
