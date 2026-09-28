package haven;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class I2<T> implements Iterator<T> {
   private Iterator<Iterator<T>> is;
   private Iterator<T> cur;
   private T co;
   private boolean hco;

   public I2(Iterator<T>... is) {
      this.is = Arrays.asList(is).iterator();
      this.f();
   }

   public I2(Collection<Iterator<T>> is) {
      this.is = is.iterator();
      this.f();
   }

   private void f() {
      while (this.cur == null || !this.cur.hasNext()) {
         if (!this.is.hasNext()) {
            this.hco = false;
            return;
         }

         this.cur = this.is.next();
      }

      this.co = this.cur.next();
      this.hco = true;
   }

   @Override
   public boolean hasNext() {
      return this.hco;
   }

   @Override
   public T next() {
      if (!this.hco) {
         throw new NoSuchElementException();
      } else {
         T ret = this.co;
         this.f();
         return ret;
      }
   }

   @Override
   public void remove() {
      throw new UnsupportedOperationException();
   }
}
