package com.codelab.cache;

import java.util.concurrent.Callable;

/**
 * @author edwin
 * @since 17 Jan 2015
 */
public class LocalCache /* implements CallableCache<String, Object> */ {

    private NameSpace namespace;

    private LocalCachePool localCachePool;

    LocalCache(NameSpace namespace, LocalCachePool localCachePool) {
        this.namespace = namespace;
        this.localCachePool = localCachePool;
    }

//    @Override
    public Object get(String key) {
        return localCachePool.get(localCachePool.getFinalKey(namespace, key));
    }


//    @Override
    public void set(String key, Object value) {
        localCachePool.put(localCachePool.getFinalKey(namespace, key), value);
    }



    public <V> V get(String key, Callable<V> callable) throws Exception {
        return localCachePool.get(localCachePool.getFinalKey(namespace, key), callable);
    }


    public NameSpace getNamespace() {
        return namespace;
    }

    public void setNamespace(NameSpace namespace) {
        this.namespace = namespace;
    }

}
