package haven;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class IntMap<V> extends AbstractMap<Integer, V> {
  private static final Object nil = new Object();
  
  private Object[] vals;
  
  private int sz;
  
  private Set<Map.Entry<Integer, V>> entries;
  
  public IntMap(int capacity) {
    this.entries = null;
    this.vals = new Object[capacity];
  }
  
  public IntMap() {
    this(0);
  }
  
  public IntMap(Map<Integer, V> m) {
    this();
    putAll(m);
  }
  
  private Object icast(V v) {
    return (v == null) ? nil : v;
  }
  
  private V ocast(Object v) {
    return (v == nil) ? null : (V)v;
  }
  
  public boolean containsKey(int k) {
    return (this.vals.length > k && this.vals[k] != null);
  }
  
  public boolean containsKey(Integer k) {
    return containsKey(k.intValue());
  }
  
  private class IteredEntry implements Map.Entry<Integer, V> {
    private final int k;
    
    private IteredEntry(int k) {
      this.k = k;
    }
    
    public Integer getKey() {
      return Integer.valueOf(this.k);
    }
    
    public V getValue() {
      return IntMap.this.get(this.k);
    }
    
    public boolean equals(Object o) {
      return (o instanceof IteredEntry && ((IteredEntry)o).k == this.k);
    }
    
    public int hashCode() {
      return this.k;
    }
    
    public V setValue(V nv) {
      return IntMap.this.put(this.k, nv);
    }
  }
  
  public Set<Map.Entry<Integer, V>> entrySet() {
    if (this.entries == null)
      this.entries = new AbstractSet<Map.Entry<Integer, V>>() {
          public int size() {
            return IntMap.this.sz;
          }
          
          public Iterator<Map.Entry<Integer, V>> iterator() {
            return new Iterator<Map.Entry<Integer, V>>() {
                private int ni = -1;
                
                private int li = -1;
                
                public boolean hasNext() {
                  if (this.ni < 0)
                    for (this.ni = this.li + 1; this.ni < IntMap.this.vals.length && 
                      IntMap.this.vals[this.ni] == null; this.ni++); 
                  return (this.ni < IntMap.this.vals.length);
                }
                
                public Map.Entry<Integer, V> next() {
                  if (!hasNext())
                    throw new NoSuchElementException(); 
                  Map.Entry<Integer, V> ret = new IntMap.IteredEntry(this.ni);
                  this.li = this.ni;
                  this.ni = -1;
                  return ret;
                }
                
                public void remove() {
                  IntMap.this.vals[this.li] = null;
                }
              };
          }
          
          public void clear() {
            IntMap.this.vals = new Object[0];
          }
        }; 
    return this.entries;
  }
  
  public V get(int k) {
    if (this.vals.length <= k)
      return null; 
    return ocast(this.vals[k]);
  }
  
  public V get(Integer k) {
    return get(k.intValue());
  }
  
  public V put(int k, V v) {
    if (this.vals.length <= k) {
      Object[] n = new Object[k + 1];
      System.arraycopy(this.vals, 0, n, 0, this.vals.length);
      this.vals = n;
    } 
    V ret = ocast(this.vals[k]);
    this.vals[k] = icast(v);
    return ret;
  }
  
  public V put(Integer k, V v) {
    return put(k.intValue(), v);
  }
  
  public V remove(int k) {
    if (k >= this.vals.length)
      return null; 
    V ret = ocast(this.vals[k]);
    this.vals[k] = null;
    return ret;
  }
  
  public V remove(Integer k) {
    return remove(k.intValue());
  }
}
