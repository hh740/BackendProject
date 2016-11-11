package com.codelab.cache.callable;

import com.codelab.cache.Cache;


public interface CallableCache<K,V,T> extends Cache<K,V> {

    public V get(K key, Callable<V,T> callable, int timeout);

    public V get(K key,Callable<V,T> callable);

    public T getResource();

    public void returnResource(T t);

}
