package com.xiaomi.infra.hbase.client;

import java.io.ByteArrayInputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.base.Preconditions;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.util.Pair;

import com.xiaomi.miliao.zookeeper.ZKFacade;

/**
 * <p>HBaseClient configuration utility class, including
 * <p>1. utility methods to read HBaseClient configuration for business
 * @author cuijianwei
 *
 */
public class HConfigUtil {
  private static final Log LOG = LogFactory.getLog(HConfigUtil.class);
  
  public static final String ZK_PREFIX = "zk://";
  public static final String LOCAL_FILE_PREFIX = "file:///";
  public static final int ZK_SESSION_TIMEOUT = 30000;
  public static final int ZK_CONNECTION_TIMEOUT = 30000;
  public static final String SLASH = "/";
  public static final String ZK_SERVER_SEPERATOR = ",";

  // business zk path string
  public static final String HBASE_BUSINESS_ROOT_NODE = "/databases/hbase";
  public static final String HBASE_MICLOUD_GALLERY_ALBUMSHARETAG_NODE = "micloud/gallery_albumsharetag";
  public static final String HBASE_MICLOUDE_NODE = "micloud"; // temporary keep this to compatible with old version
  public static final String HBASE_MILIAO_NODE = "miliao";
  public static final String HBASE_INFRA_NODE = "infra"; // config node for infra test
  public static final String HBASE_MICLOUD_SMS = "micloud/sms";
  public static final String HBASE_MICLOUD_PHONECALL = "micloud/phonecall";
  public static final String HBASE_MICLOUD_SMS_SEARCH = "micloud/sms_search";
  public static final String HBASE_XMPUSH = "xmpush";

  private static ConcurrentMap<String, ZkClient> zkClients =
      new ConcurrentHashMap<String, ZkClient>();

  private static ZkClient getZkClient(String servers) {
    ZkClient client = zkClients.get(servers);
    if (client == null) {
      client = new ZkClient(servers, ZK_SESSION_TIMEOUT, ZK_CONNECTION_TIMEOUT,
                                     new BytesPushThroughSerializer());
      ZkClient current = zkClients.putIfAbsent(servers, client);
      if (current != null) {
        client.close();
        client = current; // keep the previous one
      }
    }
    return client;
  }

  public static String getBusinessConfigZkUri(String zkServers, String businessName)
      throws HException {
    return ZK_PREFIX + zkServers + getBusinessConfigZkPath(businessName);
  }

  public static String getBusinessConfigZkUriDirectly(String zkServers, String businessName)
      throws HException {
    return ZK_PREFIX + zkServers + getBusinessConfigZkPath(businessName);
  }
  
  public static String getBusinessConfigZkUri(String businessName) throws HException {
    return getBusinessConfigZkUri(ZKFacade.getZKSettings().getZKServers(), businessName);
  }

  protected static String getBusinessConfigZkPath(String businessName) {
    return HBASE_BUSINESS_ROOT_NODE + SLASH + businessName;
  }

  // a simple function to get server and path from zkUri: zk://server/path
  // where server is formatted as 'host_1,host_2,host_3:port'
  protected static Pair<String, String> getZkServerAndPath(String zkUri) throws HException {
    try {
      // skip "zk://"
      String tempZkUri = zkUri.substring(5);
      int firstSlashIndex = tempZkUri.indexOf("/");
      String server = tempZkUri.substring(0, firstSlashIndex);
      String path = tempZkUri.substring(firstSlashIndex);
      return new Pair<String, String>(server, path);
    } catch (Exception e) {
      throw new HException(e);
    }
  }

  // Get parent node path, for example:
  // /databases/hbase/infra/failover-test ==> /databases/hbase/infra
  // zk://localhost:2181/foo/bar ==> zk://localhost:2181/foo
  protected static String getParentZkPath(String path) {
    // path terminated with / is not valid
    Preconditions.checkArgument(!path.endsWith("/"), "Invalid zk node path: " + path);
    int pos = path.lastIndexOf("/");
    if (pos == -1) {
      return null;
    }
    return path.substring(0, pos);
  }

  protected static byte[] loadConfigFromZK(String zkUri) throws HException {
    Pair<String, String> zkServerAndPath = getZkServerAndPath(zkUri);
    String server = zkServerAndPath.getFirst();
    String path = zkServerAndPath.getSecond();
    LOG.info("HBase load client information from zkServer=" + server + ", zkPath=" + path);
    ZkClient client = getZkClient(server);
    return client.readData(path);
  }

  protected static Configuration loadHBaseConfigFromZK(String zkUri, boolean loadPrentPath)
      throws HException {
    Configuration conf = HBaseConfiguration.create();
    if (loadPrentPath) {
      conf.addResource(new ByteArrayInputStream(loadConfigFromZK(getParentZkPath(zkUri))));
    }
    conf.addResource(new ByteArrayInputStream(loadConfigFromZK(zkUri)));
    return conf;
  }

  public static boolean isZkPath(String path) {
    if (path.startsWith(ZK_PREFIX)) {
      return true;
    }
    return false;
  }

  public static boolean isLocalFile(String path) {
    if (path.startsWith(LOCAL_FILE_PREFIX)) {
      return true;
    }
    return false;
  }
  
  public static String getClusterName(String baseClusterName, String tableName) {
    if (!tableName.startsWith(InternalHBaseClient.HBASE_URI_PREFIX)) {
      return baseClusterName;
    }
    String clusterName = tableName.substring(InternalHBaseClient.HBASE_URI_PREFIX.length());
    int idx = clusterName.indexOf('/');
    if (idx != -1)  {
      clusterName = clusterName.substring(0, idx);
      idx = clusterName.indexOf(':');
      if (idx != -1) {
        return clusterName.substring(0, idx);
      }
      return clusterName;
    }
    return clusterName;
  }
  
  protected static void traceConfigChangeFromZk(final String zkUri,
                                                final boolean loadParentZkPath,
                                                final ConfigurationListener listener)
      throws HException {
    if (listener != null) {
      Pair<String, String> zkServerAndPath = getZkServerAndPath(zkUri);
      String path = zkServerAndPath.getSecond();
      String parentPath = getParentZkPath(path);

      ZkClient zkClient = getZkClient(zkServerAndPath.getFirst());
      LOG.info("trace config change from zk server = " + zkServerAndPath.getFirst() + ", path = "
                   + path);
      IZkDataListener zkDataListener = new IZkDataListener() {
        @Override public void handleDataChange(String s, Object o) throws Exception {
          LOG.info("hbase configuration content updated, start to reload configuration");
          Configuration conf = loadHBaseConfigFromZK(zkUri, loadParentZkPath);
          listener.onChanged(conf);
        }

        @Override public void handleDataDeleted(String s) throws Exception {
        }
      };
      zkClient.subscribeDataChanges(path, zkDataListener);
      if (loadParentZkPath) {
        zkClient.subscribeDataChanges(getParentZkPath(path), zkDataListener);
      }
    }
  }

  public static void traceConfigChangeFromZk(final String zkUri) throws HException {
    traceConfigChangeFromZk(zkUri, false, null);
  }

  // if 'hbase.cluster.name' is set in System.property, full table name is:
  // 'hbase://hbase.cluster.name'; otherwise return 'tableName' directly.
//  public static String getFullTableName(String tableName) {
//    String clusterName = getClusterName();
//    String fullTableName = tableName;
//    if (clusterName != null && clusterName.length() > 0) {
//      fullTableName = "hbase://" + clusterName + "/" + tableName;
//    }
//    LOG.debug("getFullTableName from clusterName=" + clusterName + ", tableName=" + tableName
//        + ", fullTableName=" + fullTableName);
//    return fullTableName;
//  }
  
  // return 'hbase://clusterName/tableName'
  public static String getFullTableName(String clusterName, String tableName) {
    return "hbase://" + clusterName + "/" + tableName;
  }
  
  // return 'hbase://clusterName/tableName'
  // if 'tableName' has the form : 'hbase://clusterName0/tableName',
  // also return hbase://clusterName/tableName;
//  public static String constructFullTableName(String clusterName, String tableName) {
//    final String prefix = "hbase://";
//    if (tableName.startsWith(prefix)) {
//      tableName = tableName.substring(prefix.length(), tableName.length());
//      String[] splits = tableName.split("/");
//      Preconditions.checkArgument(splits.length == 2, "Illegal table name %s", tableName);
//      tableName = splits[1];
//    }
//    return getFullTableName(clusterName, tableName);
//  }
//
//  public static String constructFullTableName(String tableName) {
//    if (tableName.startsWith("hbase://")) {
//      return tableName;
//    }
//    return getFullTableName(tableName);
//  }

  public static interface ConfigurationListener {
    void onChanged(Configuration config);
  }
}
