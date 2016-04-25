package com.codelab.cache;

import com.codelab.redis.cluster.RedisClient;
import com.codelab.redis.cluster.RedisPool;
import com.codelab.redis.cluster.RedisPoolFactory;
import com.codelab.redis.cluster.RedisStandaloneClient;

/**
 * Created by wangke on 16/4/24.
 */
public class RedisCacheImpl extends AbstructCache<String,Object,RedisClient>{


    private RedisPool pool;

    public RedisCacheImpl(String namespace) {
        super(namespace);
        pool  = RedisPoolFactory.getInstance().getRedisPool(namespace);
    }

    @Override
    public RedisClient getResource() {
        return pool.getRedisClient();
    }

    @Override
    public void returnResource(RedisClient client) {
        pool.release(client);
    }

}
