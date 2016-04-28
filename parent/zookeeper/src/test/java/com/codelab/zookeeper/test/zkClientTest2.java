package com.codelab.zookeeper.test;


import com.codelab.zookeeper.ZKClient;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.junit.Test;

import java.util.List;

public class zkClientTest2 {

    @Test
    public void clientTest() {

        ZkClient client = ZKClient.getZkClient();

        client.create("/test","hello",CreateMode.EPHEMERAL);

        client.close();
    }
}
