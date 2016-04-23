package com.codelab.redis.cluster;

public interface RedisPool {

    RedisClient getRedisClient();

    void release(RedisClient client);
}
