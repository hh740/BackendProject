package com.codelab.redis.test;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.BoundKeyOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * @author edwin wangke
 * @since 20 Jun 2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:context/context-redis.xml"})
public class RedisTemplateTest {

    private static Logger logger = LoggerFactory
            .getLogger(RedisTemplateTest.class);


    @Resource
    RedisTemplate redisTemplate;

    @Ignore
    @Test
    public void testFindProductUnderCarrier() {


        BoundValueOperations<String, String> ops = redisTemplate.boundValueOps("wang");
        String value = ops.get();
        Assert.assertEquals(value,"ke");

    }
}
