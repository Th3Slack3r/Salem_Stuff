package haven;

import java.util.LinkedList;
import java.util.NoSuchElementException;

public class PrioQueue<E extends Prioritized> extends LinkedList<E> {
   public E peek() {
      E rv = null;
      int mp = 0;

      for (E e : this) {
         int ep = e.priority();
         if (rv == null || ep > mp) {
            mp = ep;
            rv = e;
         }
      }

      return rv;
   }

   public E element() {
      E rv;
      if ((rv = this.peek()) == null) {
         throw new NoSuchElementException();
      } else {
         return rv;
      }
   }

   public E poll() {
      E rv = this.peek();
      this.remove(rv);
      return rv;
   }

   public E remove() {
      E rv;
      if ((rv = this.poll()) == null) {
         throw new NoSuchElementException();
      } else {
         return rv;
      }
   }
}
