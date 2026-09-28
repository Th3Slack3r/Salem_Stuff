package haven;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;

public class ArrayIdentity {
   private static HashMap<ArrayIdentity.Entry<?>, ArrayIdentity.Entry<?>> set = new HashMap<>();
   private static ReferenceQueue<Object> cleanq = new ReferenceQueue<>();

   private static void clean() {
      Reference<?> ref;
      while ((ref = cleanq.poll()) != null) {
         set.remove(ref);
      }
   }

   private static <T> ArrayIdentity.Entry<T> getcanon(ArrayIdentity.Entry<T> e) {
      return (ArrayIdentity.Entry<T>)set.get(e);
   }

   public static <T> T[] intern(T[] arr) {
      ArrayIdentity.Entry<T> e = new ArrayIdentity.Entry<>(arr);
      synchronized (ArrayIdentity.class) {
         clean();
         ArrayIdentity.Entry<T> e2 = getcanon(e);
         T[] ret;
         if (e2 == null) {
            set.put(e, e);
            ret = arr;
         } else {
            ret = (T[])e2.get();
            if (ret == null) {
               set.remove(e2);
               set.put(e, e);
               ret = arr;
            }
         }

         return ret;
      }
   }

   private static class Entry<T> extends WeakReference<T[]> {
      private Entry(T[] arr) {
         super(arr, ArrayIdentity.cleanq);
      }

      @Override
      public boolean equals(Object x) {
         if (!(x instanceof ArrayIdentity.Entry)) {
            return false;
         } else {
            T[] a = this.get();
            if (a == null) {
               return false;
            } else {
               ArrayIdentity.Entry<?> e = (ArrayIdentity.Entry<?>)x;
               Object[] ea = (Object[])e.get();
               if (ea == null) {
                  return false;
               } else if (ea.length != a.length) {
                  return false;
               } else {
                  for (int i = 0; i < a.length; i++) {
                     if (a[i] != ea[i]) {
                        return false;
                     }
                  }

                  return true;
               }
            }
         }
      }

      @Override
      public int hashCode() {
         T[] a = this.get();
         if (a == null) {
            return 0;
         } else {
            int ret = 1;

            for (T o : a) {
               ret = ret * 31 + System.identityHashCode(o);
            }

            return ret;
         }
      }
   }
}
