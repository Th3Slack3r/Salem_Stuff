package haven;

import java.util.LinkedList;
import java.util.NoSuchElementException;

public class PrioQueue<E extends Prioritized> extends LinkedList<E> {
  public E peek() {
    Prioritized prioritized;
    E rv = null;
    int mp = 0;
    for (Prioritized prioritized1 : this) {
      int ep = prioritized1.priority();
      if (rv == null || ep > mp) {
        mp = ep;
        prioritized = prioritized1;
      } 
    } 
    return (E)prioritized;
  }
  
  public E element() {
    E rv;
    if ((rv = peek()) == null)
      throw new NoSuchElementException(); 
    return rv;
  }
  
  public E poll() {
    E rv = peek();
    remove(rv);
    return rv;
  }
  
  public E remove() {
    E rv;
    if ((rv = poll()) == null)
      throw new NoSuchElementException(); 
    return rv;
  }
}
