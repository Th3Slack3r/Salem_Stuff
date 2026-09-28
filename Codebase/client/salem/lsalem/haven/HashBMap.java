package haven;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;

public class HashBMap<K, V> extends AbstractMap<K, V> implements BMap<K, V> {
   private final Map<K, V> fmap;
   private final Map<V, K> rmap;
   private final BMap<V, K> rev;
   private Set<Entry<K, V>> entries = null;

   private HashBMap(Map<K, V> f, Map<V, K> r, BMap<V, K> rev) {
      this.fmap = f;
      this.rmap = r;
      this.rev = rev;
   }

   public HashBMap() {
      this.fmap = new HashMap<>();
      this.rmap = new HashMap<>();
      this.rev = new HashBMap<>(this.rmap, this.fmap, this);
   }

   @Override
   public boolean containsKey(Object k) {
      return this.fmap.containsKey(k);
   }

   @Override
   public Set<Entry<K, V>> entrySet() {
      if (this.entries == null) {
         this.entries = new AbstractSet<Entry<K, V>>() {
            @Override
            public int size() {
               return HashBMap.this.fmap.size();
            }

            @Override
            public Iterator<Entry<K, V>> iterator() {
               return new Iterator<Entry<K, V>>() {
                  private final Iterator<Entry<K, V>> iter = HashBMap.this.fmap.entrySet().iterator();
                  private Entry<K, V> next;
                  private Entry<K, V> last;

                  @Override
                  public boolean hasNext() {
                     if (this.next != null) {
                        return true;
                     } else if (!this.iter.hasNext()) {
                        return false;
                     } else {
                        Entry<K, V> e = this.iter.next();
                        this.next = new IteredEntry<>(e.getKey(), e.getValue());
                        return true;
                     }
                  }

                  public Entry<K, V> next() {
                     if (!this.hasNext()) {
                        throw new NoSuchElementException();
                     } else {
                        Entry<K, V> ret = this.last = this.next;
                        this.next = null;
                        return ret;
                     }
                  }

                  @Override
                  public void remove() {
                     this.iter.remove();
                     if (HashBMap.this.rmap.remove(this.last.getValue()) != this.last.getKey()) {
                        throw new ConcurrentModificationException("reverse-map invariant broken");
                     }
                  }

                  class IteredEntry<K, V> implements Entry<K, V> {
                     private final K k;
                     private final V v;

                     IteredEntry(K k, V v) {
                        this.k = k;
                        this.v = v;
                     }

                     @Override
                     public K getKey() {
                        return this.k;
                     }

                     @Override
                     public V getValue() {
                        return this.v;
                     }

                     @Override
                     public boolean equals(Object o) {
                        return o instanceof IteredEntry && ((IteredEntry)o).k == this.k && ((IteredEntry)o).v == this.v;
                     }

                     @Override
                     public int hashCode() {
                        return this.k.hashCode() ^ this.v.hashCode();
                     }

                     @Override
                     public V setValue(V nv) {
                        throw new UnsupportedOperationException();
                     }
                  }
               };
            }

            @Override
            public void clear() {
               HashBMap.this.fmap.clear();
               HashBMap.this.rmap.clear();
            }
         };
      }

      return this.entries;
   }

   @Override
   public V get(Object k) {
      return this.fmap.get(k);
   }

   @Override
   public V put(K k, V v) {
      if (k != null && v != null) {
         V old = this.fmap.put(k, v);
         this.rmap.put(v, k);
         return old;
      } else {
         throw new NullPointerException();
      }
   }

   @Override
   public V remove(Object k) {
      V old = this.fmap.remove(k);
      this.rmap.remove(old);
      return old;
   }

   @Override
   public BMap<V, K> reverse() {
      return this.rev;
   }
}
