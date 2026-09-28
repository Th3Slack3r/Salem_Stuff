package haven;

import java.util.HashSet;
import java.util.Set;

public class HackThread extends Thread {
  private final Set<Runnable> ils;
  
  public HackThread(ThreadGroup tg, Runnable target, String name) {
    super((tg == null) ? tg() : tg, target, name);
    this.ils = new HashSet<>();
  }
  
  public HackThread(Runnable target, String name) {
    this(null, target, name);
  }
  
  public HackThread(String name) {
    this(null, name);
  }
  
  public static ThreadGroup tg() {
    return Thread.currentThread().getThreadGroup();
  }
  
  public void addil(Runnable r) {
    synchronized (this.ils) {
      this.ils.add(r);
    } 
  }
  
  public void remil(Runnable r) {
    synchronized (this.ils) {
      this.ils.remove(r);
    } 
  }
  
  public void interrupt() {
    super.interrupt();
    synchronized (this.ils) {
      for (Runnable r : this.ils)
        r.run(); 
    } 
  }
}
