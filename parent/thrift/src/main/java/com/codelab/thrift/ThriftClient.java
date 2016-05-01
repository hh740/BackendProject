package com.codelab.thrift;

import com.codelab.zookeeper.ZKClient;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Properties;

/**
 * Created by wangke on 16/4/30.
 */
public class ThriftClient {

    static final Logger logger = LoggerFactory.getLogger(ThriftClient.class);

    static final String CLIENTS = "clients";

    static final String SERVERS = "servers";

    static Object client = null;

    static String aimServer;

    private ThriftClient() {

    }

    public synchronized static <Iface> Iface getClient(Class<Iface> clazz) {

        if (client != null)
            return (Iface) client;

        String serviceDefName = ThriftUtils.getServiceDefinitionClass(clazz);
        String zkConfigPath = ThriftUtils.getThriftZKPath(serviceDefName);
        ZkClient zkClient = ZKClient.getZkClient();

        logger.debug("zk client prepare create  the persistent node:{}", zkConfigPath);
        if (!zkClient.exists(zkConfigPath))
            logger.error("path not found:" + zkConfigPath);

        List<String> nodes = zkClient.getChildren(zkConfigPath);
        if (nodes.isEmpty())
            logger.error("no useful nodes found:" + zkConfigPath);

        aimServer = chooseAimServer(zkClient, zkConfigPath, nodes);

        //register callback
        zkClient.subscribeChildChanges(zkConfigPath, new IZkChildListener() {
            @Override
            public void handleChildChange(String s, List<String> list) throws Exception {
                logger.info("node:{} changes,child num:{}", s, list.size());
                //if the server changes,redo get client
                if (list.isEmpty()){
                    client = null;
                    logger.debug("client return null");
                }
                else if (!list.contains(aimServer)) {
                    String aimAddress = chooseAimServer(zkClient, zkConfigPath, list);
                    logger.debug("before recreate client:{}", client);
                    client = createNewClient(aimAddress, clazz);
                    logger.debug("aimAddress:{},aimServer:{}", aimAddress, aimServer);
                    aimServer = aimAddress;
                    logger.debug("after recreate client:{}", client);
                }
            }
        });

        try {
            client = createNewClient(aimServer, clazz);
        } catch (Exception e) {
            logger.error("create client error cause:{}", e.getMessage());
        }
        return (Iface) client;
    }


    private static String chooseAimServer(ZkClient zkClient, String zkConfigPath, List<String> nodes) {

        Properties p = zkClient.readData(zkConfigPath);
        if (p == null)
            throw new NullPointerException("properties is null,please check the thrift server running");
        Integer clients = (Integer) p.getOrDefault(CLIENTS, 0) + 1;
        Integer servers = nodes.size();
        p.put(CLIENTS, clients);
        p.put(SERVERS, servers);
        zkClient.writeData(zkConfigPath, p);
        return nodes.get(clients % servers);
    }

    private static <Iface> Iface createNewClient(String aimAddress, Class<Iface> clazz) throws Exception {
        //create client
        String[] address = aimAddress.split("\\:");
        if (address.length != 2)
            logger.warn("adress format is not well ");
        String ip = address[0];
        Integer port = Integer.valueOf(address[1]);

        TTransport transport = new TFramedTransport(new TSocket(ip, port));
        transport.open();
        TProtocol protocol = new TBinaryProtocol(transport);
        Constructor cons = clazz.getConstructor(TProtocol.class);
        return (Iface) cons.newInstance(protocol);
    }
}
