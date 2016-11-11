package com.codelab.thrift.test;

import com.codelab.thrift.ThriftServiceRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Note: the JUNIT can not test the mult thread program
 * so use the main fuction to start a example server
 */
public class ThriftServerTest {

    public static Logger logger = LoggerFactory.getLogger(ThriftServerTest.class);

    public static void main(final String[] args) throws Exception {
        // two ways of start a thrift server
        // note: make sure the context-thrift scan the whole package,and the impl class
        // have the @service tag
        //   1
        ThriftServiceRunner.startThriftServer(new HelloImpl(), 16663);

        //   2
        // GenericApplicationContext ac = new GenericXmlApplicationContext("classpath:context/context-thrift.xml");
        // ThriftServiceRunner.startThriftServer(HelloImpl.class, ac, 16666);
    }
}
