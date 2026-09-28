package haven;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class CacheMap<K, V> extends AbstractMap<K, V> {
  private final Map<K, Reference<V>> back;
  
  private final ReferenceQueue<V> cleanq = new ReferenceQueue<>();
  
  private final RefType reftype;
  
  private Set<Map.Entry<K, V>> entries;
  
  static class SRef<K, V> extends SoftReference<V> implements Ref<K> {
    final K key;
    
    SRef(K key, V val, ReferenceQueue<V> queue) {
      super(val, queue);
      this.key = key;
    }
    
    public K key() {
      return this.key;
    }
  }
  
  static class WRef<K, V> extends WeakReference<V> implements Ref<K> {
    final K key;
    
    WRef(K key, V val, ReferenceQueue<V> queue) {
      super(val, queue);
      this.key = key;
    }
    
    public K key() {
      return this.key;
    }
  }
  
  public enum RefType {
    SOFT {
      public <K, V> Reference<V> mkref(K k, V v, ReferenceQueue<V> cleanq) {
        return new CacheMap.SRef<>(k, v, cleanq);
      }
    },
    WEAK {
      public <K, V> Reference<V> mkref(K k, V v, ReferenceQueue<V> cleanq) {
        return new CacheMap.WRef<>(k, v, cleanq);
      }
    };
    
    public abstract <K, V> Reference<V> mkref(K param1K, V param1V, ReferenceQueue<V> param1ReferenceQueue);
  }
  
  public CacheMap() {
    this(RefType.SOFT);
  }
  
  public CacheMap(Map<K, V> m) {
    this();
    putAll(m);
  }
  
  public boolean containsKey(Object k) {
    return (get(k) != null);
  }
  
  private class IteredEntry implements Map.Entry<K, V> {
    private final K k;
    
    private V v;
    
    private IteredEntry(K k, V v) {
      this.k = k;
      this.v = v;
    }
    
    public K getKey() {
      return this.k;
    }
    
    public V getValue() {
      return this.v;
    }
    
    public boolean equals(Object o) {
      return (o instanceof IteredEntry && ((IteredEntry)o).k == this.k);
    }
    
    public int hashCode() {
      return this.k.hashCode();
    }
    
    public V setValue(V nv) {
      return CacheMap.this.put(this.k, this.v = nv);
    }
  }
  
  public CacheMap(RefType type) {
    this.entries = null;
    this.reftype = type;
    this.back = new HashMap<>();
  }
  
  public Set<Map.Entry<K, V>> entrySet() {
    if (this.entries == null)
      this.entries = new AbstractSet<Map.Entry<K, V>>() {
          public int size() {
            CacheMap.this.clean();
            return CacheMap.this.back.size();
          }
          
          public Iterator<Map.Entry<K, V>> iterator() {
            CacheMap.this.clean();
            final Iterator<Map.Entry<K, Reference<V>>> iter = CacheMap.this.back.entrySet().iterator();
            return new Iterator<Map.Entry<K, V>>() {
                private K nk;
                
                private V nv;
                
                public boolean hasNext() {
                  while (true) {
                    if (this.nv != null)
                      return true; 
                    if (!iter.hasNext())
                      return false; 
                    Map.Entry<K, Reference<V>> e = iter.next();
                    K k = e.getKey();
                    V v = ((Reference<V>)e.getValue()).get();
                    if (v != null) {
                      this.nk = k;
                      this.nv = v;
                      return true;
                    } 
                  } 
                }
                
                public Map.Entry<K, V> next() {
                  if (!hasNext())
                    throw new NoSuchElementException(); 
                  Map.Entry<K, V> ret = new CacheMap.IteredEntry(this.nk, this.nv);
                  this.nk = null;
                  this.nv = null;
                  return ret;
                }
                
                public void remove() {
                  iter.remove();
                }
              };
          }
          
          public void clear() {
            CacheMap.this.back.clear();
          }
        }; 
    return this.entries;
  }
  
  private void clean() {
    Reference<? extends V> ref;
    while ((ref = this.cleanq.poll()) != null) {
      Ref rr = (Ref)ref;
      remove(rr.key());
    } 
  }
  
  public V get(Object k) {
    clean();
    Reference<V> ref = this.back.get(k);
    return (ref == null) ? null : ref.get();
  }
  
  public V put(K k, V v) {
    clean();
    Reference<V> old = this.back.put(k, this.reftype.mkref(k, v, this.cleanq));
    return (old == null) ? null : old.get();
  }
  
  public V remove(Object k) {
    clean();
    Reference<V> ref = this.back.remove(k);
    return (ref == null) ? null : ref.get();
  }
  
  static interface Ref<K> {
    K key();
  }
}
