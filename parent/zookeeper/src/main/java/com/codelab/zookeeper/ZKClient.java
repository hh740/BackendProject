package com.codelab.zookeeper;

import org.I0Itec.zkclient.ZkClient;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by wangke on 16/4/27.
 */
public class ZKClient {

    private static ZkClient zkClient = null;

    private static String CONFIG_FILE = "zookeeper.properties";

    private static Logger logger = Logger.getLogger(ZKClient.class);

    private ZKClient() {
    }

    public static synchronized ZkClient getZkClient() {
        if (zkClient == null) {
            logger.debug("get config input stream !");
            InputStream in = ZKClient.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
            Properties properties = new Properties();
            try {
                properties.load(in);
            } catch (IOException e) {
                logger.error("Load " + CONFIG_FILE + " failed");
                throw new RuntimeException(e);
            }

            if(!properties.containsKey("zk.hosts")){
                logger.error("Load param zk.hosts failed");
                throw new IllegalArgumentException(CONFIG_FILE +"do not have zk.host key");
            }
            String hosts  =  properties.getProperty("zk.hosts");
            zkClient = new ZkClient(hosts);
        }
        return zkClient;

    }
}

