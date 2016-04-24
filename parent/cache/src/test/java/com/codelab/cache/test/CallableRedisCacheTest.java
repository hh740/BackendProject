package com.codelab.cache.test;

import com.codelab.cache.CallableCacheImpl;
import com.codelab.cache.RedisCacheImpl;
import com.codelab.cache.callable.Callable;
import com.codelab.cache.callable.CallableCache;
import com.codelab.redis.cluster.RedisClient;
import com.codelab.redis.cluster.RedisPoolFactory;
import com.codelab.redis.cluster.RedisStandaloneClient;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by wangke on 16/4/22.
 * this case show us how to use redis callable cache in our system
 * the jvm cache to be the level 1
 * the redis cache to be the level 2
 * the mysql cache to be the level 3
 * note: it must be have a tiny program to sync redis and mysql
 */
public class CallableRedisCacheTest {

    private CallableCache<String, Object,RedisClient> callableCache = null;



    @Before
    public void loadData() {

        callableCache = new RedisCacheImpl("TEST");

    }

    @Test
    public void getDataFromCache() throws InterruptedException {

        callableCache.get("test4", new Callable<Object, RedisClient>() {
            @Override
            public Object call(RedisClient client) throws Exception {
                RedisStandaloneClient rsc = (RedisStandaloneClient)client;
                String res=  rsc.get("test4");
                return res;
            }
        });

         String result = (String )callableCache.get("test4");

        System.out.print("======================================");

        System.out.print(result);

    }
}
