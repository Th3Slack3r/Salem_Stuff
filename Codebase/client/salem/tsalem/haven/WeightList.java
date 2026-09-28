package haven;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WeightList<T> implements Serializable {
  List<T> c;
  
  List<Integer> w;
  
  int tw = 0;
  
  public WeightList() {
    this.c = new ArrayList<>();
    this.w = new ArrayList<>();
  }
  
  public void add(T c, int w) {
    this.c.add(c);
    this.w.add(Integer.valueOf(w));
    this.tw += w;
  }
  
  public T pick(int p) {
    p %= this.tw;
    int i = 0;
    while ((p -= ((Integer)this.w.get(i)).intValue()) >= 0)
      i++; 
    return this.c.get(i);
  }
  
  public T pick(Random gen) {
    return pick(gen.nextInt(this.tw));
  }
  
  public int size() {
    return this.c.size();
  }
}
