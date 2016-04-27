package com.codelab.zookeeper.test;


import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.junit.Test;

import java.util.List;

public class zkClientTest {

    @Test
    public void clientTest() {

        ZkClient zkClient = new ZkClient("localhost:2181", 5000);
        //根据类型创建节点

        //注册监听器
        zkClient.subscribeChildChanges("/test", new IZkChildListener() {
            @Override
            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {

                System.out.println("children changes ");
                System.out.println("parent path:" + parentPath);
                if (!currentChilds.isEmpty())
                    for (String child : currentChilds)
                        System.out.println("child :" + child);
                else
                    System.out.println("child node is empty ! ");
            }
        });

        zkClient.subscribeDataChanges("/test", new IZkDataListener() {

                    @Override
                    public void handleDataChange(String dataPath, Object data) throws Exception {
                        System.out.println(dataPath + " content changes " + data.toString());

                    }

                    @Override
                    public void handleDataDeleted(String dataPath) throws Exception {
                        System.out.println("delete path:" + dataPath);
                    }
                }
        );

        zkClient.create("/test", "test1", CreateMode.PERSISTENT);
        zkClient.createPersistent("/test/test3");
        zkClient.createEphemeral("/test2");

        //获取数据
        zkClient.writeData("/test","test2");
        String data = zkClient.readData("/test");

        //删除节点
        zkClient.deleteRecursive("/test");


        zkClient.close();


    }
}
