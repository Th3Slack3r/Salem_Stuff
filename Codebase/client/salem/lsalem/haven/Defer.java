package haven;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.WeakHashMap;

public class Defer extends ThreadGroup {
   private static final Map<ThreadGroup, Defer> groups = new WeakHashMap<>();
   private final Queue<Defer.Future<?>> queue = new PrioQueue<>();
   private final Collection<Thread> pool = new LinkedList<>();
   private final int maxthreads = 2;

   public Defer(ThreadGroup parent) {
      super(parent, "DPC threads");
   }

   private void defer(Defer.Future<?> f) {
      synchronized (this.queue) {
         boolean e = this.queue.isEmpty();
         this.queue.add(f);
         this.queue.notify();
         if ((this.pool.isEmpty() || !e) && this.pool.size() < 2) {
            Thread n = new Defer.Worker();
            n.start();
            this.pool.add(n);
         }
      }
   }

   public <T> Defer.Future<T> defer(Defer.Callable<T> task) {
      Defer.Future<T> f = new Defer.Future<>(task);
      this.defer(f);
      return f;
   }

   public static <T> Defer.Future<T> later(Defer.Callable<T> task) {
      ThreadGroup tg = Thread.currentThread().getThreadGroup();
      if (tg instanceof Defer) {
         return ((Defer)tg).defer(task);
      } else {
         Defer d;
         synchronized (groups) {
            if ((d = groups.get(tg)) == null) {
               groups.put(tg, d = new Defer(tg));
            }
         }

         return d.defer(task);
      }
   }

   public interface Callable<T> {
      T call() throws InterruptedException;
   }

   public static class CancelledException extends RuntimeException {
      public CancelledException() {
         super("Execution cancelled");
      }

      public CancelledException(Throwable cause) {
         super(cause);
      }
   }

   public static class DeferredException extends RuntimeException {
      public DeferredException(Throwable cause) {
         super(cause);
      }
   }

   public class Future<T> implements Runnable, Prioritized {
      public final Defer.Callable<T> task;
      private int prio = 0;
      private T val;
      private volatile String state = "";
      private RuntimeException exc = null;
      private Thread running = null;

      private Future(Defer.Callable<T> task) {
         this.task = task;
      }

      public void cancel() {
         synchronized (this) {
            if (this.running != null) {
               this.running.interrupt();
            } else {
               this.exc = new Defer.CancelledException();
               this.chstate("done");
            }
         }
      }

      private void chstate(String nst) {
         synchronized (this) {
            this.state = nst;
            this.notifyAll();
         }
      }

      @Override
      public void run() {
         synchronized (this) {
            if (this.state == "done") {
               return;
            }

            this.running = Thread.currentThread();
         }

         try {
            this.val = this.task.call();
            this.chstate("done");
         } catch (InterruptedException var9) {
            this.exc = new Defer.CancelledException(var9);
            this.chstate("done");
         } catch (Loading var10) {
         } catch (RuntimeException var11) {
            this.exc = var11;
            this.chstate("done");
         } finally {
            if (this.state != "done") {
               this.chstate("resched");
            }

            this.running = null;
         }
      }

      public T get() {
         synchronized (this) {
            this.boostprio(5);
            if (this.state == "done") {
               if (this.exc != null) {
                  throw new Defer.DeferredException(this.exc);
               } else {
                  return this.val;
               }
            } else {
               if (this.state == "resched") {
                  Defer.this.defer(this);
                  this.state = "";
               }

               throw new Defer.NotDoneException(this);
            }
         }
      }

      public boolean done() {
         synchronized (this) {
            this.boostprio(5);
            if (this.state == "resched") {
               Defer.this.defer(this);
               this.state = "";
            }

            return this.state == "done";
         }
      }

      @Override
      public int priority() {
         return this.prio;
      }

      public void boostprio(int prio) {
         synchronized (this) {
            if (this.prio < prio) {
               this.prio = prio;
            }
         }
      }
   }

   public static class NotDoneException extends Loading {
      public final Defer.Future future;

      public NotDoneException(Defer.Future future) {
         this.future = future;
      }

      @Override
      public boolean canwait() {
         return true;
      }

      @Override
      public void waitfor() throws InterruptedException {
         synchronized (this.future) {
            while (!this.future.done()) {
               this.future.wait();
            }
         }
      }
   }

   private class Worker extends HackThread {
      private Worker() {
         super(Defer.this, null, "Worker thread");
         this.setDaemon(true);
      }

      @Override
      public void run() {
         try {
            while (true) {
               Defer.Future<?> f;
               try {
                  long start = System.currentTimeMillis();
                  synchronized (Defer.this.queue) {
                     while ((f = Defer.this.queue.poll()) == null) {
                        if (System.currentTimeMillis() - start > 5000L) {
                           return;
                        }

                        Defer.this.queue.wait(1000L);
                     }
                  }
               } catch (InterruptedException var22) {
                  return;
               }

               f.run();
               Object var24 = null;
            }
         } finally {
            synchronized (Defer.this.queue) {
               Defer.this.pool.remove(this);
               if (Defer.this.pool.size() < 1 && !Defer.this.queue.isEmpty()) {
                  Thread n = Defer.this.new Worker();
                  n.start();
                  Defer.this.pool.add(n);
               }
            }
         }
      }
   }
}
