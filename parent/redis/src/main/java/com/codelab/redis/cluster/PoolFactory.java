package com.codelab.redis.cluster;

public interface PoolFactory {

    public RedisPool newRedisPool(String host, int port);

    public RedisPool getRedisPool(String name);

    public void destroy();
}
