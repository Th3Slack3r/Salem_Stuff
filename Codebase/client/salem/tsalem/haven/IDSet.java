package haven;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;

public class IDSet<T> {
  private final HashMap<WRef<T>, WRef<T>> bk = new HashMap<>();
  
  private final ReferenceQueue<T> queue = new ReferenceQueue<>();
  
  private static class WRef<T> extends WeakReference<T> {
    private final int hash;
    
    private WRef(T ob, ReferenceQueue<T> queue) {
      super(ob, queue);
      this.hash = ob.hashCode();
    }
    
    public boolean equals(Object o) {
      if (!(o instanceof WRef))
        return false; 
      WRef<?> r = (WRef)o;
      return Utils.eq(get(), r.get());
    }
    
    public int hashCode() {
      return this.hash;
    }
  }
  
  private void clean() {
    WRef<?> old;
    while ((old = (WRef)this.queue.poll()) != null)
      this.bk.remove(old); 
  }
  
  public T intern(T ob) {
    synchronized (this.bk) {
      clean();
      WRef<T> ref = new WRef<>(ob, this.queue);
      WRef<T> old = this.bk.get(ref);
      if (old == null) {
        this.bk.put(ref, ref);
        return ob;
      } 
      return old.get();
    } 
  }
}
