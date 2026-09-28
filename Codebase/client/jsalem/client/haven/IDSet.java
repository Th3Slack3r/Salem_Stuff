package haven;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;

public class IDSet<T> {
   private final HashMap<IDSet.WRef<T>, IDSet.WRef<T>> bk = new HashMap<>();
   private final ReferenceQueue<T> queue = new ReferenceQueue<>();

   private void clean() {
      IDSet.WRef<?> old;
      while ((old = (IDSet.WRef<?>)this.queue.poll()) != null) {
         this.bk.remove(old);
      }
   }

   public T intern(T ob) {
      synchronized (this.bk) {
         this.clean();
         IDSet.WRef<T> ref = new IDSet.WRef<>(ob, this.queue);
         IDSet.WRef<T> old = this.bk.get(ref);
         if (old == null) {
            this.bk.put(ref, ref);
            return ob;
         } else {
            return old.get();
         }
      }
   }

   private static class WRef<T> extends WeakReference<T> {
      private final int hash;

      private WRef(T ob, ReferenceQueue<T> queue) {
         super(ob, queue);
         this.hash = ob.hashCode();
      }

      @Override
      public boolean equals(Object o) {
         if (!(o instanceof IDSet.WRef)) {
            return false;
         } else {
            IDSet.WRef<?> r = (IDSet.WRef<?>)o;
            return Utils.eq(this.get(), r.get());
         }
      }

      @Override
      public int hashCode() {
         return this.hash;
      }
   }
}
