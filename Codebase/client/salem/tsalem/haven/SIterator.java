package haven;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class SIterator<T> implements Iterator<T> {
  private int st = 0;
  
  private T n;
  
  public abstract T snext() throws NoSuchElementException;
  
  private void ref() {
    if (this.st == 0)
      try {
        this.n = snext();
        this.st = 1;
      } catch (NoSuchElementException e) {
        this.st = 2;
      }  
  }
  
  public boolean hasNext() {
    ref();
    return (this.st == 1);
  }
  
  public T next() {
    ref();
    if (this.st == 2)
      throw new NoSuchElementException(); 
    this.st = 0;
    return this.n;
  }
  
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
