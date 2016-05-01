package com.codelab.thrift;

import com.codelab.zookeeper.ZKClient;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.lang.Validate;
import org.apache.thrift.TProcessor;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.Properties;


public final class ThriftServiceRunner {

    static final Logger logger = LoggerFactory.getLogger(ThriftServiceRunner.class);

    static final String SERVRES = "servers";

    public static <T> void startThriftServer(T service, int port) {

        Validate.notNull(service, "service is null");
        String serviceDefName = ThriftUtils.getServiceDefinitionClass(service.getClass());
        String zkConfigPath = ThriftUtils.getThriftZKPath(serviceDefName);

        ZkClient zkClient = ZKClient.getZkClient();

        logger.info("zk client prepare create  the persistent node:{}", zkConfigPath);
        //create parent node
        if (!zkClient.exists(zkConfigPath))
            zkClient.createPersistent(zkConfigPath, true);

        try {
            //create new  Half-Sync/Half-Async server
            InetAddress address = InetAddress.getLocalHost();
            InetSocketAddress inetSocketAddress = new InetSocketAddress(address, port);
            logger.info("cerate HSHA server hostname:{},port:{}", address.getHostName(), port);
            TServer server = createHsHaServer(serviceDefName, service, inetSocketAddress);
            new Thread(new Runnable() {
                @Override
                public void run() {

                    String ip = address.getHostAddress() + ":" + port;
                    String registerPath = zkConfigPath + "/" + ip;
                    logger.info("zk client prepare cerate the ephemeral node:{}", registerPath);
                    try {
                        zkClient.createEphemeral(registerPath, new Properties().setProperty("hostname", address.getHostName()));
                        Properties p = zkClient.readData(zkConfigPath);
                        Integer servers = 0;
                        if (p == null)
                            p = new Properties();
                        else if (p.containsKey(SERVRES))
                            servers = (Integer) p.get(SERVRES);
                        p.put(SERVRES, servers + 1);
                        zkClient.writeData(zkConfigPath, p);
                        logger.info("start server on {}......", ip);
                        server.serve();
                    } catch (Exception e) {
                        logger.warn("server stoped,cause:{}", e.getMessage());
                        zkClient.close();
                    } finally {
                        logger.info("zkClient closed finally ");
                        zkClient.close();
                    }
                    logger.info("Thrift server stopped.");
                    System.exit(-1);
                }
            }, "ServerThread").start();

        } catch (Exception e) {
            logger.error("create server exception:{}", e.getMessage());
        }
    }

    public static <T> void startThriftServer(Class<T> serviceImplClass, GenericApplicationContext context, int port) {
        startThriftServer(serviceImplClass.cast(BeanFactoryUtils.beanOfTypeIncludingAncestors(context, serviceImplClass)), port);
    }


    private static <T> TServer createHsHaServer(String serviceDefName, T service, InetSocketAddress address) throws Exception {

        TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(address);
        //create processor
        Class<?> processorClass = Class.forName(serviceDefName + ThriftUtils.Constants.PROCESSOR_SUFFIX);
        Class<?> ifaceClass = Class.forName(serviceDefName + ThriftUtils.Constants.IFACE_SUFFIX);

        Constructor<TProcessor> ctor = (Constructor<TProcessor>) processorClass.getConstructor(ifaceClass);
        TProcessor processor = ctor.newInstance(service);

        return new THsHaServer(new THsHaServer.Args(serverTransport).processor(processor));

    }

}
