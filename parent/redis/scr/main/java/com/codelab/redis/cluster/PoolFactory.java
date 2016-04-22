package com.codelab.redis.cluster;

public interface PoolFactory {

    public RedisPool newCachePool(String host, int port);

    public RedisPool getCachePool(String name);

    public void destroy();
}
