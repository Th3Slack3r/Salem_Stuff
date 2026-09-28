package haven.test;

import java.util.Collection;
import java.util.HashSet;

public class MultiClient extends BaseTest {
   public Collection<TestClient> clients = new HashSet<>();
   public int num;
   public int delay;
   public int started;

   public MultiClient(int num, int delay) {
      this.num = num;
      this.delay = delay;
      this.started = 0;
   }

   @Override
   public void run() {
      long lastck = System.currentTimeMillis();
      long laststarted = 0L;

      try {
         while (true) {
            long now = System.currentTimeMillis();
            long timeout = 1000L;
            if (this.started < this.num && now - laststarted >= this.delay) {
               TestClient c = new TestClient("test" + (this.started + 1));
               new CharSelector(c, null, null) {
                  @Override
                  public void succeed() {
                     System.out.println("Selected character");
                  }
               };
               synchronized (this.clients) {
                  this.clients.add(c);
               }

               c.start();
               this.started++;
               laststarted = now;
            }

            if (this.started < this.num && this.delay - (now - laststarted) < timeout) {
               timeout = this.delay - (now - laststarted);
            }

            if (timeout < 0L) {
               timeout = 0L;
            }

            try {
               Thread.sleep(timeout);
            } catch (InterruptedException var16) {
               this.num = 0;
               this.stopall();
            }

            if (now - lastck > 1000L) {
               int alive = 0;

               for (TestClient c : this.clients) {
                  if (c.alive()) {
                     alive++;
                  }
               }

               if (alive == 0 && this.started >= this.num) {
                  printf("All clients are dead, exiting", new Object[0]);
                  return;
               }

               printf("Alive: %d/%d/%d", new Object[]{alive, this.started, this.num});
               lastck = now;
            }
         }
      } finally {
         this.stopall();
      }
   }

   public void stopall() {
      synchronized (this.clients) {
         for (TestClient c : this.clients) {
            c.stop();
         }
      }
   }

   public static void usage() {
      System.err.println("usage: MultiClient NUM [DELAY]");
   }

   public static void main(String[] args) {
      if (args.length < 1) {
         usage();
         System.exit(1);
      }

      int num = Integer.parseInt(args[0]);
      int delay = 0;
      if (args.length > 1) {
         delay = Integer.parseInt(args[1]);
      }

      new MultiClient(num, delay).start();
   }
}
