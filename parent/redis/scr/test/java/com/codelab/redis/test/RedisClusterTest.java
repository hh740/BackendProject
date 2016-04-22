package com.codelab.redis.test;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class RedisClusterTest {

    private static Logger logger = LoggerFactory
            .getLogger(RedisClusterTest.class);

    @Test
    public void testCluster() {



    }
}
