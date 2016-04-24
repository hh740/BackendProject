package com.codelab.redis.cluster;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.util.JedisClusterCRC16;

import java.util.Set;


public class RedisClusterClient extends JedisCluster implements RedisClient{

    public RedisClusterClient(Set<HostAndPort> nodes) {
        super(nodes);
    }

    public RedisClusterClient(Set<HostAndPort> nodes, int timeout) {
        super(nodes, timeout);
    }

    public RedisClusterClient(Set<HostAndPort> nodes, int timeout, int maxRedirections) {
        super(nodes, timeout, maxRedirections);
    }

    public RedisClusterClient(Set<HostAndPort> nodes, GenericObjectPoolConfig poolConfig) {
        super(nodes, poolConfig);
    }

    public RedisClusterClient(Set<HostAndPort> nodes, int timeout, GenericObjectPoolConfig poolConfig) {
        super(nodes, timeout, poolConfig);
    }

    public RedisClusterClient(Set<HostAndPort> jedisClusterNode, int timeout, int maxRedirections, GenericObjectPoolConfig poolConfig) {
        super(jedisClusterNode, timeout, maxRedirections, poolConfig);
    }

    public RedisClusterClient(Set<HostAndPort> jedisClusterNode, int connectionTimeout, int soTimeout, int maxRedirections, GenericObjectPoolConfig poolConfig) {
        super(jedisClusterNode, connectionTimeout, soTimeout, maxRedirections, poolConfig);
    }
}
