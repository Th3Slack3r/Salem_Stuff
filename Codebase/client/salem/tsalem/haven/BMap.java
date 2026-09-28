package haven;

import java.util.Map;

public interface BMap<K, V> extends Map<K, V> {
  BMap<V, K> reverse();
}
