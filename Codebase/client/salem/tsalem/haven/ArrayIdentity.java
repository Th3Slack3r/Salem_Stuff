package haven;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;

public class ArrayIdentity {
  private static HashMap<Entry<?>, Entry<?>> set = new HashMap<>();
  
  private static ReferenceQueue<Object> cleanq = new ReferenceQueue();
  
  private static class Entry<T> extends WeakReference<T[]> {
    private Entry(T[] arr) {
      super(arr, (ReferenceQueue)ArrayIdentity.cleanq);
    }
    
    public boolean equals(Object x) {
      if (!(x instanceof Entry))
        return false; 
      T[] a = get();
      if (a == null)
        return false; 
      Entry<?> e = (Entry)x;
      Object[] ea = e.get();
      if (ea == null)
        return false; 
      if (ea.length != a.length)
        return false; 
      for (int i = 0; i < a.length; i++) {
        if (a[i] != ea[i])
          return false; 
      } 
      return true;
    }
    
    public int hashCode() {
      T[] a = get();
      if (a == null)
        return 0; 
      int ret = 1;
      for (T o : a)
        ret = ret * 31 + System.identityHashCode(o); 
      return ret;
    }
  }
  
  private static void clean() {
    Reference<?> ref;
    while ((ref = cleanq.poll()) != null)
      set.remove(ref); 
  }
  
  private static <T> Entry<T> getcanon(Entry<T> e) {
    return (Entry<T>)set.get(e);
  }
  
  public static <T> T[] intern(T[] arr) {
    Entry<T> e = new Entry<>((Object[])arr);
    synchronized (ArrayIdentity.class) {
      T[] ret;
      clean();
      Entry<T> e2 = getcanon(e);
      if (e2 == null) {
        set.put(e, e);
        ret = arr;
      } else {
        ret = e2.get();
        if (ret == null) {
          set.remove(e2);
          set.put(e, e);
          ret = arr;
        } 
      } 
      return ret;
    } 
  }
}
