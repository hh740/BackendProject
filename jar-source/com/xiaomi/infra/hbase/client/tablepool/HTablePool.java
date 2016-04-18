package com.xiaomi.infra.hbase.client.tablepool;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.util.Bytes;

import com.xiaomi.infra.hbase.client.HException;

/**
 * <p>Wrapped HTablePool based org.apache.hadoop.hbase.client.HTablePool.
 * <p>The extra adding function including:
 * <p>1. add concurrentUsingTableCounts statistics
 * @author cuijianwei
 *
 */
public class HTablePool extends org.apache.hadoop.hbase.client.HTablePool {
  // TODO(cuijianwei): integrate concurrent using table counts into HTablePool
  private ConcurrentHashMap<String, AtomicInteger> concurrentUsingTableCounts = new ConcurrentHashMap<String, AtomicInteger>();

  public HTablePool(final Configuration config, final int maxSize) {
    super(config, maxSize, new HTableFactory());
  }

  private void increaseConcurrentUsingTableCount(String tableName) {
    AtomicInteger initCount = new AtomicInteger(0);
    AtomicInteger previousCount = null;
    if ((previousCount = concurrentUsingTableCounts.putIfAbsent(tableName, initCount)) == null) {
      // no count associate with tableName
      initCount.incrementAndGet();
    } else {
      previousCount.incrementAndGet();
    }
  }

  public HTableInterface getTable(String tableName) {
    HTableInterface hTable = super.getTable(tableName);
    increaseConcurrentUsingTableCount(tableName);
    return hTable;
  }
  
  public void returnTable(HTableInterface hTable) throws HException {
    try {
      if (hTable != null) {
        hTable.close();
        AtomicInteger counter = concurrentUsingTableCounts.get(Bytes.toString(hTable
            .getFullTableName()));
        if (counter != null) {
          counter.decrementAndGet();
        }
      }
    } catch (IOException e) {
      throw new HException(e);
    }
  }
  
  public int getConcurrentUsingTableCount(String tableName) {
    AtomicInteger value = concurrentUsingTableCounts.get(tableName);
    return value == null ? 0 : value.get();
  }
  
  public HTablePoolStatistics getStatistics(String tableName) {
    return new HTablePoolStatistics(getConcurrentUsingTableCount(tableName),
      getCurrentPoolSize(tableName));
  }
}
