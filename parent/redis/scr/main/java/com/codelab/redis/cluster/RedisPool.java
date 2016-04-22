package com.codelab.redis.cluster;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

public interface RedisPool {

    RedisClient getRedisClient();

    void release(RedisClient client);
}
