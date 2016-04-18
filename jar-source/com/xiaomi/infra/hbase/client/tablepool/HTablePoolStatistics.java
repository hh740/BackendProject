package com.xiaomi.infra.hbase.client.tablepool;

/**
 * <p>Statistcs of HBaseClient, including:
 * <p>1. statistics of HTablePool
 * @author cuijianwei
 *
 */
public class HTablePoolStatistics {
  public static int maxPooledHTableCount;
  public int concurrentUsingHTableCount;
  public int pooledHTableCount;

  public HTablePoolStatistics(int concurrentUsingHTableCount, int pooledHTableCount) {
    this.concurrentUsingHTableCount = concurrentUsingHTableCount;
    this.pooledHTableCount = pooledHTableCount;
  }
}

