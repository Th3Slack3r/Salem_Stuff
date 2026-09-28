package haven;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class WeakList<T> extends AbstractCollection<T> {
   private final ReferenceQueue<T> cleanq = new ReferenceQueue<>();
   private WeakList.Entry<T> head = null;

   private void clean() {
      Reference<? extends T> ref;
      while ((ref = this.cleanq.poll()) != null) {
         WeakList.Entry e = (WeakList.Entry)ref;
         e.unlink();
      }
   }

   @Override
   public Iterator<T> iterator() {
      this.clean();
      return new Iterator<T>() {
         WeakList.Entry<T> c = WeakList.this.head;
         WeakList.Entry<T> l = null;
         T n = (T)null;

         @Override
         public boolean hasNext() {
            while (this.n == null) {
               if (this.c == null) {
                  return false;
               }

               this.n = (this.l = this.c).get();
               this.c = this.c.n;
            }

            return true;
         }

         @Override
         public T next() {
            if (!this.hasNext()) {
               throw new NoSuchElementException();
            } else {
               T ret = this.n;
               this.n = null;
               return ret;
            }
         }

         @Override
         public void remove() {
            if (this.l == null) {
               throw new IllegalStateException();
            } else {
               this.l.unlink();
               this.l = null;
            }
         }
      };
   }

   public WeakList.Entry<T> add2(T e) {
      this.clean();
      WeakList.Entry<T> n = new WeakList.Entry<>(e, this);
      n.link();
      return n;
   }

   @Override
   public boolean add(T e) {
      this.add2(e);
      return true;
   }

   @Override
   public void clear() {
      this.head = null;
   }

   @Override
   public int size() {
      int ret = 0;

      for (T e : this) {
         ret++;
      }

      return ret;
   }

   public static class Entry<E> extends WeakReference<E> {
      private WeakList.Entry<E> n;
      private WeakList.Entry<E> p;
      private WeakList<E> l;

      private Entry(E e, WeakList<E> l) {
         super(e, l.cleanq);
         this.l = l;
      }

      private void link() {
         this.n = this.l.head;
         if (this.l.head != null) {
            this.l.head.p = this;
         }

         this.l.head = this;
      }

      private void unlink() {
         if (this.n != null) {
            this.n.p = this.p;
         }

         if (this.p != null) {
            this.p.n = this.n;
         }

         if (this.l.head == this) {
            this.l.head = this.n;
         }

         this.n = this.p = null;
      }

      public void remove() {
         if (this.l == null) {
            throw new IllegalStateException();
         } else {
            this.unlink();
            this.l = null;
         }
      }
   }
}
