package haven;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.WeakHashMap;

public class Defer extends ThreadGroup {
  private static final Map<ThreadGroup, Defer> groups = new WeakHashMap<>();
  
  private final Queue<Future<?>> queue = new PrioQueue<>();
  
  private final Collection<Thread> pool = new LinkedList<>();
  
  private final int maxthreads = 2;
  
  public static interface Callable<T> {
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
  
  public static class NotDoneException extends Loading {
    public final Defer.Future future;
    
    public NotDoneException(Defer.Future future) {
      this.future = future;
    }
    
    public boolean canwait() {
      return true;
    }
    
    public void waitfor() throws InterruptedException {
      synchronized (this.future) {
        while (!this.future.done())
          this.future.wait(); 
      } 
    }
  }
  
  public class Future<T> implements Runnable, Prioritized {
    public final Defer.Callable<T> task;
    
    private int prio = 0;
    
    private T val;
    
    private volatile String state = "";
    
    private RuntimeException exc = null;
    
    private Thread running = null;
    
    private int fAs;
    
    public void cancel() {
      synchronized (this) {
        if (this.running != null) {
          this.running.interrupt();
        } else {
          this.exc = new Defer.CancelledException();
          chstate("done");
        } 
      } 
    }
    
    private void chstate(String nst) {
      synchronized (this) {
        this.state = nst;
        notifyAll();
      } 
    }
    
    public void run() {
      synchronized (this) {
        if (this.state == "done")
          return; 
        this.running = Thread.currentThread();
      } 
      try {
        this.val = this.task.call();
        chstate("done");
      } catch (InterruptedException exc) {
        this.exc = new Defer.CancelledException(exc);
        chstate("done");
      } catch (Loading loading) {
      
      } catch (RuntimeException exc) {
        this.exc = exc;
        chstate("done");
      } finally {
        if (this.state != "done")
          chstate("resched"); 
        this.running = null;
      } 
    }
    
    private Future(Defer.Callable<T> task) {
      this.fAs = 0;
      this.task = task;
    }
    
    public T get() {
      synchronized (this) {
        boostprio(5);
        if (this.state == "done")
          if (this.exc != null) {
            this.fAs++;
            if (this.fAs > 10)
              throw new Defer.DeferredException(this.exc); 
            try {
              UI.instance.message("[Defer] " + this, GameUI.MsgType.INFO);
            } catch (Exception exception) {}
            Defer.this.defer(this);
            this.state = "";
          } else {
            return this.val;
          }  
        if (this.state == "resched") {
          Defer.this.defer(this);
          this.state = "";
        } 
        throw new Defer.NotDoneException(this);
      } 
    }
    
    public boolean done() {
      synchronized (this) {
        boostprio(5);
        if (this.state == "resched") {
          Defer.this.defer(this);
          this.state = "";
        } 
        return (this.state == "done");
      } 
    }
    
    public int priority() {
      return this.prio;
    }
    
    public void boostprio(int prio) {
      synchronized (this) {
        if (this.prio < prio)
          this.prio = prio; 
      } 
    }
  }
  
  private class Worker extends HackThread {
    private Worker() {
      super(Defer.this, (Runnable)null, "Worker thread");
      setDaemon(true);
    }
    
    public void run() {
      while (true) {
        try {
          Defer.Future<?> f;
          long start = System.currentTimeMillis();
          synchronized (Defer.this.queue) {
            while ((f = Defer.this.queue.poll()) == null) {
              if (System.currentTimeMillis() - start > 5000L)
                return; 
              Defer.this.queue.wait(1000L);
            } 
          } 
          f.run();
        } catch (InterruptedException e) {
          return;
        } finally {
          synchronized (Defer.this.queue) {
            Defer.this.pool.remove(this);
            if (Defer.this.pool.size() < 1 && !Defer.this.queue.isEmpty()) {
              Thread n = new Worker();
              n.start();
              Defer.this.pool.add(n);
            } 
          } 
        } 
      } 
    }
  }
  
  public Defer(ThreadGroup parent) {
    super(parent, "DPC threads");
  }
  
  private void defer(Future<?> f) {
    synchronized (this.queue) {
      boolean e = this.queue.isEmpty();
      this.queue.add(f);
      this.queue.notify();
      getClass();
      if ((this.pool.isEmpty() || !e) && this.pool.size() < 2) {
        Thread n = new Worker();
        n.start();
        this.pool.add(n);
      } 
    } 
  }
  
  public <T> Future<T> defer(Callable<T> task, boolean run) {
    Future<T> f = new Future<>(task);
    if (run) {
      defer(f);
    } else {
      f.chstate("resched");
    } 
    return f;
  }
  
  public <T> Future<T> defer(Callable<T> task) {
    return defer(task, true);
  }
  
  public static <T> Future<T> later(Callable<T> task, boolean run) {
    Defer d;
    ThreadGroup tg = Thread.currentThread().getThreadGroup();
    if (tg instanceof Defer)
      return ((Defer)tg).defer(task, run); 
    synchronized (groups) {
      if ((d = groups.get(tg)) == null)
        groups.put(tg, d = new Defer(tg)); 
    } 
    return d.defer(task, run);
  }
  
  public static <T> Future<T> later(Callable<T> task) {
    return later(task, true);
  }
}
