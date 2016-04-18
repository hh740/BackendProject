package com.xiaomi.infra.hbase.client.duplication;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.util.Threads;

import com.xiaomi.common.perfcounter.PerfCounter;
import com.xiaomi.infra.hbase.client.HBaseClientInterface;
import com.xiaomi.infra.hbase.client.HException;
import com.xiaomi.infra.hbase.client.InternalHBaseClient;
import com.xiaomi.infra.hbase.client.proxy.InvocationHandlerBase;
import com.xiaomi.miliao.common.ConcurrentHashSet;

/**
 * This is only for MiCloud cluster failover network test, don't rely on this for functional
 * double read/write
 */
public class DuplicationInvocationHandler extends InvocationHandlerBase {
  public static final String HBASE_CLIENT_DUP_CLUSTER_KEY = "hbase.client.duplication.cluster.name";
  public static final String HBASE_CLIENT_DUP_RATIO_KEY = "hbase.client.duplication.ratio";
  public static final String HBASE_CLIENT_DUP_THREADS_KEY = "hbase.client.duplication.threads";
  public static final String DUP_REQ_PERFCOUNTER_NAME = "hbase-client-duplicationRequest";
  public static final String DUP_REJ_PERFCOUNTER_NAME = "hbase-client-duplicationRequestRejected";
  public static final String DUP_LAT_PERFCOUNTER_PREFIX = "hbase-client-duplicationRequestLatency-";
  private static final Log LOG = LogFactory.getLog(DuplicationInvocationHandler.class);
  private final HBaseClientInterface clientImpl;
  private final ThreadLocal<Random> rand = new ThreadLocal<Random>() {
    @Override protected Random initialValue() {
      return new Random();
    }
  };

  private final InternalHBaseClient dupClient;
  private volatile Configuration conf;
  private volatile String dupClusterName;
  private volatile float dupRatio;

  private final ConcurrentMap<Method, Integer> supported = new ConcurrentHashMap<Method, Integer>();
  private final ConcurrentHashSet<Method> unsupported = new ConcurrentHashSet<Method>();

  public DuplicationInvocationHandler(HBaseClientInterface impl) throws HException {
    maxQueuedRequestCount = impl.getConfiguration().getInt(HBASE_CLIENT_MAX_QUEUED_REQUEST_COUNT,
                                                           DEFAULT_HBASE_CLIENT_MAX_CONCURRENT_REQUEST_COUNT);
    int dupThreads = impl.getConfiguration().getInt(HBASE_CLIENT_DUP_THREADS_KEY, 10);
    requestQueue = new ArrayBlockingQueue<Runnable>(maxQueuedRequestCount);
    this.executor = new ThreadPoolExecutor(1, dupThreads, 10, TimeUnit.SECONDS, requestQueue,
        Threads.newDaemonThreadFactory("hbase-client-duplication-request-worker"),
        new DuplicationRequestRejectedHandler());
    LOG.info("create request duplication invocation handler, cluster=" + dupClusterName +
                 ", maxQueueLength=" + maxQueuedRequestCount
                 + ", ratio=" + dupRatio);
    dupClient = new InternalHBaseClient(impl.getConfiguration());
    this.clientImpl = impl;
    reload(impl.getConfiguration());
  }

  protected void reload(Configuration conf) throws HException {
    if (this.conf != conf) {
      // reload conf
      synchronized (dupClient) {
        this.conf = conf;
        dupClient.reload(conf);
        dupClusterName = conf.get(HBASE_CLIENT_DUP_CLUSTER_KEY);
        if (dupClusterName != null &&
            dupClusterName.equals(conf.get(InternalHBaseClient.HBASE_CLUSTER_NAME))) {
          dupClusterName = null; // disable duplicated operation
          LOG.warn("Disable operation duplication since " +
                       HBASE_CLIENT_DUP_CLUSTER_KEY + "  equals to " +
                       InternalHBaseClient.HBASE_CLUSTER_NAME);
        }
        dupRatio = conf.getFloat(HBASE_CLIENT_DUP_RATIO_KEY, -1);
      }
    }
  }

  protected int duplicationSupport(Method method) {
    if (unsupported.contains(method)) {
      return -1;
    }
    Integer index = supported.get(method);
    if (index != null) {
      return index;
    }
    DuplicationSupport support = method.getAnnotation(DuplicationSupport.class);
    if (support == null) {
      unsupported.add(method);
      return -1;
    } else {
      supported.put(method, support.tableNameParamIndex());
      return support.tableNameParamIndex();
    }
  }

  protected void duplicateRequest(final Method method, final Object[] args) throws HException {
    int tableNameParamIndex = duplicationSupport(method);
    if (tableNameParamIndex < 0) {
      return;
    }
    reload(this.clientImpl.getConfiguration());
    if (this.dupClusterName == null || rand.get().nextDouble() > dupRatio) {
      return;
    }

    final Object[] newArgs = new Object[args.length];
    System.arraycopy(args, 0, newArgs, 0, args.length);
    
    // TODO : implement this?
//    newArgs[tableNameParamIndex] =
//        HConfigUtil.constructFullTableName(dupClusterName, (String) args[tableNameParamIndex]);

    try {
      executor.submit(new Callable<Object>() {
        @Override public Object call() throws Exception {
          long startTs = System.currentTimeMillis();
          Object result = method.invoke(dupClient, newArgs);
          long latency = System.currentTimeMillis() - startTs;
          PerfCounter.countDuration(DUP_LAT_PERFCOUNTER_PREFIX + method.getName(), latency);
          return result;
        }
      });
      PerfCounter.count(DUP_REQ_PERFCOUNTER_NAME, 1);
    } catch (Throwable t) {
      if (rand.get().nextDouble() < 0.0001) {
        LOG.info(t.getMessage() + ", current queue length=" + requestQueue.size());
      }
    }
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      duplicateRequest(method, args);
      return method.invoke(this.clientImpl, args);
    } catch (InvocationTargetException e) {
      if(e.getCause() instanceof HException) {
        throw e.getCause();
      } else {
        throw new HException(e);
      }
    } catch (Throwable e) {
      throw new HException(e);
    }
  }

  class DuplicationRequestRejectedHandler implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
      PerfCounter.count(DUP_REJ_PERFCOUNTER_NAME, 1);
      throw new RejectedExecutionException("duplication request is rejected");
    }
  }
}
