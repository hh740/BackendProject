package com.xiaomi.infra.hbase.client.async;

import com.xiaomi.infra.hbase.client.HBaseClientInterface;
import com.xiaomi.infra.hbase.client.HException;
import com.xiaomi.infra.hbase.client.proxy.InvocationHandlerBase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.util.Threads;

import java.lang.reflect.Method;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AsyncInvocationHandler extends InvocationHandlerBase {
  private static final Log LOG = LogFactory.getLog(AsyncInvocationHandler.class);
  private final HBaseClientInterface client;
  
  public AsyncInvocationHandler(HBaseClientInterface client) {
    this.client = client;
    Configuration config = this.client.getConfiguration();
    threadPoolCoreSize = config.getInt(HBASE_CLIENT_HANDLER_THREAD_POOL_CORE_SIZE, 1);
    threadPoolMaxSize = config.getInt(HBASE_CLIENT_HANDLER_THREAD_POOL_MAX_SIZE, 10);
    maxQueuedRequestCount = config.getInt(HBASE_CLIENT_MAX_QUEUED_REQUEST_COUNT,
      DEFAULT_HBASE_CLIENT_MAX_CONCURRENT_REQUEST_COUNT);
    requestQueue = new ArrayBlockingQueue<Runnable>(maxQueuedRequestCount);
    this.executor = new ThreadPoolExecutor(threadPoolCoreSize, threadPoolMaxSize,
        10, TimeUnit.SECONDS, requestQueue,
        Threads.newDaemonThreadFactory("hbase-client-request-worker"),
        new HBaseRequestQueueRejectedHandler());
    this.requestTimeout = config.getInt(HBASE_CLIENT_REQUEST_TIMEOUT,
      DFEAULT_HBASE_CLIENT_REQUEST_TIMEOUT);
    LOG.info("create asyncInvocationHandler, threadPoolCoreSize=" + threadPoolCoreSize
        + ", threadPoolMaxSize=" + threadPoolMaxSize + ", maxQueuedRequestCount="
        + maxQueuedRequestCount + ", requestTimeout=" + requestTimeout);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Future<Object> result = null;
    try {
      result = this.executor.submit(new HBaseRequest(this.client, method, args));
      return result.get(requestTimeout, TimeUnit.MILLISECONDS);
    } catch (Throwable e) {
      throw new HException(e);
    }
  }

  class HBaseRequestQueueRejectedHandler implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
      LOG.error("HBaseRequest is rejected, threadPoolShutdown=" + executor.isShutdown()
          + ", threadPoolTerminating=" + executor.isTerminating() + ", threadPoolTerminated="
          + executor.isTerminated() + "maxQueuedRequestCount=" + maxQueuedRequestCount
          + ", queuedRequestCount=" + executor.getActiveCount());
      throw new RejectedExecutionException("HBaseRequest is rejected");
    }
  }
    
  class HBaseRequest implements Callable<Object> {
    protected Object object;
    protected Method method;
    protected Object[] args;
    
    public HBaseRequest(Object object, Method method, Object[] args) {
      this.object = object;
      this.method = method;
      this.args = args;
    }

    @Override
    public Object call() throws Exception {
      return method.invoke(object, args);
    }
  }
}