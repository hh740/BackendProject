package com.codelab.thrift.test;

import com.codelab.thrift.ThriftClient;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.junit.Ignore;
import org.junit.Test;

/**
 * the example of start a thrift server
 */
public class ThriftClientTest {


    public ThriftClientTest() throws Exception {
    }

    public void startClientNormal() {

        TTransport transport = new TFramedTransport(new TSocket("192.168.1.108", 16666));
        try {
            transport.open();
        } catch (TTransportException e) {
            e.printStackTrace();
        }
        // set the protocol
        TBinaryProtocol protocol = new TBinaryProtocol(transport);
        Hello.Client client = new Hello.Client(protocol);
        // call the prc function
        try {
            int a = client.helloInt(100);
            System.out.print("a" + a);
        } catch (TException e) {
            e.printStackTrace();
        }
        transport.close();

    }

    static private  Hello.Iface client = ThriftClient.getClient(Hello.Client.class);

    public static void main(final String[] args) throws Exception {

        Integer result =  client.helloInt(20);
        Thread.sleep(45000);

        //reget the client,if the function have exceppion,retry three times
        client = ThriftClient.getClient(Hello.Client.class);
        result =  client.helloInt(20);
        System.out.println(client);


    }
}
