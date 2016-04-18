package com.xiaomi.infra.hbase.client.example;

import com.xiaomi.infra.hbase.client.HBaseClientFactory;
import com.xiaomi.infra.hbase.client.HBaseClientInterface;
import com.xiaomi.infra.hbase.client.HConfigUtil;
import com.xiaomi.infra.hbase.client.HException;
import com.xiaomi.miliao.zookeeper.EnvironmentType;
import com.xiaomi.miliao.zookeeper.ZKFacade;

// example code for user to use HBaseClient in onebox/staging/production environment.
public class EnviromentExample {
  // for onebox/staging/production usage, HBaseClient will depends a configuration node
  // in corresponding zookeeper. Users should create specific zookeeper nodes and write
  // configuration for their businesses.
  private String zkConfigPathUri;
  private HBaseClientInterface hbaseClient;
  
  public HBaseClientInterface createHBaseClientForOnebox() throws HException {
    // set enviroment to onebox
    ZKFacade.getZKSettings().setEnviromentType(EnvironmentType.ONEBOX);
    // in this example, we will use created zookeeper node '/databases/hbase/infra'
    // to config hbase client
    zkConfigPathUri = HConfigUtil.getBusinessConfigZkUri(HConfigUtil.HBASE_INFRA_NODE);
    // in onebox zookeeper, hbase server hostname is defined to 'oneboxhost', define
    // 'oneboxhost' in /etc/hosts in your test machine
    return HBaseClientFactory.getSingletonClient(zkConfigPathUri);
  }
  
  public void testHBaseForOnebox() throws HException {
    hbaseClient = createHBaseClientForOnebox();
    // do hbase operation here, refer HBaseClientExample to see example code
    hbaseClient.close();
  }
  
  public HBaseClientInterface createHBaseClientForStagingOrProducion() throws HException {
    // set environment to staging or production. here, we set to staging
    ZKFacade.getZKSettings().setEnviromentType(EnvironmentType.STAGING);
    
    // staging and production will use distributed hbase cluster which needs kerberos setting
    System.getProperties().setProperty("java.security.krb5.conf", "/etc/krb5-hadoop.conf");
    System.getProperties().setProperty("hadoop.property.hadoop.security.authentication",
      "kerberos");
    
    // also use /databases/hbase/infra to config hbase client. In staging enviroment, it will
    // make hbase client access hbase cluster 'hytst-staging'
    zkConfigPathUri = HConfigUtil.getBusinessConfigZkUri(HConfigUtil.HBASE_INFRA_NODE);
    return HBaseClientFactory.getSingletonClient(zkConfigPathUri);
  }
  
  public void testHBaseForStaging() throws HException {
    hbaseClient = createHBaseClientForStagingOrProducion();
    // do hbase operation here, refer HBaseClientExample to see example code
    hbaseClient.close();
  }
  
  public static void main(String args[]) throws HException {
    new EnviromentExample().testHBaseForOnebox();
    new EnviromentExample().testHBaseForStaging();
  }
}