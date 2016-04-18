package com.xiaomi.infra.hbase.client.proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationHandler;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class InvocationHandlerBase implements InvocationHandler {
  public static final String HBASE_CLIENT_HANDLER_THREAD_POOL_CORE_SIZE = "hbase.client.handler.threadpool.coresize";
  public static final String HBASE_CLIENT_HANDLER_THREAD_POOL_MAX_SIZE = "hbase.client.handler.threadpool.maxsize";
  public static final String HBASE_CLIENT_MAX_QUEUED_REQUEST_COUNT = "hbase.client.max.queued.request.count";
  public static final String HBASE_CLIENT_REQUEST_TIMEOUT = "hbase.client.request.timeout";
  private static final Log LOG = LogFactory.getLog(InvocationHandlerBase.class);
  public static final int DEFAULT_HBASE_CLIENT_MAX_CONCURRENT_REQUEST_COUNT = 10000;
  public static final int DFEAULT_HBASE_CLIENT_REQUEST_TIMEOUT = 5000;
  protected ThreadPoolExecutor executor;
  protected ArrayBlockingQueue<Runnable> requestQueue;
  protected int threadPoolCoreSize;
  protected int threadPoolMaxSize;
  protected int maxQueuedRequestCount;
  protected int requestTimeout;
  
  public void close(boolean closeGracefully) {
    LOG.info("Start to close HBaseInvocatioHandler, closeGracefully=" + closeGracefully);
    if (this.executor != null) {
      if (closeGracefully) {
        // disable new tasks from being submitted
        this.executor.shutdown();
        
        try {
          // wait the current running task completed
          int waitTime = requestTimeout == 0 ? DFEAULT_HBASE_CLIENT_REQUEST_TIMEOUT
              : requestTimeout;
          if (this.executor.awaitTermination(waitTime, TimeUnit.MILLISECONDS)) {
            this.executor.shutdownNow();
          }
        } catch (InterruptedException e) {
          LOG.error("Exception in gracefully close", e);
        }
      } else {
        this.executor.shutdownNow();        
      }
    }
  }
  
  public void close() {
    close(true);
  }
}
