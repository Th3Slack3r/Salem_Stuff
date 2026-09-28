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
import java.util.Map.Entry;

public class CacheMap<K, V> extends AbstractMap<K, V> {
   private final Map<K, Reference<V>> back;
   private final ReferenceQueue<V> cleanq = new ReferenceQueue<>();
   private final CacheMap.RefType reftype;
   private Set<Entry<K, V>> entries = null;

   public CacheMap(CacheMap.RefType type) {
      this.reftype = type;
      this.back = new HashMap<>();
   }

   public CacheMap() {
      this(CacheMap.RefType.SOFT);
   }

   public CacheMap(Map<K, V> m) {
      this();
      this.putAll(m);
   }

   @Override
   public boolean containsKey(Object k) {
      return this.get(k) != null;
   }

   @Override
   public Set<Entry<K, V>> entrySet() {
      if (this.entries == null) {
         this.entries = new AbstractSet<Entry<K, V>>() {
            @Override
            public int size() {
               CacheMap.this.clean();
               return CacheMap.this.back.size();
            }

            @Override
            public Iterator<Entry<K, V>> iterator() {
               CacheMap.this.clean();
               final Iterator<Entry<K, Reference<V>>> iter = CacheMap.this.back.entrySet().iterator();
               return new Iterator<Entry<K, V>>() {
                  private K nk;
                  private V nv;

                  @Override
                  public boolean hasNext() {
                     while (this.nv == null) {
                        if (!iter.hasNext()) {
                           return false;
                        }

                        Entry<K, Reference<V>> e = iter.next();
                        K k = e.getKey();
                        V v = e.getValue().get();
                        if (v != null) {
                           this.nk = k;
                           this.nv = v;
                           return true;
                        }
                     }

                     return true;
                  }

                  public Entry<K, V> next() {
                     if (!this.hasNext()) {
                        throw new NoSuchElementException();
                     } else {
                        Entry<K, V> ret = CacheMap.this.new IteredEntry(this.nk, this.nv);
                        this.nk = null;
                        this.nv = null;
                        return ret;
                     }
                  }

                  @Override
                  public void remove() {
                     iter.remove();
                  }
               };
            }

            @Override
            public void clear() {
               CacheMap.this.back.clear();
            }
         };
      }

      return this.entries;
   }

   private void clean() {
      Reference<? extends V> ref;
      while ((ref = this.cleanq.poll()) != null) {
         CacheMap.Ref rr = (CacheMap.Ref)ref;
         this.remove(rr.key());
      }
   }

   @Override
   public V get(Object k) {
      this.clean();
      Reference<V> ref = this.back.get(k);
      return ref == null ? null : ref.get();
   }

   @Override
   public V put(K k, V v) {
      this.clean();
      Reference<V> old = this.back.put(k, this.reftype.mkref(k, v, this.cleanq));
      return old == null ? null : old.get();
   }

   @Override
   public V remove(Object k) {
      this.clean();
      Reference<V> ref = this.back.remove(k);
      return ref == null ? null : ref.get();
   }

   private class IteredEntry implements Entry<K, V> {
      private final K k;
      private V v;

      private IteredEntry(K k, V v) {
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
         return o instanceof CacheMap.IteredEntry && ((CacheMap.IteredEntry)o).k == this.k;
      }

      @Override
      public int hashCode() {
         return this.k.hashCode();
      }

      @Override
      public V setValue(V nv) {
         return CacheMap.this.put(this.k, this.v = nv);
      }
   }

   interface Ref<K> {
      K key();
   }

   public static enum RefType {
      SOFT {
         @Override
         public <K, V> Reference<V> mkref(K k, V v, ReferenceQueue<V> cleanq) {
            return new CacheMap.SRef<>(k, v, cleanq);
         }
      },
      WEAK {
         @Override
         public <K, V> Reference<V> mkref(K k, V v, ReferenceQueue<V> cleanq) {
            return new CacheMap.WRef<>(k, v, cleanq);
         }
      };

      private RefType() {
      }

      public abstract <K, V> Reference<V> mkref(K var1, V var2, ReferenceQueue<V> var3);
   }

   static class SRef<K, V> extends SoftReference<V> implements CacheMap.Ref<K> {
      final K key;

      SRef(K key, V val, ReferenceQueue<V> queue) {
         super(val, queue);
         this.key = key;
      }

      @Override
      public K key() {
         return this.key;
      }
   }

   static class WRef<K, V> extends WeakReference<V> implements CacheMap.Ref<K> {
      final K key;

      WRef(K key, V val, ReferenceQueue<V> queue) {
         super(val, queue);
         this.key = key;
      }

      @Override
      public K key() {
         return this.key;
      }
   }
}
