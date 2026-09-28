package haven;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class IDRef {
  private static Map<Object, WRef> map = new HashMap<>();
  
  private static ReferenceQueue<IDRef> queue = new ReferenceQueue<>();
  
  private static int nextseq = 0;
  
  private final Object val;
  
  private final int seq;
  
  private IDRef(Object val) {
    synchronized (IDRef.class) {
      this.seq = nextseq++;
    } 
    this.val = val;
  }
  
  private static class WRef extends WeakReference<IDRef> {
    private final Object val;
    
    private WRef(IDRef ref, Object val) {
      super(ref, IDRef.queue);
      this.val = val;
    }
  }
  
  public static IDRef intern(Object x) {
    if (x == null)
      return null; 
    synchronized (map) {
      WRef old;
      while ((old = (WRef)queue.poll()) != null) {
        if (map.get(old.val) == old)
          map.remove(old.val); 
      } 
      WRef ref = map.get(x);
      IDRef id = (ref == null) ? null : ref.get();
      if (id == null) {
        id = new IDRef(x);
        ref = new WRef(id, x);
        map.put(x, ref);
      } 
      return id;
    } 
  }
  
  public String toString() {
    return "<ID: " + this.val + " (" + this.seq + ")>";
  }
}
