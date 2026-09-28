package haven;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class I2<T> implements Iterator<T> {
  private final Iterator<Iterator<T>> is;
  
  private Iterator<T> cur;
  
  private T co;
  
  private boolean hco;
  
  public I2(Iterator<T>... is) {
    this.is = Arrays.<Iterator<T>>asList(is).iterator();
    f();
  }
  
  public I2(Collection<Iterator<T>> is) {
    this.is = is.iterator();
    f();
  }
  
  private void f() {
    while (true) {
      if (this.cur != null && this.cur.hasNext()) {
        this.co = this.cur.next();
        this.hco = true;
        return;
      } 
      if (this.is.hasNext()) {
        this.cur = this.is.next();
        continue;
      } 
      break;
    } 
    this.hco = false;
  }
  
  public boolean hasNext() {
    return this.hco;
  }
  
  public T next() {
    if (!this.hco)
      throw new NoSuchElementException(); 
    T ret = this.co;
    f();
    return ret;
  }
  
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
