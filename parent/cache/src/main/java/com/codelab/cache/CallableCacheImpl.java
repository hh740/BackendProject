package com.codelab.cache;

/**
 * Created by wangke on 16/4/24.
 */
public class CallableCacheImpl extends AbstructCache<String,Object,Void>{

    public CallableCacheImpl(String namespace) {
        super(namespace);
    }

    @Override
    public Void getResource() {
        return null;
    }

    @Override
    public void returnResource(Void Void) {

    }
}
