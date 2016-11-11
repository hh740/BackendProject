package com.codelab.cache.loading;


import com.codelab.cache.Cache;

import java.util.List;
import java.util.Set;

public interface LoadingCache<K, V> extends Cache<K, V>, Loader<K, V> {

    public V get(K key);

    public List<V> gets(Set<K> keys);

    public void set(K key, V value);

    public void set(K key, V value, long timeout);

    public void delete(K key);

}
