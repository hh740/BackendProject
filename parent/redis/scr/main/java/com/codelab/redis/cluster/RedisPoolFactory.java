package com.codelab.redis.cluster;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
    public RedisPool newCachePool(String host, int port) {
        throw new RuntimeException("Unsupported operation now");
    }

    public RedisPool getCachePool(RedisCfg.Parts redisCfgPart) {
        return getCachePool(redisCfgPart.name());
    }

    @Override
    public RedisPool getCachePool(String name) {
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
                String[] s = key.split("\\.");
                if (s == null || s.length < 3)
                    errorConfig();
                String kport = s[0] + "." + s[1] + ".port";
                String kpass = s[0] + "." + s[1] + ".pass";
                logger.debug("kport:{},kpass:{}", kport, kpass);
                if (!properties.containsKey(kport) || !properties.containsKey(kpass))
                    errorConfig();
                Integer port = Integer.valueOf(properties.getProperty(kport));
                String host = properties.getProperty(key);
                String pass = properties.getProperty(kpass);
                String name = s[1];
                //根据不同类型放入不同的类型的Redis客户端
                RedisPool cachePool = new RedisPool(host, port, pass);
                POOL.put(name, cachePool);
                if (logger.isInfoEnabled())
                    logger.info("Create redisCachePool {}[{}:{}] successfully", name, host, port);
            }
        }
    }

    private void errorConfig() {
        throw new RuntimeException("redis.properties config error");
    }
}
