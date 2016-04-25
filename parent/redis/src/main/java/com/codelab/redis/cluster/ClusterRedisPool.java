package com.codelab.redis.cluster;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/*
   cluster redis pool
 */

public class ClusterRedisPool implements RedisPool {

    private static Logger logger = LoggerFactory.getLogger(ClusterRedisPool.class);

    public static final String DEFAULT_REDIS_HOST = "localhost";

    public static final int DEFAULT_REDIS_PORT = 6379;

    public static final int DEFAULT_REDIS_TIMEOUT = 0;

    private RedisClusterClient redisClusterClient;

    public ClusterRedisPool(String hosts) {
        if (StringUtils.isBlank(hosts))
            throw new NullPointerException("host can't be null");

        Set<HostAndPort> nodes = new HashSet<HostAndPort>();
        for(String pair : hosts.split(",")){
            String[] s = pair.split(":");
            String ip = s[0];
            Integer port  = Integer.valueOf(s[1]);
            if (port <= 0)
                throw new IllegalArgumentException("port must greater than zero");
            nodes.add(new HostAndPort(ip,port));
        }
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(20);
        poolConfig.setMinIdle(5);
        poolConfig.setMaxTotal(1024);
        if(logger.isInfoEnabled()) logger.info("poolConfig minIdle:{}, maxIdle:{}, maxTotal:{}", poolConfig.getMinIdle(), poolConfig.getMaxIdle(), poolConfig.getMaxTotal());
        JedisCluster  jc = new JedisCluster(nodes,1000,5,poolConfig);
        redisClusterClient = new RedisClusterClient(jc);;
    }

    public ClusterRedisPool(String hosts, String password) {
        if (StringUtils.isBlank(hosts))
            throw new NullPointerException("host can't be null");

        Set<HostAndPort> nodes = new HashSet<HostAndPort>();
        for(String pair : hosts.split(",")){
            String[] s = pair.split(":");
            String ip = s[0];
            Integer port  = Integer.valueOf(s[1]);
            if (port <= 0)
                throw new IllegalArgumentException("port must greater than zero");
            nodes.add(new HostAndPort(ip,port));
        }
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(20);
        poolConfig.setMinIdle(5);
        poolConfig.setMaxTotal(1024);
        if(logger.isInfoEnabled()) logger.info("poolConfig minIdle:{}, maxIdle:{}, maxTotal:{}", poolConfig.getMinIdle(), poolConfig.getMaxIdle(), poolConfig.getMaxTotal());
        JedisCluster  jc = new JedisCluster(nodes,1000,5,poolConfig);
        redisClusterClient = new RedisClusterClient(jc);
    }

    @Override
    public RedisClient getRedisClient() {
        return redisClusterClient;
    }

    @Override
    public void release(RedisClient client) {

        RedisClusterClient rcc = (RedisClusterClient) client;
        try {
            rcc.close();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("true source failed ");
        }
    }
}
