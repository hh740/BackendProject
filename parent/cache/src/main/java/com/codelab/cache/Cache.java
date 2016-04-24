package com.codelab.cache;

import java.util.List;
import java.util.Set;

public interface Cache<K,V> extends Lifecycle {

    public V get(K key);

    public  List<V> gets(Set<K> keys);

    public  void set(K key, V value);

    public  void set(K key, V value, long timeout);

    public  void delete(K key);

    public void init() throws Exception;

    public void destroy() throws Exception;


}
