package com.codelab.cache.test;

import com.codelab.cache.CallableCacheImpl;
import com.codelab.cache.LoadingCacheImpl;
import com.codelab.cache.callable.Callable;
import com.codelab.cache.callable.CallableCache;
import com.codelab.cache.loading.Loader;
import com.codelab.cache.loading.LoadingCache;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by wangke on 16/4/22.
 * this case show us how to use callable cache in our system
 */
public class CallableCacheTest {

    private CallableCache<String, Object,Void> callableCache = null;

    @Before
    public void loadData() {

        callableCache = new CallableCacheImpl("TEST");

    }

    @Test
    public void getDataFromCache() throws InterruptedException {


        callableCache.get("test3", new Callable<Object, Void>() {
            @Override
            public Object call(Void Void) throws Exception {

                return new String("value3");

            }
        });

        String value  = (String)callableCache.get("test3");

        System.out.println(value);



    }
}
