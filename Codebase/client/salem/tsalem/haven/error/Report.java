package haven.error;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Report implements Serializable {
  private boolean reported = false;
  
  public final Throwable t;
  
  public final long time;
  
  public final Map<String, Object> props = new HashMap<>();
  
  public Report(Throwable t) {
    this.t = t;
    this.time = System.currentTimeMillis();
    Runtime rt = Runtime.getRuntime();
    this.props.put("mem.free", Long.valueOf(rt.freeMemory()));
    this.props.put("mem.total", Long.valueOf(rt.totalMemory()));
    this.props.put("mem.max", Long.valueOf(rt.maxMemory()));
  }
  
  synchronized void join() throws InterruptedException {
    while (!this.reported)
      wait(); 
  }
  
  synchronized void done() {
    this.reported = true;
    notifyAll();
  }
}
