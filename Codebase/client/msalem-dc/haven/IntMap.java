package haven;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;

public class IntMap<V> extends AbstractMap<Integer, V> {
   private static final Object nil = new Object();
   private Object[] vals;
   private int sz;
   private Set<Entry<Integer, V>> entries = null;

   public IntMap(int capacity) {
      this.vals = new Object[capacity];
   }

   public IntMap() {
      this(0);
   }

   public IntMap(Map<Integer, V> m) {
      this();
      this.putAll(m);
   }

   private Object icast(V v) {
      return v == null ? nil : v;
   }

   private V ocast(Object v) {
      return (V)(v == nil ? null : v);
   }

   public boolean containsKey(int k) {
      return this.vals.length > k && this.vals[k] != null;
   }

   public boolean containsKey(Integer k) {
      return this.containsKey(k.intValue());
   }

   @Override
   public Set<Entry<Integer, V>> entrySet() {
      if (this.entries == null) {
         this.entries = new AbstractSet<Entry<Integer, V>>() {
            @Override
            public int size() {
               return IntMap.this.sz;
            }

            @Override
            public Iterator<Entry<Integer, V>> iterator() {
               return new Iterator<Entry<Integer, V>>() {
                  private int ni = -1;
                  private int li = -1;

                  @Override
                  public boolean hasNext() {
                     if (this.ni < 0) {
                        this.ni = this.li + 1;

                        while (this.ni < IntMap.this.vals.length && IntMap.this.vals[this.ni] == null) {
                           this.ni++;
                        }
                     }

                     return this.ni < IntMap.this.vals.length;
                  }

                  public Entry<Integer, V> next() {
                     if (!this.hasNext()) {
                        throw new NoSuchElementException();
                     } else {
                        Entry<Integer, V> ret = IntMap.this.new IteredEntry(this.ni);
                        this.li = this.ni;
                        this.ni = -1;
                        return ret;
                     }
                  }

                  @Override
                  public void remove() {
                     IntMap.this.vals[this.li] = null;
                  }
               };
            }

            @Override
            public void clear() {
               IntMap.this.vals = new Object[0];
            }
         };
      }

      return this.entries;
   }

   public V get(int k) {
      return this.vals.length <= k ? null : this.ocast(this.vals[k]);
   }

   public V get(Integer k) {
      return this.get(k.intValue());
   }

   public V put(int k, V v) {
      if (this.vals.length <= k) {
         Object[] n = new Object[k + 1];
         System.arraycopy(this.vals, 0, n, 0, this.vals.length);
         this.vals = n;
      }

      V ret = this.ocast(this.vals[k]);
      this.vals[k] = this.icast(v);
      return ret;
   }

   public V put(Integer k, V v) {
      return this.put(k.intValue(), v);
   }

   public V remove(int k) {
      if (k >= this.vals.length) {
         return null;
      } else {
         V ret = this.ocast(this.vals[k]);
         this.vals[k] = null;
         return ret;
      }
   }

   public V remove(Integer k) {
      return this.remove(k.intValue());
   }

   private class IteredEntry implements Entry<Integer, V> {
      private final int k;

      private IteredEntry(int k) {
         this.k = k;
      }

      public Integer getKey() {
         return this.k;
      }

      @Override
      public V getValue() {
         return IntMap.this.get(this.k);
      }

      @Override
      public boolean equals(Object o) {
         return o instanceof IntMap.IteredEntry && ((IntMap.IteredEntry)o).k == this.k;
      }

      @Override
      public int hashCode() {
         return this.k;
      }

      @Override
      public V setValue(V nv) {
         return IntMap.this.put(this.k, nv);
      }
   }
}
