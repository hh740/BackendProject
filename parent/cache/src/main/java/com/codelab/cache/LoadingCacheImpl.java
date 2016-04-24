package com.codelab.cache;

import com.codelab.cache.loading.Loader;

/**
 * Created by wangke on 16/4/24.
 */
public class LoadingCacheImpl extends AbstructCache<String,Object,Void>{

    public LoadingCacheImpl(String namespace, Loader<String, Object> loader) {
        super(namespace, loader);
    }

    @Override
    public Void getResource() {
        return null;
    }

    @Override
    public void returnResource(Void Void) {

    }
}
