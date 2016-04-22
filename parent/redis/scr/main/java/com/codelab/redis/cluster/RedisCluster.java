package com.codelab.redis.cluster;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Redis;
import redis.clients.util.JedisClusterCRC16;

import java.util.Set;

/**
 * @author edwin
 * @since 02 十二月 2015
 */
public class RedisCluster extends JedisCluster {

    public RedisCluster(Set<HostAndPort> nodes) {
        super(nodes);
    }

    public RedisCluster(Set<HostAndPort> nodes, int timeout) {
        super(nodes, timeout);
    }

    public RedisCluster(Set<HostAndPort> nodes, int timeout, int maxRedirections) {
        super(nodes, timeout, maxRedirections);
    }

    public RedisCluster(Set<HostAndPort> nodes, GenericObjectPoolConfig poolConfig) {
        super(nodes, poolConfig);
    }

    public RedisCluster(Set<HostAndPort> nodes, int timeout, GenericObjectPoolConfig poolConfig) {
        super(nodes, timeout, poolConfig);
    }

    public RedisCluster(Set<HostAndPort> jedisClusterNode, int timeout, int maxRedirections, GenericObjectPoolConfig poolConfig) {
        super(jedisClusterNode, timeout, maxRedirections, poolConfig);
    }

    public RedisCluster(Set<HostAndPort> jedisClusterNode, int connectionTimeout, int soTimeout, int maxRedirections, GenericObjectPoolConfig poolConfig) {
        super(jedisClusterNode, connectionTimeout, soTimeout, maxRedirections, poolConfig);
    }
}
