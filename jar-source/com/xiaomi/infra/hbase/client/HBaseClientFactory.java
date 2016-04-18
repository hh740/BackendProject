package com.xiaomi.infra.hbase.client;

import java.io.IOException;
import java.lang.reflect.Proxy;

import com.xiaomi.infra.hbase.client.duplication.DuplicationInvocationHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;

import com.xiaomi.infra.hbase.client.async.AsyncInvocationHandler;

public class HBaseClientFactory {
  private static final Log LOG = LogFactory.getLog(HBaseClientFactory.class);
  public static final String HBASE_CLIENT_ENABLE_ASYNC_PROXY = "hbase.client.enable.asyncproxy";
  private static boolean enableDuplicationProxy;
  private static boolean enableAsyncProxy;
  private static InternalHBaseClient singletonClient = null;
  private static HBaseClientInterface singletonDuplicationProxy = null;
  private static HBaseClientInterface singletonAsyncClientProxy = null;
  private static volatile HBaseClientInterface returnedClient = null;
  private static DuplicationInvocationHandler dupHandler = null;
  private static AsyncInvocationHandler asyncHandler = null;
  private static final Object singletonClientLock = new Object();
  
  // this method will read core-site.xml, hbase-site.xml.. etc in classpath to create an configuration
  public static HBaseClientInterface getSingletonClient() throws HException {
    return createSingletonClient(new Class<?>[]{}, new Object[]{});
  }
  
  public static HBaseClientInterface createClient() throws HException {
    return createInternalClient(new Class<?>[]{}, new Object[]{});
  }
  
  public static HBaseClientInterface getSingletonClient(Configuration config) throws HException {
    return createSingletonClient(new Class<?>[]{Configuration.class}, new Object[]{config});
  }
  
  public static HBaseClientInterface createClient(Configuration config) throws HException {
    return createInternalClient(new Class<?>[] { Configuration.class }, new Object[] { config });
  }
  
  public static HBaseClientInterface getSingletonClient(String configPath) throws HException {
    return createSingletonClient(new Class<?>[]{String.class}, new Object[]{configPath});
  }
  
  public static HBaseClientInterface createClient(String configPath) throws HException {
    return createInternalClient(new Class<?>[] { String.class }, new Object[] { configPath });
  }

  public static HBaseClientInterface getSingletonClient(String zkUri,
                                                        boolean loadParentZkPath,
                                                        boolean autoReload) throws HException {
    return createSingletonClient(new Class<?>[]{String.class, boolean.class, boolean.class},
                                 new Object[]{zkUri, loadParentZkPath, autoReload});
  }

  public static HBaseClientInterface createClient(String zkUri,
      boolean loadParentZkPath,
      boolean autoReload) throws HException {
    return createInternalClient(new Class<?>[]{String.class, boolean.class, boolean.class},
      new Object[]{zkUri, loadParentZkPath, autoReload});
  }
  
  // create an configuration which will connect hbase server in hbaseServerHost
  public static Configuration createConfigWithServiceHost(String hbaseServerHost) {
    Configuration config = HBaseConfiguration.create();
    // HBaseConfiguration.create() will read config files such core-site.xml, hbase-site.xml... etc in
    // classpath, these config may prevent us connecting to localhost hbase. Therefore, we call config.clear
    // and only set hbase.zookeeper.quorum which is enough for client to access hbase server.
    // Caution: if any other default configs are needed in future, we must add them here.
    config.clear();
    config.set(HConstants.ZOOKEEPER_QUORUM, hbaseServerHost);
    return config;
  }
  
  protected static HBaseClientInterface createSingletonClient(Class<?>[] paraTypes, Object[] parameters) throws HException {
    if (returnedClient == null) {
      synchronized (singletonClientLock) {
        if (returnedClient == null) {
          try {
            singletonClient = InternalHBaseClient.class.getConstructor(paraTypes).newInstance(parameters);
            enableDuplicationProxy = singletonClient.getConfiguration().get(
                DuplicationInvocationHandler.HBASE_CLIENT_DUP_CLUSTER_KEY) != null;
            enableAsyncProxy = singletonClient.getConfiguration().getBoolean(
              HBASE_CLIENT_ENABLE_ASYNC_PROXY, false);
            LOG.info("Create Singleton HBaseClientInterface, enableAsyncProxy=" + enableAsyncProxy
                + ", enableDuplicationProxy=" + enableDuplicationProxy);
          } catch (Exception e) {
            throw new HException("Create Singleton HBaseClient fail", e);
          }

          HBaseClientInterface client = singletonClient;
          if (enableDuplicationProxy) {
            singletonDuplicationProxy = createDuplicationProxy(client);
            client = singletonDuplicationProxy;
          }

          if (enableAsyncProxy) {
            singletonAsyncClientProxy = createAsyncClientProxy(client);
            client = singletonAsyncClientProxy;
          }
          returnedClient = client;
        }
      }
    }
    // TODO(cuijianwei): make HBaseClient an internal class, so that users must invoke
    // getSingletonClient to create HBaseClientInterface. We need have full control
    // to decide create a HBaseClient or a AsyncProxy
    return returnedClient;
  }
  
  private static HBaseClientInterface createInternalClient(Class<?>[] paraTypes, Object[] parameters)
      throws HException {
    try {
      return InternalHBaseClient.class.getConstructor(paraTypes).newInstance(parameters);
    } catch (Exception e) {
      throw new HException("Create HBaseClient fail", e);
    }
  }
  
  public static void closeSingletonClient() throws HException {
    LOG.info("Close Singleton HBaseClientInterface, enableAsyncProxy=" + enableAsyncProxy);
    if (returnedClient != null) {
      synchronized (singletonClientLock) {
        if (returnedClient != null) {
          closeClientProxy();
          singletonClient.close();
          singletonClient = null;
          returnedClient = null;
        }
      }
    }
  }

  protected static HBaseClientInterface createDuplicationProxy(HBaseClientInterface impl)
      throws HException {
    dupHandler = new DuplicationInvocationHandler(impl);
    return (HBaseClientInterface) Proxy.newProxyInstance(
        HBaseClientInterface.class.getClassLoader(), new Class[] { HBaseClientInterface.class },
        dupHandler);
  }

  protected static HBaseClientInterface createAsyncClientProxy(HBaseClientInterface impl) {
    asyncHandler = new AsyncInvocationHandler(impl);
    return (HBaseClientInterface) Proxy.newProxyInstance(
      HBaseClientInterface.class.getClassLoader(), new Class[] { HBaseClientInterface.class },
      asyncHandler);
  }
  
  protected static void closeClientProxy() {
    if (singletonAsyncClientProxy != null) {
      asyncHandler.close();
      asyncHandler = null;
      singletonAsyncClientProxy = null;
    }

    if (singletonDuplicationProxy != null) {
      dupHandler.close();
      dupHandler = null;
      singletonDuplicationProxy = null;
    }
  }
}
