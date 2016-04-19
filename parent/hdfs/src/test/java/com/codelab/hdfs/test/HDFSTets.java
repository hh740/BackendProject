package com.codelab.hdfs.test;

import com.codelab.hdfs.HDFSService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;


/**
 * @author edwin wangke
 * @since 20 Jun 2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:context/context-hdfs.xml"})
public class HDFSTets {

    private static Logger logger = LoggerFactory
            .getLogger(HDFSTets.class);

    @Resource
    HDFSService hdfsService;

    @Ignore
    @Test
    public void testHDFS() {


        hdfsService.readFileFromHDFS("/test",10);
    }
}
