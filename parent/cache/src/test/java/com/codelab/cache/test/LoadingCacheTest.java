package com.codelab.cache.test;

import com.codelab.cache.loading.LoadingCache;
import com.codelab.cache.LoadingCacheImpl;
import com.codelab.cache.loading.Loader;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangke on 16/4/22.
 * this case show us how to use loading cache in our system
 */
public class LoadingCacheTest {

    private LoadingCache<String, Object> loadingCache = null;

    @Before
    public void loadData() {

        loadingCache = new LoadingCacheImpl("TEST_NAMESPACE", new Loader<String, Object>() {
            @Override
            public Map<String, Object> load() throws Exception {

                Map<String, Object> data = new HashMap<>();
                //get data from db or other saver
                data.put("test", new String("value"));
                return data;

            }
        });

    }

    @Ignore
    @Test
    public void getDataFromCache() throws InterruptedException {

        Thread.sleep(1200);
        Object obj = loadingCache.get("test");
        String str = (String) obj;
        System.out.println("str:" + str);

    }
}
