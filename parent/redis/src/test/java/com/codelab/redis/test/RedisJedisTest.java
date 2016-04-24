package com.codelab.redis.test;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;

/**
 * @author wangke
 * @since 20 Jun 2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:context/context-redis.xml"})
public class RedisJedisTest {

    private static Logger logger = LoggerFactory
            .getLogger(RedisJedisTest.class);


    @Resource
    JedisPool jedisPool;

    @Ignore
    @Test
    public void testFindProductUnderCarrier() {

        Jedis jedis = jedisPool.getResource();
        try {
            jedis.set("test", "test");
            String str = jedis.get("test");
            Assert.assertEquals(str, "test");
            jedis.del("test");

        } finally {
            jedisPool.returnResource(jedis);
        }

    }
}
