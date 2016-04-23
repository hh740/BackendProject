package com.codelab.redis.cluster;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

/*
 *  standalone RedisPool
 *
 */

public class DefalutRedisPool implements  RedisPool{

    private static Logger logger = LoggerFactory.getLogger(DefalutRedisPool.class);

    public static final String DEFAULT_REDIS_HOST = "localhost";

    public static final int DEFAULT_REDIS_PORT = 6379;

    public static final int DEFAULT_REDIS_TIMEOUT = 0;

    private JedisPool pool;

    public DefalutRedisPool(String address) {
        if (StringUtils.isBlank(address))
            throw new NullPointerException("address can't be null");
        String[] splitAddress = address.split("\\:");
        String host = splitAddress[0];
        Integer port = Integer.valueOf(splitAddress[1]);
        if (port <= 0)
            throw new IllegalArgumentException("port must greater than zero");
        pool = new JedisPool(host, port);
        if (logger.isDebugEnabled()) logger.debug("JedisPool[{},{}] configured successfully", address, port);
    }

    public DefalutRedisPool(String address, String password) {

        if (StringUtils.isBlank(address))
            throw new NullPointerException("address can't be null");
        String[] splitAddress = address.split("\\:");
        String host = splitAddress[0];
        Integer port = Integer.valueOf(splitAddress[1]);
        if (port <= 0)
            throw new IllegalArgumentException("port must greater than zero");
        pool = new JedisPool(new GenericObjectPoolConfig(), host, port, DEFAULT_REDIS_TIMEOUT, password);
        if (logger.isDebugEnabled()) logger.debug("JedisPool[{},{}] configured successfully", address, port);

    }

    public RedisStandaloneClient getRedisClient() {
        return new RedisStandaloneClient(pool.getResource());
    }

    public void release(RedisClient client ) {
        RedisStandaloneClient rsc = (RedisStandaloneClient) client;
        pool.returnResource(rsc.getDelegate());
    }

}
