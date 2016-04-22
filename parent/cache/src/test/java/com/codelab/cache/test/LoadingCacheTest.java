package com.codelab.cache.test;

import com.codelab.cache.Loader;
import com.codelab.cache.loading.LoadingCache;
import com.codelab.cache.loading.LocalLoadingCache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by wangke on 16/4/22.
 * this case show us how to use loading cache in our system
 */
public class LoadingCacheTest {

    private LoadingCache<String, Object> loadingCache = null;

    @Before
    public void loadData() {

        loadingCache = new LocalLoadingCache<>("TEST_NAMESPACE", new Loader<String, Object>() {
            @Override
            public Map<String, Object> load() throws Exception {

                Map<String, Object> data = new HashMap<>();
                //get data from db or other saver
                data.put("test", new String("value"));
                return data;

            }
        });

    }

    @Test
    public void getDataFromCache() {

        Object obj = loadingCache.get("test");
        String str = (String) obj;
        System.out.println("str:" + str);

    }
}
