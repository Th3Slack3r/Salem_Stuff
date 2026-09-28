package haven.glsl;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class OrderList<E> extends AbstractCollection<E> {
   private final List<OrderList<E>.Element> bk = new ArrayList<>();
   private boolean sorted;

   public boolean add(E e, int o) {
      this.bk.add(new OrderList.Element(e, o));
      this.sorted = false;
      return true;
   }

   @Override
   public int size() {
      return this.bk.size();
   }

   @Override
   public Iterator<E> iterator() {
      if (!this.sorted) {
         Collections.sort(this.bk);
         this.sorted = true;
      }

      return new Iterator<E>() {
         private final Iterator<OrderList<E>.Element> bi = OrderList.this.bk.iterator();

         @Override
         public boolean hasNext() {
            return this.bi.hasNext();
         }

         @Override
         public E next() {
            return this.bi.next().e;
         }

         @Override
         public void remove() {
            this.bi.remove();
         }
      };
   }

   class Element implements Comparable<OrderList<E>.Element> {
      final E e;
      final int o;

      Element(E e, int o) {
         this.e = e;
         this.o = o;
      }

      public int compareTo(OrderList<E>.Element b) {
         return this.o - b.o;
      }
   }
}
