package com.codelab.redis.cluster;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;


public class RedisClusterPool implements RedisPool {

    private static Logger logger = LoggerFactory.getLogger(RedisClusterPool.class);

    private RedisCluster redisCluster;

    private JedisPool pool;

    private Properties properties = new Properties();

    public RedisClusterPool(String host, int port) {

        InputStream in = getClass().getClassLoader().getResourceAsStream("redis.cluster.properties");
        properties.load(in);
        String line = properties.getProperty("redis.cluster");
        if (logger.isInfoEnabled()) logger.info("redis.cluster:{}", line);
        Assert.assertNotNull(line);
        Set<HostAndPort> nodes = new HashSet<HostAndPort>();
        for (String pair : line.split(",")) {
            String[] s = pair.split(":");
            nodes.add(new HostAndPort(s[0], Integer.valueOf(s[1])));
        }
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(20);
        poolConfig.setMinIdle(5);
        poolConfig.setMaxTotal(1024);
        if (logger.isInfoEnabled())
            logger.info("poolConfig minIdle:{}, maxIdle:{}, maxTotal:{}", poolConfig.getMinIdle(), poolConfig.getMaxIdle(), poolConfig.getMaxTotal());
        redisCluster = new RedisCluster(nodes, 1000, 5, poolConfig);
    }

    RedisClusterPool(String host, int port, String password) {

    }


    @Override
    public RedisClient getRedisClient() {
        //随机返回一个cluster的客户端
        try {
            pool = redisCluster.getClusterNodes().get(0);
        }catch(Exception e){


        }
        return new RedisClient(pool.getResource());
    }

    @Override
    public void release(RedisClient client) {
        pool.returnResource(client.getDelegate());
    }
}
