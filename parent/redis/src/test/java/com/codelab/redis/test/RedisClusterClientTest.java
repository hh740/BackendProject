package com.codelab.redis.test;

import com.codelab.redis.cluster.*;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author edwin wangke
 * @since 20 Jun 2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:context/context-redis.xml"})
public class RedisClusterClientTest {

    private static Logger logger = LoggerFactory
            .getLogger(RedisClusterClientTest.class);

    @Ignore
    @Test
    public void testCluster() {
        RedisPoolFactory factory = RedisPoolFactory.getInstance();
        //get diff type of pool
        RedisPool pool1 = factory.getRedisPool("test1");
        RedisStandaloneClient rc = (RedisStandaloneClient)pool1.getRedisClient();
        rc.set("abc","def");
        String res = rc.get("abc");
        Assert.assertEquals(res,"def");

        RedisPool pool2 = factory.getRedisPool("test2");
        RedisClusterClient rc2 =(RedisClusterClient) pool2.getRedisClient();
        rc2.set("abc","helloworld");
        res = rc2.get("abc");


    }
}
