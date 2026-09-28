package haven;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class SIterator<T> implements Iterator<T> {
   private int st = 0;
   private T n;

   public abstract T snext() throws NoSuchElementException;

   private void ref() {
      if (this.st == 0) {
         try {
            this.n = this.snext();
            this.st = 1;
         } catch (NoSuchElementException var2) {
            this.st = 2;
         }
      }
   }

   @Override
   public boolean hasNext() {
      this.ref();
      return this.st == 1;
   }

   @Override
   public T next() {
      this.ref();
      if (this.st == 2) {
         throw new NoSuchElementException();
      } else {
         this.st = 0;
         return this.n;
      }
   }

   @Override
   public void remove() {
      throw new UnsupportedOperationException();
   }
}
