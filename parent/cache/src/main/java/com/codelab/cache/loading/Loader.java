package com.codelab.cache.loading;

import java.util.Map;


public interface Loader<K,V> {

    public Map<K, V> load() throws Exception;
}
