package com.codelab.thrift;

import com.codelab.zookeeper.ZKClient;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.support.GenericApplicationContext;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Created by wangke on 16/4/27.
 */
public final class ThriftServiceRunner {

    static final Logger logger = LoggerFactory.getLogger(ThriftServiceRunner.class);

    public static <T> T startThriftServer(Class<T> serviceImplClass, GenericApplicationContext context, T service) {

        Validate.notNull(service, "service is null");
        String serviceDefName = ThriftUtils.getServiceDefinitionClass(serviceImplClass);
        String zkConfigPath = ThriftUtils.getThriftZKPath(serviceDefName);

        ZkClient zkClient = ZKClient.getZkClient();

        //create parent node
        if (!zkClient.exists(zkConfigPath))
            zkClient.createPersistent(zkConfigPath, true);


    }

    public static <T> T startThriftServer(Class<T> serviceImplClass, GenericApplicationContext context) {
        return startThriftServer(serviceImplClass, context, serviceImplClass.cast(BeanFactoryUtils.beanOfTypeIncludingAncestors(context, serviceImplClass)));
    }


    private static<T> void startThriftService(int port,Class<T> serverInstance) {

        String localBindIp = null;
        try {
            localBindIp = InetAddress.getLocalHost().getHostAddress();
            TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(10005);
            serverInstance
        } catch (UnknownHostException e) {
            logger.error("host is not known");
            e.printStackTrace();
        } catch (TTransportException e) {
            logger.error("");
            e.printStackTrace();
        }


        final TServer server = serverInstance;


        new Thread(new Runnable() {
            @Override
            public void run() {
                server.serve();
                logger.info("Thrift server stopped.");
                System.exit(-1);
            }
        }).start();

    }

    private static String getIp() {
        InetAddress.getLocalHost().getHostAddress();
    }


}
