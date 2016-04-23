package com.codelab.redis.cluster;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class RedisPoolFactory implements PoolFactory {

    private static Logger logger = LoggerFactory.getLogger(RedisPoolFactory.class);

    private static class StaticNestedClass {
        public static RedisPoolFactory instance = new RedisPoolFactory();
    }

    public static final String CONFIG_FILE = "redis.properties";

    private String configFile = CONFIG_FILE;

    private Properties properties = new Properties();

    private ConcurrentHashMap<String, RedisPool> POOL = new ConcurrentHashMap<String, RedisPool>();

    private RedisPoolFactory() {
        logger.debug("get config input stream !");
        InputStream in = getClass().getClassLoader().getResourceAsStream(getConfigFile());

        try {
            properties.load(in);
        } catch (IOException e) {
            logger.error("Load {} failed", getConfigFile());
            throw new RuntimeException(e);
        }
        createCachePools(properties);
        if (logger.isInfoEnabled())
            logger.info("Create redisCachePoolFactory successfully, total pool:{}", POOL.size());
    }

    public static RedisPoolFactory getInstance() {
        return StaticNestedClass.instance;
    }

    @Override
    public RedisPool newRedisPool(String host, int port) {

        throw new RuntimeException("Unsupported operation now");
    }

    @Override
    public RedisPool getRedisPool(String name) {
        if (!POOL.containsKey(name))
            throw new NullPointerException();
        return POOL.get(name);
    }

    @Override
    public void destroy() {
        if (logger.isInfoEnabled())
            logger.info("Destroy redisCachePoolFactory started");
        for (RedisPool pool : POOL.values()) {
            // TODO
            // pool.destroy();
        }
        if (logger.isInfoEnabled())
            logger.info("Destroy redisCachePoolFactory successfully");
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    private void createCachePools(Properties properties) {
        if (properties.isEmpty())
            return;
        Set<String> keys = properties.stringPropertyNames();
        for (String key : keys)
            logger.debug("key:{},value:{}", key, properties.getProperty(key));
        for (String key : keys) {
            if (StringUtils.isBlank(key))
                errorConfig();
            if (key.endsWith("host")) {
                String address = properties.getProperty(key);
                //judge the cluster type,standalone or cluster
                RedisPool redisPool = null;
                String[] s = key.split("\\.");
                if (s == null || s.length < 3)
                    errorConfig();
                String name = s[1];
                String kpass = s[0] + "." + name + ".pass";
                logger.debug("kpass:{}", kpass);
                if (!properties.containsKey(kpass))
                    errorConfig();
                String pass = properties.getProperty(kpass);

                if (Pattern.matches("((\\d+\\.){3}\\d+:\\d+,)+((\\d+\\.){3}\\d+:\\d+)", address)) {
                    //cluster mode
                    redisPool = new ClusterRedisPool(address,pass);
                }
                else if (Pattern.matches("(\\d+\\.){3}\\d+:\\d+", address)) {
                    //standalone mode
                    redisPool = new DefalutRedisPool(address, pass);
                }
                else {
                    throw new IllegalArgumentException("address in not correct:"+address);
                }
                if (redisPool != null)
                    POOL.put(name, redisPool);
                else
                    throw new NullPointerException("redis pool is null");

                if (logger.isInfoEnabled())
                    logger.info("Create redisCachePool{}:{} successfully", address);
            }
        }
    }

    private void errorConfig() {
        throw new RuntimeException("redis.properties config error");
    }
}
