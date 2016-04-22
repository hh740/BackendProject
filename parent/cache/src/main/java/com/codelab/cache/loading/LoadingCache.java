package com.codelab.cache.loading;


import com.codelab.cache.Lifecycle;

public interface LoadingCache<K, V> extends Lifecycle /* extends Cache */ {

    public void load() throws Exception;

    V get(K key);

    void set(K key, V value);

    void set(K key, V value, int timeout);

    void delete(K key);
}
