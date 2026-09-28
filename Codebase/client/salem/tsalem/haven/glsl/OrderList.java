package haven.glsl;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class OrderList<E> extends AbstractCollection<E> {
  private final List<Element> bk = new ArrayList<>();
  
  private boolean sorted;
  
  class Element implements Comparable<Element> {
    final E e;
    
    final int o;
    
    Element(E e, int o) {
      this.e = e;
      this.o = o;
    }
    
    public int compareTo(Element b) {
      return this.o - b.o;
    }
  }
  
  public boolean add(E e, int o) {
    this.bk.add(new Element(e, o));
    this.sorted = false;
    return true;
  }
  
  public int size() {
    return this.bk.size();
  }
  
  public Iterator<E> iterator() {
    if (!this.sorted) {
      Collections.sort(this.bk);
      this.sorted = true;
    } 
    return new Iterator<E>() {
        private final Iterator<OrderList<E>.Element> bi = OrderList.access$000(OrderList.this).iterator();
        
        public boolean hasNext() {
          return this.bi.hasNext();
        }
        
        public E next() {
          return ((OrderList.Element)this.bi.next()).e;
        }
        
        public void remove() {
          this.bi.remove();
        }
      };
  }
}
