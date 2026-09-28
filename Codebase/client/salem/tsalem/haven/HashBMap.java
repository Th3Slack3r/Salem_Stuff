package haven;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class HashBMap<K, V> extends AbstractMap<K, V> implements BMap<K, V> {
  private final Map<K, V> fmap;
  
  private final Map<V, K> rmap;
  
  private final BMap<V, K> rev;
  
  private Set<Map.Entry<K, V>> entries;
  
  private HashBMap(Map<K, V> f, Map<V, K> r, BMap<V, K> rev) {
    this.entries = null;
    this.fmap = f;
    this.rmap = r;
    this.rev = rev;
  }
  
  public HashBMap() {
    this.entries = null;
    this.fmap = new HashMap<>();
    this.rmap = new HashMap<>();
    this.rev = new HashBMap(this.rmap, this.fmap, this);
  }
  
  public boolean containsKey(Object k) {
    return this.fmap.containsKey(k);
  }
  
  public Set<Map.Entry<K, V>> entrySet() {
    if (this.entries == null)
      this.entries = new AbstractSet<Map.Entry<K, V>>() {
          public int size() {
            return HashBMap.this.fmap.size();
          }
          
          public Iterator<Map.Entry<K, V>> iterator() {
            return new Iterator<Map.Entry<K, V>>() {
                private final Iterator<Map.Entry<K, V>> iter = HashBMap.this.fmap.entrySet().iterator();
                
                private Map.Entry<K, V> next;
                
                private Map.Entry<K, V> last;
                
                class IteredEntry<K, V> implements Map.Entry<K, V> {
                  private final K k;
                  
                  private final V v;
                  
                  public K getKey() {
                    return this.k;
                  }
                  
                  public V getValue() {
                    return this.v;
                  }
                  
                  public boolean equals(Object o) {
                    return (o instanceof IteredEntry && ((IteredEntry)o).k == this.k && ((IteredEntry)o).v == this.v);
                  }
                  
                  public int hashCode() {
                    return this.k.hashCode() ^ this.v.hashCode();
                  }
                  
                  public V setValue(V nv) {
                    throw new UnsupportedOperationException();
                  }
                }
                
                public boolean hasNext() {
                  if (this.next != null)
                    return true; 
                  if (!this.iter.hasNext())
                    return false; 
                  Map.Entry<K, V> e = this.iter.next();
                  this.next = new IteredEntry<>(e.getKey(), e.getValue());
                  return true;
                }
                
                public Map.Entry<K, V> next() {
                  if (!hasNext())
                    throw new NoSuchElementException(); 
                  Map.Entry<K, V> ret = this.last = this.next;
                  this.next = null;
                  return ret;
                }
                
                public void remove() {
                  this.iter.remove();
                  if (HashBMap.this.rmap.remove(this.last.getValue()) != this.last.getKey())
                    throw new ConcurrentModificationException("reverse-map invariant broken"); 
                }
              };
          }
          
          public void clear() {
            HashBMap.this.fmap.clear();
            HashBMap.this.rmap.clear();
          }
        }; 
    return this.entries;
  }
  
  public V get(Object k) {
    return this.fmap.get(k);
  }
  
  public V put(K k, V v) {
    if (k == null || v == null)
      throw new NullPointerException(); 
    V old = this.fmap.put(k, v);
    this.rmap.put(v, k);
    return old;
  }
  
  public V remove(Object k) {
    V old = this.fmap.remove(k);
    this.rmap.remove(old);
    return old;
  }
  
  public BMap<V, K> reverse() {
    return this.rev;
  }
}
