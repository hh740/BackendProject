package com.xiaomi.infra.hbase.client;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.DoNotRetryIOException;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HRegionLocation;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Condition;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Row;
import org.apache.hadoop.hbase.client.RowMutations;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.coprocessor.AggregationClient;
import org.apache.hadoop.hbase.client.coprocessor.Batch;
import org.apache.hadoop.hbase.client.coprocessor.LongColumnInterpreter;
import org.apache.hadoop.hbase.client.replication.ReplicationAdmin;
import org.apache.hadoop.hbase.coprocessor.ColumnInterpreter;
import org.apache.hadoop.hbase.coprocessor.example.generated.BulkDeleteProtos.BulkDeleteRequest;
import org.apache.hadoop.hbase.coprocessor.example.generated.BulkDeleteProtos.BulkDeleteRequest.Builder;
import org.apache.hadoop.hbase.coprocessor.example.generated.BulkDeleteProtos.BulkDeleteRequest.DeleteType;
import org.apache.hadoop.hbase.coprocessor.example.generated.BulkDeleteProtos.BulkDeleteResponse;
import org.apache.hadoop.hbase.coprocessor.example.generated.BulkDeleteProtos.BulkDeleteService;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.ipc.BlockingRpcCallback;
import org.apache.hadoop.hbase.ipc.CoprocessorRpcChannel;
import org.apache.hadoop.hbase.ipc.ServerRpcController;
import org.apache.hadoop.hbase.protobuf.ProtobufUtil;
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos;
import org.apache.hadoop.hbase.protobuf.generated.HBaseProtos;
import org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.EmptyMsg;
import org.apache.hadoop.hbase.protobuf.generated.HBaseProtos.LongMsg;
import org.apache.hadoop.hbase.protobuf.generated.MultiRowMutationProtos;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Pair;

import com.google.protobuf.Service;
import com.xiaomi.common.perfcounter.PerfCounter;
import com.xiaomi.infra.hbase.client.tablepool.HTablePool;
import com.xiaomi.infra.hbase.client.tablepool.HTablePoolStatistics;
import com.xiaomi.infra.hbase.salted.KeySalter;
import com.xiaomi.infra.hbase.salted.SaltedHTable;
import com.xiaomi.miliao.counter.MultiCounter;

/**
 * Implementation class of HBaseClientInterface
 * @author cuijianwei
 * TODO make this private
 */
public class InternalHBaseClient implements HBaseClientInterface {
  
  private static final Log LOG = LogFactory.getLog(InternalHBaseClient.class);
  
  public static final String HBASE_URI_PREFIX = "hbase://";
  public static final int HBASE_URI_PREFIX_LENGTH = HBASE_URI_PREFIX.length();
  // hbase client configuration key name
  public static final String HTABLE_POOL_MAX_SIZE = "hbase.client.tablepool.maxsize";
  public static final String HBASE_CLIENT_SCANNER_CACHING = "hbase.client.scanner.caching";
  public static final String HBASE_IPC_CLIENT_CONNECTION_MAXIDELTIME = "hbase.ipc.client.connection.maxidletime";
  public static final String ZK_RECOVERY_RETRY = "zookeeper.recovery.retry";
  public static final String HTABLE_AUTOFLUSH = "hbase.client.autoflush";
  public static final String HBASE_CLUSTER_NAME = "hbase.cluster.name";
  public static final String HBASE_WRITE_ENABLED = "hbase.client.write.enabled";
  public static final String HBASE_READ_ENABLED = "hbase.client.read.enabled";
  public static final String HBASE_ADMIN_ENABLED = "hbase.client.admin.enabled";
  public static final String HBASE_SLOW_ACCESS_CUTOFF = "hbase.slow.access.cutoff";
  public static final String HBASE_IPC_CLIENT_TPCNODELAY = "hbase.ipc.client.tcpnodelay";
  public static final String HBASE_IPC_CREATE_SOCKET_TIMEOUT = "ipc.socket.timeout";

  public static final int DEFAULT_HATBLE_POOL_MAX_SIZE = 10;
  public static final int DEFAULT_HBASE_SLOW_ACCESS_CUTOFF = 300;
  // replication id for cross-cluster replication add and remove
  public static final String REPLICATION_ID_KEY = "hbase.client.replication.id";
  public static final String DEFAULT_REPLICATION_ID_KEY = "10";

  protected Map<String, HConnection> connections = new ConcurrentHashMap<String, HConnection>();

  protected HConnection getOrCreateConnection(String clusterName, Configuration conf)
      throws IOException {
    clusterName = clusterName == null ? "" : clusterName;
    HConnection connection = connections.get(clusterName);
    if (connection != null) {
      return connection;
    }
    return createAndCacheConnection(clusterName, conf);
  }
  
  protected synchronized HConnection createAndCacheConnection(String clusterName, Configuration conf)
      throws IOException {
    HConnection connection = connections.get(clusterName);
    if (connection != null) {
      return connection;
    }
    
    Configuration clusterConf = HBaseClientUtil.getConfigurationByClusterName(conf, clusterName);
    connection = HConnectionManager.createConnection(clusterConf);
    connections.put(clusterName, connection);
    return connection;
  }

  protected void closeHTablePool(HTablePool tablePool) throws HException {
    if (tablePool != null) {
      try {
        tablePool.close();
      } catch (IOException ioe) {
        throw new HException("Failed to close HBase table pool", ioe);
      }
    }
  }
  
  protected void closeConnections(Map<String, HConnection> connections) throws HException {
    if (connections != null) {
      try {
        for (HConnection connection : connections.values()) {
          connection.close();
        }
        connections.clear();
      } catch (IOException e) {
        throw new HException("Failed to close HBase connection", e);
      }
    }
  }
  
  protected Pair<String, String> getClusterAndTableName(String tableName) {
    if (tableName.startsWith(HBASE_URI_PREFIX)) {
      String subString = tableName.substring(HBASE_URI_PREFIX_LENGTH);
      int endIndex = subString.indexOf('/');
      return new Pair<String, String>(subString.substring(0, endIndex),
          subString.substring(endIndex + 1));
    } else {
      return new Pair<String, String>(this.clusterName, tableName);
    }
  }
  
  protected String getFullTableName(String tableName) {
    if (tableName.startsWith(HBASE_URI_PREFIX)) {
      return tableName;
    } else {
      if (this.clusterName.isEmpty()) {
        return tableName;
      } else {
        return HBASE_URI_PREFIX + this.clusterName + "/" + tableName;
      }
    }
  }
  
  // hbase client configuration keys
  public static final String[] HBASE_CLIENT_CONFIG_KEYS = new String[] {
      HConstants.HBASE_CLIENT_IPC_POOL_SIZE, HConstants.HBASE_RPC_TIMEOUT_KEY,
      HConstants.HBASE_CLIENT_OPERATION_TIMEOUT, HConstants.HBASE_CLIENT_RETRIES_NUMBER,
      HConstants.HBASE_CLIENT_PREFETCH_LIMIT, HBASE_CLIENT_SCANNER_CACHING,
      HBASE_IPC_CLIENT_CONNECTION_MAXIDELTIME, HConstants.ZK_SESSION_TIMEOUT, ZK_RECOVERY_RETRY,
      HTABLE_POOL_MAX_SIZE, HConstants.ZOOKEEPER_QUORUM, "hbase.zookeeper.property.clientPort",
      HBASE_CLUSTER_NAME, HBASE_WRITE_ENABLED, HBASE_READ_ENABLED, HTABLE_AUTOFLUSH,
      HBASE_SLOW_ACCESS_CUTOFF, HBASE_IPC_CLIENT_TPCNODELAY, HBASE_IPC_CREATE_SOCKET_TIMEOUT};

  // perfcount name
  public static final String PERFORMCOUNT_SCAN = "scan";
  public static final String PERFORMCOUNT_SALTED_SCAN = "saltedScan";

  private volatile Configuration config;
  // After https://phabricator.d.xiaomi.net/D19528, HTable will be created from HConnection,
  // then, the HTable will be closed after each operation. For online services, this is desirable
  // because each mutation should be applied to server. However, for offline usage, such as
  // MR/Spark, users expect buffered mutating, but setting autoflush to 'false' for HTable
  // could not help because it will always be closed after put.
  // The desirable solution is that there is a global BufferedMutator, see BufferedMutatorImpl.java
  // in trunk code. In 0.98, we don't have such class. So, we keep a HTablePool here, and if users
  // set autoflush to 'false', we will get HTable from hTablePool for put and batch-put methods,
  // the hTablePool will be closed when the client reload or close.
  private volatile HTablePool hTablePool;
  private volatile int maxPooledHTableCount;
  private volatile AggregationClient aggregationClient;
  private volatile boolean autoFlush;
  private volatile int slowAccessCutoff;
  private volatile int scanCaching;
  private volatile boolean readEnabled;
  private volatile boolean writeEnabled;
  private volatile boolean adminEnabled;
  private volatile String clusterName = "";
  private boolean closed = false;
  
  public static final String HBASE = "hbase";
  public static final String HBASE_PERFCOUNT_PRFEIX = HBASE + MultiCounter.PATH_SEPARATOR_STRING;
  public static final String HBASE_FAIL = HBASE_PERFCOUNT_PRFEIX + MultiCounter.FAIL_SUFFIX;

  public InternalHBaseClient() throws HException {
    this(HBaseConfiguration.create());
  }

  public InternalHBaseClient(Configuration config) throws HException {
    reload(config);
  }

  // configPath could be local file path
  // file:///var/config..., or zk path: zk://host:port/path
  public InternalHBaseClient(String configPath) throws HException {
    this(configPath, false, true);
  }

  public InternalHBaseClient(String configPath, boolean loadZkParentPath, boolean autoReload)
      throws HException {
    Configuration conf = loadConfiguration(configPath, loadZkParentPath);
    reload(conf);
    if (autoReload && HConfigUtil.isZkPath(configPath)) {
      // It's safe to reference this at this point
      final InternalHBaseClient client = this;
      HConfigUtil.traceConfigChangeFromZk(configPath,
                                          loadZkParentPath,
                                          new HConfigUtil.ConfigurationListener() {
                                            @Override public void onChanged(Configuration config) {
                                              try {
                                                // safe to call this concurrently
                                                client.reload(config);
                                              } catch (HException he) {
                                                LOG.error("Failed to reload hbase configuration: "
                                                              + config);
                                              }
                                            }
                                          });
    }
  }

  public synchronized void reload(final Configuration config) throws HException {
    this.config = config;
    String tmpClusterName = this.config.get(InternalHBaseClient.HBASE_CLUSTER_NAME);
    this.clusterName = tmpClusterName == null ? "" : tmpClusterName;
    LOG.info("Load HBase configuration, cluster name=" + clusterName);

    // This update can be partially exposed to the caller during update, it's OK
    maxPooledHTableCount = this.config.getInt(HTABLE_POOL_MAX_SIZE, DEFAULT_HATBLE_POOL_MAX_SIZE);
    autoFlush = this.config.getBoolean(HTABLE_AUTOFLUSH, true);
    slowAccessCutoff = this.config.getInt(HBASE_SLOW_ACCESS_CUTOFF,
                                          DEFAULT_HBASE_SLOW_ACCESS_CUTOFF);
    scanCaching = this.config.getInt(HBASE_CLIENT_SCANNER_CACHING, 1);
    Map<String, HConnection> oldConnectionsMap = connections;
    connections = new ConcurrentHashMap<String, HConnection>();
    
    HTablePool oldHTablePool = hTablePool;
    hTablePool = new HTablePool(this.config, maxPooledHTableCount);
    HTablePoolStatistics.maxPooledHTableCount = maxPooledHTableCount;
    
    readEnabled = this.config.getBoolean(HBASE_READ_ENABLED, true);
    writeEnabled = this.config.getBoolean(HBASE_WRITE_ENABLED, true);
    adminEnabled = this.config.getBoolean(HBASE_ADMIN_ENABLED, true);
    aggregationClient = new AggregationClient(config);
    LOG.info(getConfigurationString());

    closeHTablePool(oldHTablePool);
    closeConnections(oldConnectionsMap);
  }
  
  protected HConnection createHConnection(Configuration conf, String clusterName) throws HException {
    try {
      if (clusterName == null) {
        return HConnectionManager.createConnection(conf);
      } else {
        return HConnectionManager.createConnection(conf, HBASE_URI_PREFIX + clusterName);
      }
    } catch (IOException e) {
      throw new HException("create HConnection fail", e);
    }
  }

  public Configuration getConfiguration() {
    return this.config;
  }
  
  public String getConfigurationString() {
    String configurationString = "HBaseClient Configuration:\n";
    if (this.config == null) {
      return configurationString;
    }

    for (int i = 0; i < HBASE_CLIENT_CONFIG_KEYS.length; ++i) {
      configurationString += (HBASE_CLIENT_CONFIG_KEYS[i] + "="
          + this.config.get(HBASE_CLIENT_CONFIG_KEYS[i]) + "\n");
    }
    return configurationString;
  }

  // load client configuration from configPath, which could be local
  // file path or zk path. When it's zk path, the parent zk node can
  // be loaded as the base configuration
  public static Configuration loadConfiguration(String configPath, boolean loadParentZkPath)
      throws HException {
    try {
      Configuration configInPath;
      if (HConfigUtil.isZkPath(configPath)) {
        configInPath = HConfigUtil.loadHBaseConfigFromZK(configPath, loadParentZkPath);
      } else if (HConfigUtil.isLocalFile(configPath)) {
        configInPath = HBaseConfiguration.create();
        configInPath.addResource(new Path(configPath));
      } else {
        throw new HException(
            "configPath format error, be local file format, file://var/.. or zk path format: zk://host:port/path, actual="
                + configPath);
      }
      return configInPath;
    } catch (Throwable e) {
      if (e instanceof HException) {
        throw (HException)e;
      } else {
        throw new HException(e);
      }
    }
  }

  public static Configuration loadConfiguration(String configPath) throws HException {
    return loadConfiguration(configPath, false);
  }

  public static String parsePerfcountTableName(String tableName) {
    if (!tableName.startsWith(HBASE_URI_PREFIX)) {
      return tableName;
    }
    tableName = tableName.substring(HBASE_URI_PREFIX.length());
    if (tableName.endsWith("/")) {
      tableName = tableName.substring(0, tableName.length() - 1);
    }
    tableName = tableName.replaceAll("/", "-");
    tableName = tableName.replace(":", "-");
    return tableName;
  }
  
  protected String constructClusterPerfcountName(String tableName) {
    return HBASE_PERFCOUNT_PRFEIX + HConfigUtil.getClusterName(clusterName, tableName);
  }
  
  protected String constructClusterFailPerfcountName(String tableName) {
    return constructClusterPerfcountName(tableName) + MultiCounter.FAIL_SUFFIX;
  }
  
  protected static String constructTablePerfcountName(String tableName) {
    return HBASE_PERFCOUNT_PRFEIX + parsePerfcountTableName(tableName);
  }

  protected static String constructTableFailPerfcountName(String tableName) {
    return constructTablePerfcountName(tableName) + MultiCounter.FAIL_SUFFIX;
  }
  
  protected static String constructMethodCallPerfcountName(String tableName, String methodName) {
    return constructTablePerfcountName(tableName) + MultiCounter.PATH_SEPARATOR_STRING + methodName;
  }

  protected static String constructMethodFailPerfcountName(String tableName, String methodName) {
    return constructMethodCallPerfcountName(tableName, methodName) + MultiCounter.FAIL_SUFFIX;
  }
  
  protected void logHBaseSlowAccess(String methodName, long timeConsume) {
    if (timeConsume >= this.slowAccessCutoff) {
      LOG.warn("hbase slow access, method=" + methodName + ", timeconsume=" + timeConsume);
    }
  }

  protected void checkReadEnabled() throws HException {
    ensureNotClosed();
    if (!readEnabled) {
      throw new OperationDisabledException("read");
    }
  }

  protected void checkWriteEnabled() throws HException {
    ensureNotClosed();
    if (!writeEnabled) {
      throw new OperationDisabledException("write");
    }
  }

  protected void checkAdminEnabled() throws HException {
    ensureNotClosed();
    if (!adminEnabled) {
      throw new OperationDisabledException("admin");
    }
  }
  
  protected void ensureNotClosed() throws HException {
    if (closed) {
      throw new HException("HBaseClient is closed, operation is disabled");
    }    
  }

  protected HTableInterface getHTable(String tableName) throws IOException {
    Pair<String, String> clusterAndTable = getClusterAndTableName(tableName);
    return getOrCreateConnection(clusterAndTable.getFirst(), config).getTable(
      clusterAndTable.getSecond());
  }
  
  public Result get(String tableName, Get get) throws HException {
    checkReadEnabled();
    long startTime = System.currentTimeMillis();
    HTableInterface hTable = null;
    try {
      hTable = getHTable(tableName);
      return hTable.get(get);
    } catch (Throwable e) {
      addFailCounter(tableName, "get", 1);
      throw new HException(e);
    } finally {
      try {
        closeHTable(hTable);
      } finally {
        long consumeInMs = (System.currentTimeMillis() - startTime);
        addCounter(tableName, "get", 1, consumeInMs);
        logHBaseSlowAccess("Get", consumeInMs);
      }
    }
  }
  
  public static void closeHTable(HTableInterface table) throws HException {
    if (table != null) {
      try {
        table.close();
      } catch (Throwable e) {
        throw new HException(e);
      }
    }
  }

  @Override
  public Result[] get(String tableName, List<Get> gets) throws HException {
    checkReadEnabled();
    long startTime = System.currentTimeMillis();
    HTableInterface hTable = null;
    try {
      hTable = getHTable(tableName);
      return hTable.get(gets);
    } catch (Throwable e) {
      addFailCounter(tableName, "batch-get", 1);
      throw new HException(e);
    } finally {
      try {
        closeHTable(hTable);
      } finally {
        long consumeInMs = (System.currentTimeMillis() - startTime);
        addCounter(tableName, "batch-get", 1, consumeInMs);
        logHBaseSlowAccess("batch-get", consumeInMs);
      }
    }
  }

  public void put(String tableName, Put put) throws HException {
    checkWriteEnabled();
    long startTime = System.currentTimeMillis();
    HTableInterface hTable = null;
    try {
      if (autoFlush) {
        hTable = getHTable(tableName);
      } else {
        hTable = hTablePool.getTable(getFullTableName(tableName));
      }
      hTable.put(put);
    } catch (Throwable e) {
      addFailCounter(tableName, "put", 1);
      throw new HException(e);
    } finally {
      try {
        if (autoFlush) {
          closeHTable(hTable);
        } else {
          hTablePool.returnTable(hTable);
        }
      } finally {
        long consumeInMs = (System.currentTimeMillis() - startTime);
        addCounter(tableName, "put", 1, consumeInMs);
        logHBaseSlowAccess("put", consumeInMs);
      }
    }
  }

  public void delete(String tableName, Delete delete) throws HException {
    checkWriteEnabled();
    long startTime = System.currentTimeMillis();
    HTableInterface hTable = null;
    try {
      hTable = getHTable(tableName);
      hTable.delete(delete);
    } catch (Throwable e) {
      addFailCounter(tableName, "delete", 1);
      throw new HException(e);
    } finally {
      try {
        closeHTable(hTable);
      } finally {
        long consumeInMs = (System.currentTimeMillis() - startTime);
        addCounter(tableName, "delete", 1, consumeInMs);
        logHBaseSlowAccess("delete", consumeInMs);
      }
    }
  }
  
  public void delete(String tableName, List<Delete> deletes) throws HException {
    checkWriteEnabled();
    long startTime = System.currentTimeMillis();
    HTableInterface hTable = null;
    try {
      hTable = getHTable(tableName);
      hTable.delete(deletes);
    } catch (Throwable e) {
      addFailCounter(tableName, "batch-delete", 1);
      throw new HException(e);
    } finally {
      try {
        closeHTable(hTable);
      } finally {
        long consumeInMs = (System.currentTimeMillis() - startTime);
        addCounter(tableName, "batch-delete", 1, consumeInMs);
        logHBaseSlowAccess("batch-delete", consumeInMs);
      }
    }
  }

  @Override
  public List<Integer> mutateRows(String tableName, List<Mutation> mutations,
      List<Condition> conditions) throws HException {
    checkWriteEnabled();

    long startTime = System.currentTimeMillis();
    HTableInterface hTable = null;
    try {
      final MultiRowMutationProtos.MutateRowsRequest.Builder requestBuilder =
          MultiRowMutationProtos.MutateRowsRequest.newBuilder();
      requestBuilder.addAllCondition(ProtobufUtil.toConditions(conditions));
      for (Mutation m : mutations) {
        if (m instanceof Delete) {
          requestBuilder.addMutationRequest(ProtobufUtil.toMutation(
              ClientProtos.MutationProto.MutationType.DELETE, m));
        } else if (m instanceof Put) {
          requestBuilder.addMutationRequest(ProtobufUtil.toMutation(
              ClientProtos.MutationProto.MutationType.PUT, m));
        } else {
          throw new DoNotRetryIOException("mutateRows supports only put and delete, not " +
              m.getClass().getName());
        }
      }
      hTable = getHTable(tableName);
      CoprocessorRpcChannel channel = hTable.coprocessorService(mutations.get(0).getRow());
      MultiRowMutationProtos.MultiRowMutationService.BlockingInterface service =
          MultiRowMutationProtos.MultiRowMutationService.newBlockingStub(channel);
      return service.mutateRows(null, requestBuilder.build()).getUnmetConditionsList();
    } catch (Throwable e) {
      addFailCounter(tableName, "mutate-rows", 1);
      throw new HException(e);
    } finally {
      try {
        closeHTable(hTable);
      } finally {
        long consumeInMs = (System.currentTimeMillis() - startTime);
        addCounter(tableName, "mutate-rows", 1, consumeInMs);
        logHBaseSlowAccess("mutate-rows", consumeInMs);
      }
    }
  }

  /**
   * Return max value of column. Returned column must be set to 'scan', the type of this column must
   * be 'Long'
   */
  @Override
  public Long max(String tableName, Scan scan) throws HException {
    checkReadEnabled();
    long startTime = System.currentTimeMillis();
    // now, hbase only provide LongColumnInterpreter implementing LongColumnInterpreter
    final ColumnInterpreter<Long, Long, EmptyMsg, LongMsg, LongMsg> ci = new LongColumnInterpreter();
    HTableInterface hTable = null;
    try {
      hTable = getHTable(tableName);
      return aggregationClient.max((HTable)hTable, ci, scan);
    } catch (Throwable e) {
      addFailCounter(tableName, "max", 1);
      throw new HException("max method of AggregationClient fail", e);
    } finally {
      try {
        closeHTable(hTable);
      } finally {
        long consumeInMs = (System.currentTimeMillis() - startTime);
        addCounter(tableName, "max", 1, consumeInMs);
        logHBaseSlowAccess("max", consumeInMs);
      }
    }
  }

  @Override
  public long rowCount(String tableName, final Scan scan) throws HException {
    checkReadEnabled();
    long startTime = System.currentTimeMillis();
    final ColumnInterpreter<Long, Long, EmptyMsg, LongMsg, LongMsg> ci = new LongColumnInterpreter();
    HTableInterface hTable = null;
    try {
      hTable = getHTable(tableName);
      return aggregationClient.rowCount((HTable)hTable, ci, scan);
    } catch (Throwable e) {
      addFailCounter(tableName, "rowCount", 1);
      throw new HException("rowCount method of AggregationClient fail", e);
    } finally {
      try {
        closeHTable(hTable);
      } finally {
        long consumeInMs = (System.currentTimeMillis() - startTime);
        addCounter(tableName, "rowCount", 1, consumeInMs);
        logHBaseSlowAccess("rowCount", consumeInMs);
      }
    }
  }
  
  public List<Result> scan(String tableName, Scan scan) throws HException {
    return scan(tableName, scan, Integer.MAX_VALUE);
  }
  
  public List<Result> scan(String tableName, Scan scan, int limit) throws HException {
    return scanInternal(tableName, scan, limit, null);
  }
  
  protected List<Result> scanInternal(String tableName, Scan scan, int limit,
      SaltedScanOptions options) throws HException {
    checkReadEnabled();
    // TODO : setSamll() automatically according to limit/caching, so that do small
    //        scanner more efficiently.
    long startTime = System.currentTimeMillis();
    List<Result> results = new ArrayList<Result>();
    HTableInterface hTable = null;
    ResultScanner scanner = null;
    String perfCountName = PERFORMCOUNT_SCAN;
    try {
      hTable = getHTable(tableName);
      // set caching to avoid fetching unnecessary data from server
      int userSetCaching = scan.getCaching() <= 0 ? scanCaching : scan.getCaching();
      if (userSetCaching > limit) {
        scan.setCaching(limit);
        scan.setSmall(true);
      }
      if (hTable instanceof SaltedHTable) {
        scanner = HClientScanner.createSaltedScanner((SaltedHTable)hTable, scan, options);
      } else {
        scanner = hTable.getScanner(scan);
      }
      Result result = null;
      while ((result = scanner.next()) != null) {
        results.add(result);
        if (results.size() == limit) {
          break;
        }
      }
      return results;
    } catch (Throwable e) {
      addFailCounter(tableName, perfCountName, 1);
      throw new HException(e);
    } finally {
      try {
        if (scanner != null) {
          scanner.close();
        }
        closeHTable(hTable);
      } finally {
        long consumeInMs = (System.currentTimeMillis() - startTime);
        addCounter(tableName, perfCountName, 1, consumeInMs);
        logHBaseSlowAccess(perfCountName, consumeInMs);
      }
    }    
  }

  public HClientScanner getScanner(String tableName, Scan scan) throws HException {
    return getScannerInternal(tableName, scan, null);
  }
  
  public static class SaltedScanOptions {
    public byte[][] slots = null;
    public boolean merged = true;
    public boolean keepSalted = false;

    public SaltedScanOptions() {}

    public SaltedScanOptions(byte[][] slots, boolean merged) {
      this.slots = slots;
      this.merged = merged;
    }
  }

  // the implementation logic is the same as HTable.getScanner(Scan scan)
  protected HClientScanner getScannerInternal(String tableName, Scan scan, SaltedScanOptions options)
      throws HException {
    checkReadEnabled();
    long startTime = System.currentTimeMillis();
    HTableInterface hTable = null;
    HClientScanner scanner = null;
    // extends PooledTable to support getScannerCaching() method
    /*
     * if (scan.getCaching() <= 0) { scan.setCaching(hTable.getScannerCaching()); }
     */

    try {
      hTable = getHTable(tableName);
      scanner = new HClientScanner(hTable.getConfiguration(), scan, hTable.getTableName(), options,
          hTable);
      return scanner;
    } catch (IOException e) {
      addFailCounter(tableName, "getScanner", 1);
      throw new HException(e);
    } finally {
      try {
        if (scanner == null) {
          closeHTable(hTable);
        }
      } finally {
        long consumeInMs = (System.currentTimeMillis() - startTime);
        addCounter(tableName, "getScanner", 1, consumeInMs);
        logHBaseSlowAccess("getScanner", consumeInMs);
      }
    }
  }

  @Override
  public <R> R coprocessorExec(String tableName, byte[] row, CoprocessorCall<R> call)
      throws HException {
    long startTime = System.currentTimeMillis();
    HTableInterface hTable = null;
    try {
      hTable = getHTable(tableName);
      CoprocessorRpcChannel channel = hTable.coprocessorService(row);
      return call.call(channel);
    } catch (Throwable e) {
      addFailCounter(tableName, "coprocessor-exec", 1);
      throw new HException(e);
    } finally {
      try {
        closeHTable(hTable);
      } finally {
        long consumeInMs = (System.currentTimeMillis() - startTime);
        addCounter(tableName, "coprocessor-exec", 1, consumeInMs);
        logHBaseSlowAccess("coprocessor-exec", consumeInMs);
      }
    }
  }

  @Override
  public <T extends Service, R> void coprocessorExec(String tableName,
      Class<T> protocol, byte[] startKey, byte[] endKey, Batch.Call<T, R> callable,
      Batch.Callback<R> callback) throws HException {
    checkReadEnabled();
    checkWriteEnabled();
    long startTime = System.currentTimeMillis();
    HTableInterface hTable = null;
    try {
      hTable = getHTable(tableName);
      hTable.coprocessorService(protocol, startKey, endKey, callable, callback);
    } catch (Throwable e) {
      addFailCounter(tableName, "coprocessor-exec", 1);
      throw new HException(e);
    } finally {
      try {
        closeHTable(hTable);
      } finally {
        long consumeInMs = (System.currentTimeMillis() - startTime);
        addCounter(tableName, "coprocessor-exec", 1, consumeInMs);
        logHBaseSlowAccess("coprocessor-exec", consumeInMs);
      }
    }
  }

  public class EndpointInvocationHandler<T extends Service>
      implements InvocationHandler
  {
    Class<T> protocol;
    String tableName;
    byte[] row;

    public EndpointInvocationHandler(Class<T> protocol, String tableName, byte[] row) {
      this.protocol = protocol;
      this.tableName = tableName;
      this.row = row;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      long startTime = System.currentTimeMillis();
      Object result;
      HTableInterface hTable = null;
      try {
        hTable = getHTable(tableName);
        T target = ProtobufUtil.newServiceStub(protocol, hTable.coprocessorService(row));
        result = method.invoke(target, args);
      } catch (Throwable e) {
        addFailCounter(tableName, method.getName(), 1);
        throw new HException(e);
      } finally {
        try {
          closeHTable(hTable);
        } finally {
          long consumeInMs = (System.currentTimeMillis() - startTime);
          addCounter(tableName, method.getName(), 1, consumeInMs);
          logHBaseSlowAccess(method.getName(), consumeInMs);
        }
      }
      return result;
    }
  }

  @Override
  public Pair<byte[][], byte[][]> getStartEndKeys(String tableName) throws HException {
    long startTime = System.currentTimeMillis();
    HTable hTable = null;
    try {
      hTable = new HTable(this.config, tableName);
      return hTable.getStartEndKeys();
    } catch (Throwable e) {
      addFailCounter(tableName, "getStartEndKeys", 1);
      throw new HException(e);
    } finally {
      try {
        if (hTable != null) {
          hTable.close();
        }
      } catch (Throwable e) {
        throw new HException(e);
      } finally {
        long consumeInMs = (System.currentTimeMillis() - startTime);
        addCounter(tableName, "getStartEndKeys", 1, consumeInMs);
        logHBaseSlowAccess("getStartEndKeys", consumeInMs);
      }
    }
  }

  public List<HRegionLocation> getRegionsInRange(String tableName, byte[] startKey,
      byte[] endKey) throws HException {
    long startTime = System.currentTimeMillis();
    HTable hTable = null;
    try {
      hTable = new HTable(this.config, tableName);
      return hTable.getRegionsInRange(startKey, endKey);
    } catch (Throwable e) {
      addFailCounter(tableName, "getRegionsInRange", 1);
      throw new HException(e);
    } finally {
      try {
        if (hTable != null) {
          hTable.close();
        }
      } catch (Throwable e) {
        throw new HException(e);
      } finally {
        long consumeInMs = (System.currentTimeMillis() - startTime);
        addCounter(tableName, "getRegionsInRange", 1, consumeInMs);
        logHBaseSlowAccess("getRegionsInRange", consumeInMs);
      }
    }
  }

  public void close() throws HException {
    if (closed) {
      return;
    }
    closeHTablePool(hTablePool);
    closeConnections(connections);
    closed = true;
  }
  
  public boolean checkAndPut(String tableName, final byte[] row, final byte[] family,
      final byte[] qualifier, final CompareOp compareOp, final byte[] value, final Put put)
      throws HException {
    checkWriteEnabled();
    long startTime = System.currentTimeMillis();
    HTableInterface hTable = null;
    try {
      hTable = getHTable(tableName);
      byte[] checkedQualifier = qualifier;
      if (checkedQualifier == null) {
        checkedQualifier = new byte[0];
      }
      RowMutations mutations = new RowMutations(row);
      mutations.add(put);
      return hTable.checkAndMutate(row, family, checkedQualifier, compareOp, value, mutations);
    } catch (Throwable e) {
      addFailCounter(tableName, "checkAndPutWithOperator", 1);
      throw new HException(e);
    } finally {
      try {
        closeHTable(hTable);
      } finally {
        long consumeInMs = (System.currentTimeMillis() - startTime);
        addCounter(tableName, "checkAndPutWithOperator", 1, consumeInMs);
        logHBaseSlowAccess("checkAndPutWithOperator", consumeInMs);
      }
    }
  }

  public boolean checkAndPut(String tableName, final byte[] row, final byte[] family,
      final byte[] qualifier, final byte[] value, final Put put) throws HException {
    checkWriteEnabled();
    long startTime = System.currentTimeMillis();
    HTableInterface hTable = null;
    try {
      hTable = getHTable(tableName);
      byte[] checkedQualifier = qualifier;
      if (checkedQualifier == null) {
        checkedQualifier = new byte[0];
      }
      return hTable.checkAndPut(row, family, checkedQualifier, value, put);
    } catch (Throwable e) {
      addFailCounter(tableName, "checkAndPut", 1);
      throw new HException(e);
    } finally {
      try {
        closeHTable(hTable);
      } finally {
        long consumeInMs = (System.currentTimeMillis() - startTime);
        addCounter(tableName, "checkAndPut", 1, consumeInMs);
        logHBaseSlowAccess("checkAndPut", consumeInMs);
      }
    }
  }
  
  public boolean checkNullAndPut(String tableName, final byte[] row, final byte[] family,
      final byte[] qualifier, final CompareOp compareOp, final byte[] value, final Put put) throws HException {
    checkWriteEnabled();
    long startTime = System.currentTimeMillis();
    HTableInterface hTable = null;
    try {
      hTable = getHTable(tableName);
      byte[] checkedQualifier = qualifier;
      if (checkedQualifier == null) {
        checkedQualifier = new byte[0];
      }
      if (hTable.checkAndPut(row, family, checkedQualifier, null, put)) {
        return true;
      }
      RowMutations mutations = new RowMutations(row);
      mutations.add(put);
      return hTable.checkAndMutate(row, family, checkedQualifier, compareOp, value, mutations);
    } catch (Throwable e) {
      addFailCounter(tableName, "checkNullAndPut", 1);
      throw new HException(e);
    } finally {
      try {
        closeHTable(hTable);
      } finally {
        long consumeInMs = (System.currentTimeMillis() - startTime);
        addCounter(tableName, "checkNullAndPut", 1, consumeInMs);
        logHBaseSlowAccess("checkNullAndPut", consumeInMs);
      }
    }
  }

  public boolean checkAndDelete(String tableName, byte[] row, byte[] family, byte[] qualifier,
      byte[] value, Delete delete) throws HException {
    checkWriteEnabled();
    long startTime = System.currentTimeMillis();
    HTableInterface hTable = null;
    try {
      hTable = getHTable(tableName);
      return hTable.checkAndDelete(row, family, qualifier, value, delete);
    } catch (Throwable e) {
      addFailCounter(tableName, "checkAndDelete", 1);
      throw new HException(e);
    } finally {
      try {
        closeHTable(hTable);
      } finally {
        long consumeInMs = (System.currentTimeMillis() - startTime);
        addCounter(tableName, "checkAndDelete", 1, consumeInMs);
        logHBaseSlowAccess("checkAndDelete", consumeInMs);
      }
    }
  }

  public boolean checkAndDelete(String tableName, byte[] row, byte[] family, byte[] qualifier,
      final CompareOp compareOp, byte[] value, Delete delete) throws HException {
    checkWriteEnabled();
    long startTime = System.currentTimeMillis();
    HTableInterface hTable = null;
    try {
      hTable = getHTable(tableName);
      RowMutations mutations = new RowMutations(row);
      mutations.add(delete);
      return hTable.checkAndMutate(row, family, qualifier, compareOp, value, mutations);
    } catch (Throwable e) {
      addFailCounter(tableName, "checkAndDelete", 1);
      throw new HException(e);
    } finally {
      try {
        closeHTable(hTable);
      } finally {
        long consumeInMs = (System.currentTimeMillis() - startTime);
        addCounter(tableName, "checkAndDelete", 1, consumeInMs);
        logHBaseSlowAccess("checkAndDelete", consumeInMs);
      }
    }
  }

  public void put(String tableName, final List<Put> puts) throws HException {
    checkWriteEnabled();
    long startTime = System.currentTimeMillis();
    HTableInterface hTable = null;
    try {
      if (autoFlush) {
        hTable = getHTable(tableName);
      } else {
        hTable = hTablePool.getTable(getFullTableName(tableName));
      }
      hTable.put(puts);
    } catch (Throwable e) {
      addFailCounter(tableName, "batch-put", 1);
      throw new HException(e);
    } finally {
      try {
        if (autoFlush) {
          closeHTable(hTable);
        } else {
          hTablePool.returnTable(hTable);
        }
      } finally {
        long consumeInMs = (System.currentTimeMillis() - startTime);
        addCounter(tableName, "batch-put", 1, consumeInMs);
        logHBaseSlowAccess("batch-put", consumeInMs);
      }
    }
  }
 
  /**
   * if a action failed, the batch will throw an exception
   */
  @Override
  public Object[] batch(String tableName, final List<? extends Row> actions) throws HException {
    Object[] results = new Object[actions.size()];
    batch(tableName, actions, results);    
    for (Object object : results) {
      if (object == null || object instanceof Throwable) {
        throw new HException("One Action failed");
      }
    }
    return results;
  }
  
  public void batch(String tableName, final List<? extends Row> actions, final Object[] results) throws HException {
    if (!readEnabled || !writeEnabled) {
      for (Row row : actions) {
        if (row instanceof Get) {
          checkReadEnabled();
        }
        if (row instanceof Mutation) {
          checkWriteEnabled();
        }
      }
    }

    long startTime = System.currentTimeMillis();
    HTableInterface hTable = null;
    try {
      hTable = getHTable(tableName);
      hTable.batch(actions, results);
    } catch (Throwable e) {
      addFailCounter(tableName, "batch", 1);
      throw new HException(e);
    } finally {
      try {
        closeHTable(hTable);
      } finally {
        long consumeInMs = (System.currentTimeMillis() - startTime);
        addCounter(tableName, "batch", 1, consumeInMs);
        logHBaseSlowAccess("batch", consumeInMs);
      }
    }
  }
  
  @Override
  public BulkDeleteResponse bulkDelete(String tableName, final Scan scan, final DeleteType deleteType,
      final Long timestamp, final int rowBatchSize) throws HException {
    checkWriteEnabled();
    
    long startTime = System.currentTimeMillis();
    HTableInterface hTable = null;
    try {
      hTable = getHTable(tableName);
      Batch.Call<BulkDeleteService, BulkDeleteResponse> callable =
          new Batch.Call<BulkDeleteService, BulkDeleteResponse>() {
          ServerRpcController controller = new ServerRpcController();
          BlockingRpcCallback<BulkDeleteResponse> rpcCallback =
            new BlockingRpcCallback<BulkDeleteResponse>();

          public BulkDeleteResponse call(BulkDeleteService service) throws IOException {
            Builder builder = BulkDeleteRequest.newBuilder();
            builder.setScan(ProtobufUtil.toScan(scan));
            builder.setDeleteType(deleteType);
            builder.setRowBatchSize(rowBatchSize);
            if (timestamp != null) {
              builder.setTimestamp(timestamp);
            }
            service.delete(controller, builder.build(), rpcCallback);
            return rpcCallback.get();
          }
        };
        Map<byte[], BulkDeleteResponse> resultByRegion = hTable.coprocessorService(BulkDeleteService.class, scan
            .getStartRow(), scan.getStopRow(), callable);
      long rowsDeletedCnt = 0;
      long versionsDeletedCnt = 0;
      for (BulkDeleteResponse response : resultByRegion.values()) {
        rowsDeletedCnt += response.getRowsDeleted();
        versionsDeletedCnt += response.getVersionsDeleted();
      }
      org.apache.hadoop.hbase.coprocessor.example.generated.BulkDeleteProtos.BulkDeleteResponse.Builder responseBuilder = BulkDeleteResponse
          .newBuilder();
      responseBuilder.setRowsDeleted(rowsDeletedCnt);
      responseBuilder.setVersionsDeleted(versionsDeletedCnt);
      return responseBuilder.build();
    } catch (Throwable e) {
      addFailCounter(tableName, "bulk-delete", 1);
      throw new HException(e);
    } finally {
      try {
        closeHTable(hTable);
      } finally {
        long consumeInMs = (System.currentTimeMillis() - startTime);
        addCounter(tableName, "bulk-delete", 1, consumeInMs);
        logHBaseSlowAccess("bulk-delete", consumeInMs);
      }
    }
  }

  @Override
  public Result increment(String tableName, final Increment increment) throws HException {
    checkWriteEnabled();
    long startTime = System.currentTimeMillis();
    HTableInterface hTable = null;
    try {
      hTable = getHTable(tableName);
      return hTable.increment(increment);
    } catch (Throwable e) {
      addFailCounter(tableName, "increment", 1);
      throw new HException(e);
    } finally {
      try {
        closeHTable(hTable);
      } finally {
        long consumeInMs = (System.currentTimeMillis() - startTime);
        addCounter(tableName, "increment", 1, consumeInMs);
        logHBaseSlowAccess("increment", consumeInMs);
      }
    }
  }
  
  @Override
  public long incrementColumnValue(String tableName, final byte[] row, final byte[] family,
      final byte[] qualifier, final long amount) throws HException {
    checkWriteEnabled();
    long startTime = System.currentTimeMillis();
    HTableInterface hTable = null;
    try {
      hTable = getHTable(tableName);
      byte[] checkedQualifier = qualifier;
      if (checkedQualifier == null) {
        checkedQualifier = new byte[0];
      }
      return hTable.incrementColumnValue(row, family, checkedQualifier, amount);
    } catch (Throwable e) {
      addFailCounter(tableName, "incrementColumnValue", 1);
      throw new HException(e);
    } finally {
      try {
        closeHTable(hTable);
      } finally {
        long consumeInMs = (System.currentTimeMillis() - startTime);
        addCounter(tableName, "incrementColumnValue", 1, consumeInMs);
        logHBaseSlowAccess("increment", consumeInMs);
      }
    }
  }

  abstract class AdminCallable<R> {
    protected HBaseAdmin hbaseAdmin;
    
    public AdminCallable(String cluster) throws HException {
      cluster = cluster == null ? clusterName : cluster;
      try {
        hbaseAdmin = createHBaseAdmin(cluster);
      } catch (Throwable e) {
        throw new HException(e);
      }
    }
    
    public abstract R call() throws Throwable;
    public R run() throws HException {
      try {
        return this.call();
      } catch (Throwable e) {
        throw new HException(e);
      } finally {
        try {
          if (hbaseAdmin != null) {
            hbaseAdmin.close();
          }
        } catch (Throwable e) {
          throw new HException(e);
        }
      }
    }
  }

  public void createTable(final HTableDescriptor desc) throws HException {
    createTable(null, desc);
  }

  public void createTable(final String clusterName, final HTableDescriptor desc) throws HException {
    checkAdminEnabled();
    new AdminCallable<Void>(clusterName) {
      @Override
      public Void call() throws Throwable {
        this.hbaseAdmin.createTable(desc);
        return null;
      }
    }.run();
  }

  public void createTable(final HTableDescriptor desc, final byte[] startKey, final byte[] endKey,
      final int numRegions) throws HException {
    createTable(null, desc, startKey, endKey, numRegions);
  }
  
  public void createTable(final String clusterName, final HTableDescriptor desc,
      final byte[] startKey, final byte[] endKey, final int numRegions) throws HException {
    checkAdminEnabled();
    new AdminCallable<Void>(clusterName) {
      @Override
      public Void call() throws Throwable {
        this.hbaseAdmin.createTable(desc, startKey, endKey, numRegions);
        return null;
      }
    }.run();
  }
  
  public void createTable(final HTableDescriptor desc, final byte [][] splitKeys) throws HException {
    createTable(null, desc, splitKeys);
  }
  
  public void createTable(final String clusterName, final HTableDescriptor desc,
      final byte[][] splitKeys) throws HException {
    checkAdminEnabled();
    new AdminCallable<Void>(clusterName) {
      @Override
      public Void call() throws Throwable {
        this.hbaseAdmin.createTable(desc, splitKeys);
        return null;
      }
    }.run();    
  }
  
  public void disableTable(final String tableName) throws HException {
    disableTable(null, tableName);
  }
  
  public void disableTable(final String clusterName, final String tableName) throws HException {
    checkAdminEnabled();
    new AdminCallable<Void>(clusterName) {
      @Override
      public Void call() throws Throwable {
        this.hbaseAdmin.disableTable(tableName);
        return null;
      }
    }.run();
  }
  
  public void deleteTable(final String tableName) throws HException {
    deleteTable(null, tableName);
  }
  
  public void deleteTable(final String clusterName, final String tableName) throws HException {
    checkAdminEnabled();
    new AdminCallable<Void>(clusterName) {
      @Override
      public Void call() throws Throwable {
        this.hbaseAdmin.deleteTable(tableName);
        return null;
      }
    }.run();    
  }
  
  public void enableTable(final String tableName) throws HException {
    enableTable(null, tableName);
  }
  
  public void enableTable(final String clusterName, final String tableName) throws HException {
    checkAdminEnabled();
    new AdminCallable<Void>(clusterName) {
      @Override
      public Void call() throws Throwable {
        this.hbaseAdmin.enableTable(tableName);
        return null;
      }
    }.run();    
  }

  public void snapshot(final String snapshotName, final String tableName,
      final HBaseProtos.SnapshotDescription.Type type) throws HException {
    snapshot(null, snapshotName, tableName, type);
  }

  public void snapshot(final String clusterName, final String snapshotName, final String tableName,
      final HBaseProtos.SnapshotDescription.Type type) throws HException {
    checkAdminEnabled();
    new AdminCallable<Void>(clusterName) {
      @Override
      public Void call() throws Throwable {
        this.hbaseAdmin.snapshot(snapshotName, tableName, type);
        return null;
      }
    }.run();
  }

  public void deleteSnapshot(final String snapshotName) throws HException {
    deleteSnapshot(null, snapshotName);
  }

  public void deleteSnapshot(final String clusterName, final String snapshotName)
      throws HException {
    checkAdminEnabled();
    new AdminCallable<Void>(clusterName) {
      @Override
      public Void call() throws Throwable {
        this.hbaseAdmin.deleteSnapshot(snapshotName);
        return null;
      }
    }.run();
  }

  public void listSnapshots() throws HException {
    listSnapshots(null);
  }

  public void listSnapshots(final String clusterName) throws HException {
    checkAdminEnabled();
    new AdminCallable<Void>(clusterName) {
      @Override
      public Void call() throws Throwable {
        this.hbaseAdmin.listSnapshots();
        return null;
      }
    }.run();
  }

  public void cloneSnapshot(final String snapshotName, final String tableName) throws HException {
    cloneSnapshot(null, snapshotName, tableName);
  }

  public void cloneSnapshot(final String clusterName, final String snapshotName,
      final String tableName) throws HException {
    checkAdminEnabled();
    new AdminCallable<Void>(clusterName) {
      @Override
      public Void call() throws Throwable {
        this.hbaseAdmin.cloneSnapshot(snapshotName, tableName);
        return null;
      }
    }.run();
  }

  public void modifyTable(final String tableName, final HTableDescriptor desc) throws HException {
    modifyTable(null, tableName, desc);
  }

  public void modifyTable(final String clusterName, final String tableName,
      final HTableDescriptor desc) throws HException {
    checkAdminEnabled();
    new AdminCallable<Void>(clusterName) {
      @Override
      public Void call() throws Throwable {
        this.hbaseAdmin.modifyTable(Bytes.toBytes(tableName), desc);
        return null;
      }
    }.run();
  }

  public HTableDescriptor getTableDescriptor(final String tableName) throws HException {
    return getTableDescriptor(null, tableName);
  }
  
  public HTableDescriptor getTableDescriptor(final String clusterName, final String tableName)
      throws HException {
    checkAdminEnabled();
    return new AdminCallable<HTableDescriptor>(clusterName) {
      @Override
      public HTableDescriptor call() throws Throwable {
        return this.hbaseAdmin.getTableDescriptor(Bytes.toBytes(tableName));
      }
    }.run();    
  }
  
  public boolean isTableEnabled(final String tableName) throws HException {
    return isTableEnabled(null, tableName);
  }
  
  public boolean isTableEnabled(final String clusterName, final String tableName) throws HException {
    checkAdminEnabled();
    return new AdminCallable<Boolean>(clusterName) {
      @Override
      public Boolean call() throws Throwable {
        return this.hbaseAdmin.isTableEnabled(Bytes.toBytes(tableName));
      }
    }.run();    
  }
  
  public boolean isTableDisabled(final String tableName) throws HException {
    return isTableDisabled(null, tableName);
  }
  
  public boolean isTableDisabled(final String clusterName, final String tableName) throws HException {
    checkAdminEnabled();
    return new AdminCallable<Boolean>(clusterName) {
      @Override
      public Boolean call() throws Throwable {
        return this.hbaseAdmin.isTableDisabled(Bytes.toBytes(tableName));
      }
    }.run();
  }
  
  public boolean tableExists(final String tableName) throws HException {
    return tableExists(null, tableName);
  }
  
  public boolean tableExists(final String clusterName, final String tableName) throws HException {
    checkAdminEnabled();
    return new AdminCallable<Boolean>(clusterName) {
      @Override
      public Boolean call() throws Throwable {
        return this.hbaseAdmin.tableExists(tableName);
      }
    }.run();
  }
  
  private void addCounter(String tableName, String method, int count, long time) {
    PerfCounter.count(HBASE, count);
    PerfCounter.count(constructClusterPerfcountName(tableName), count);
    PerfCounter.count(constructTablePerfcountName(tableName), count);
    PerfCounter.count(constructMethodCallPerfcountName(tableName, method), count, time);
  }
  
  private void addFailCounter(String tableName, String method, int count) {
    PerfCounter.count(HBASE_FAIL, count);
    PerfCounter.count(constructClusterFailPerfcountName(tableName), count);
    PerfCounter.count(constructTableFailPerfcountName(tableName), count);
    PerfCounter.count(constructMethodFailPerfcountName(tableName, method), count);
  }

  protected HBaseAdmin createHBaseAdmin(String clusterName) throws IOException, HException {
    return new HBaseAdmin(getOrCreateConnection(clusterName, config));
  }
  
  protected ReplicationAdmin createReplicationAdmin(String clusterName) throws IOException {
    HConnection connection = getOrCreateConnection(clusterName, config);
    Configuration repConf = new Configuration(connection.getConfiguration());
    repConf.setBoolean(HConstants.REPLICATION_ENABLE_KEY, true);
    return new ReplicationAdmin(repConf);
  }
  
  // replications for different tableCFs will share the same peerId which enable only one replication thread
  protected String getGlobalPeerId() {
    return config.get(REPLICATION_ID_KEY, DEFAULT_REPLICATION_ID_KEY);
  }
  
  @Override
  public HClientScanner getScanner(String tableName, Scan scan, byte[][] slots, boolean merged)
      throws HException {
    return getScannerInternal(tableName, scan, new SaltedScanOptions(slots, merged));
  }

  @Override
  public List<Result> scan(String tableName, Scan scan, byte[][] slots, boolean merged)
      throws HException {
    return scan(tableName, scan, Integer.MAX_VALUE, slots, merged);
  }

  @Override
  public List<Result> scan(String tableName, Scan scan, int limit, byte[][] slots, boolean merged)
      throws HException {
    return scanInternal(tableName, scan, limit, new SaltedScanOptions(slots, merged));
  }
  
  protected KeySalter getKeySalter(String tableName) throws HException {
    HTableInterface hTable = null;
    try {
      hTable = getHTable(tableName);
      return SaltedHTable.getKeySalter(hTable);
    } catch (Throwable e) {
      if (e instanceof HException) {
        throw (HException)e;
      }
      throw new HException(e);
    } finally {
      closeHTable(hTable);
    }
  }
  
  protected String getClusterName() {
    return this.clusterName;
  }
  
  public void appendReplicationTableCFs(String masterCluster, String peerCluster, String tableName,
      Set<String> families) throws HException {
    appendReplicationTableCFsForPeer(masterCluster, getGlobalPeerId(), tableName, families);
  }

  public void appendReplicationTableCFsForPeer(String masterCluster, String peerId,
      String tableName, Set<String> families) throws HException {
    checkAdminEnabled();
    ReplicationAdmin replicationAdmin = null;
    try {
      replicationAdmin = createReplicationAdmin(masterCluster);
      Map<TableName, Set<String>> tableCFs = new HashMap<TableName, Set<String>>();
      tableCFs.put(TableName.valueOf(tableName), families);
      replicationAdmin.appendPeerTableCFs(peerId, tableCFs);
    } catch (Throwable e) {
      throw new HException(e);
    } finally {
      try {
        if (replicationAdmin != null) {
          replicationAdmin.close();
        }
      } catch (Throwable e) {
        throw new HException(e);
      }
    }
  }
  
  public void removeReplicationTableCFs(String masterCluster, String peerCluster, String tableName,
      Set<String> families) throws HException {
    removeReplicationTableCFs(masterCluster, getGlobalPeerId(), tableName, families);
  }
  
  public void removeReplicationTableCFsForPeer(String masterCluster, String peerId,
      String tableName, Set<String> families) throws HException {
    checkAdminEnabled();
    ReplicationAdmin replicationAdmin = null;
    try {
      replicationAdmin = createReplicationAdmin(masterCluster);
      Map<TableName, List<String>> tableCFs = replicationAdmin.getPeerTableCFs(peerId);
      TableName tName = TableName.valueOf(tableName);
      if (tableCFs.containsKey(tName)) {
        if (families != null) {
          List<String> cfs = tableCFs.get(tName);
          if (cfs == null) {
            LOG.warn("can not remove table cfs for peer=" + peerId
                + ", because no tableCFs existed, but required tableCFs");
            return;
          }
          List<String> resultCFs = new ArrayList<String>();
          for (String cf : cfs) {
            if (!families.contains(cf)) {
              resultCFs.add(cf);
            }
          }
          tableCFs.put(tName, resultCFs);
        } else {
          tableCFs.remove(tName);
        }
        replicationAdmin.setPeerTableCFs(peerId, tableCFs);
      }
    } catch (Throwable e) {
      throw new HException(e);
    } finally {
      try {
        if (replicationAdmin != null) {
          replicationAdmin.close();
        }
      } catch (Throwable e) {
        throw new HException(e);
      }
    }
  }
}
