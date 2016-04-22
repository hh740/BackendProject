package com.codelab.cache;

import java.util.Map;


public interface Loader<K,V> {

    public Map<K, V> load() throws Exception;
}
