package com.codelab.redis.cluster;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

/*
    默认的单机RedisPool
 */

public class DefalutRedisPool {

    private static Logger logger = LoggerFactory.getLogger(DefalutRedisPool.class);

    public static final String DEFAULT_REDIS_HOST = "localhost";

    public static final int DEFAULT_REDIS_PORT = 6379;

    public static final int DEFAULT_REDIS_TIMEOUT = 0;

    private JedisPool pool;

    public DefalutRedisPool(String host, int port) {
        if (StringUtils.isBlank(host))
            throw new NullPointerException("host can't be null");
        if (port <= 0)
            throw new IllegalArgumentException("port must greater than zero");
        pool = new JedisPool(host, port);
        if (logger.isDebugEnabled()) logger.debug("JedisPool[{},{}] configured successfully", host, port);
    }

    public DefalutRedisPool(String host, int port, String password) {
        if (StringUtils.isBlank(host))
            throw new NullPointerException("host can't be null");
        if (port <= 0)
            throw new IllegalArgumentException("port must greater than zero");
        pool = new JedisPool(new GenericObjectPoolConfig(), host, port, DEFAULT_REDIS_TIMEOUT, password);
        if (logger.isDebugEnabled())
            logger.debug("JedisPool[{},{}] configured successfully", host, port);
    }

    RedisClient getRedisClient() {
        return new RedisClient(pool.getResource());
    }

    void release(RedisClient client) {
        pool.returnResource(client.getDelegate());
    }
}