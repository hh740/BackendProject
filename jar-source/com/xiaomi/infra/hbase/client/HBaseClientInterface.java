package com.xiaomi.infra.hbase.client;

import java.util.List;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HRegionLocation;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.Condition;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Row;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.coprocessor.Batch;
import org.apache.hadoop.hbase.coprocessor.example.generated.BulkDeleteProtos.BulkDeleteRequest.DeleteType;
import org.apache.hadoop.hbase.coprocessor.example.generated.BulkDeleteProtos.BulkDeleteResponse;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.protobuf.generated.HBaseProtos;
import org.apache.hadoop.hbase.util.Pair;

import com.google.protobuf.Service;
import com.xiaomi.infra.hbase.client.duplication.DuplicationSupport;

/**
 * <p>
 * HBase Client interface definition.
 * <p>
 * HBaseClient implements this interface to realize the following features:
 * <p>
 * 1. all implemented methods are thread-safe
 * <p>
 * 2. all implemented methods will report corresponding PerfCount
 * <p>
 * 3. all implemented methods all throw HException
 * <p>
 * 4. should switch automatically among Onebox, Staging and Online environment
 * <p>
 * 5. using name service and client configuration from ZK
 * <p>
 * TODO features include:
 * <p>
 * 1. switch automatically between master-master cluster
 * <p>
 * 2. integrate secondary index support
 * @author cuijianwei
 */
public interface HBaseClientInterface {
  /**
   * get hbase configuration for client
   */
  public Configuration getConfiguration();
  
  /** 
   * wrapped interface for get(Get get) defined in HTableInterface
   */
  @DuplicationSupport
  public Result get(String tableName, Get get) throws HException;

  /** 
   * wrapped interface for get(List<Get> gets) defined in HTableInterface
   */
  @DuplicationSupport
  public Result[] get(String tableName, List<Get> gets) throws HException;

  /**
   * wrapped interface for put(Put put) defined in HTableInterface
   */
  @DuplicationSupport
  public void put(String tableName, Put put) throws HException;

  /**
   * scan 'tableName' and return results satisfy 'scan'
   */
  @DuplicationSupport
  public List<Result> scan(String tableName, Scan scan) throws HException;
  
  /**
   * scan 'tableName' and return first limit results satisfy 'scan'
   */
  @DuplicationSupport
  public List<Result> scan(String tableName, Scan scan, int limit) throws HException;

  /**
   * wrapped interface for delete(Delete delete) defined in HTableInterface.
   * Warning: if we want delete some column, it's better to invoke Delete.deleteColumns(family, qualifier)
   * instead of Delete.deleteColumn(family, qualifier). There will be correctness and efficiency problems
   * with Delete.deleteColumn(family, qualifier).
   * For details, view the comment of Delete.deleteColumn(byte [] family, byte [] qualifier)
   */
  @DuplicationSupport
  public void delete(String tableName, Delete delete) throws HException;

  /**
   * wrapped interface for mutateRows defined in MultiRowMutationService
   */
  @DuplicationSupport
  public List<Integer> mutateRows(String tableName, List<Mutation> mutations,
      List<Condition> conditions) throws HException;

  /**
   * wrapped interface for <R, S> R max(final byte[] tableName, final ColumnInterpreter<R, S> ci,
   * final Scan scan) defined in AggregationClient
   */
  @DuplicationSupport
  public Long max(String tableName, final Scan scan) throws HException;

  /**
   * wrapped interface for <R, S> long rowCount(final byte[] tableName, final ColumnInterpreter<R, S> ci,
   * final Scan scan) defined in AggregationClient
   */
  @DuplicationSupport
  public long rowCount(String tableName, final Scan scan) throws HException;
  
  /**
   * close connections to hbase servers
   * @throws HException
   */
  public void close() throws HException;

  /**
   * wrapped interface for getScanner(final Scan scan) defined in HTableInterface
   */
  public HClientScanner getScanner(String tableName, Scan scan) throws HException;

  /**
   * wrapped interface for coprocessorService in HTable
   */
  @DuplicationSupport
  public <T extends Service, R> void coprocessorExec(String tableName,
      Class<T> protocol, byte[] startKey, byte[] endKey, Batch.Call<T, R> callable,
      Batch.Callback<R> callback) throws HException;

  /**
   * wrapped interface for coprocessorService in HTable
   */
  @DuplicationSupport
  public <R> R coprocessorExec(String tableName, byte[] row, CoprocessorCall<R> call)
      throws HException;
  /**
   * Get start keys and end keys of regions of the table.
   */
  @DuplicationSupport
  public Pair<byte[][], byte[][]> getStartEndKeys(String tableName) throws HException;

  /**
   * Get all regions in the range.
   */
  @DuplicationSupport
  public List<HRegionLocation> getRegionsInRange(String tableName, byte[] startKey,
      byte[] endKey) throws HException;

  /**
   * wrapped interface for checkAndPut(final byte[] row, final byte[] family,
   * final byte[] qualifier, final CompareOp compareOp, final byte[] value, final Put put) in HTable
   */
  @DuplicationSupport
  public boolean checkAndPut(String tableName, final byte[] row, final byte[] family,
      final byte[] qualifier, final CompareOp compareOp, final byte[] value, final Put put)
      throws HException;

  /**
   * wrapped interface for checkAndPut(final byte[] row, final byte[] family,
   * final byte[] qualifier, final byte[] value, final Put put) in HTable
   */
  @DuplicationSupport
  public boolean checkAndPut(String tableName, final byte[] row, final byte[] family,
      final byte[] qualifier, final byte[] value, final Put put) throws HException;

  /**
   * if row is not exist, insert 'put'; otherwise, invoke checkAndPut(String tableName, final byte[]
   * row, final byte[] family, final byte[] qualifier, final CompareOp compareOp,
   * final byte[] value, final Put put)
   */
  @DuplicationSupport
  public boolean checkNullAndPut(String tableName, final byte[] row, final byte[] family,
      final byte[] qualifier, final CompareOp compareOp, final byte[] value, final Put put)
      throws HException;

  /**
   * wrapped interface for checkAndDelete(final byte[] row, final byte[] family,
   * final byte[] qualifier, final byte[] value, final Delete delete) in HTable
   */
  @DuplicationSupport
  public boolean checkAndDelete(String tableName, final byte[] row, final byte[] family,
      final byte[] qualifier, final byte[] value, final Delete delete) throws HException;

  /**
   * wrapped interface for checkAndDelete(final byte[] row, final byte[] family, final byte[]
   * qualifier, final byte[] value, final CompareOp compareOp, final Delete delete) in HTable
   */
  @DuplicationSupport
  public boolean checkAndDelete(String tableName, final byte[] row, final byte[] family,
      final byte[] qualifier, final CompareOp compareOp, final byte[] value, final Delete delete)
      throws HException;
  
  /**
   * wrapped interface for put(List<Put> ...) in HTable
   */
  @DuplicationSupport
  public void put(String tableName, final List<Put> puts) throws HException;

  /**
   * wrapped interface for delete(List<Delete> ...) in HTable
   */
  @DuplicationSupport
  public void delete(String tableName, final List<Delete> deletes) throws HException;

  /**
   * wrapped interface for Object[] batch(final List<? extends Row> actions) in HTable
   */
  @DuplicationSupport
  public Object[] batch(String tableName, final List<? extends Row> actions) throws HException;

  /**
   * wrapped interface for void batch(final List<?extends Row> actions, final Object[] results) in HTable, which
   * could return partial results when some row actions failed
   */
  @DuplicationSupport
  public void batch(String tableName, final List<? extends Row> actions, final Object[] results) throws HException;
  
  /**
   * wrapped interface for delete(HTableInterface...) in EndpointClient
   */
  @DuplicationSupport
  public BulkDeleteResponse bulkDelete(String tableName, final Scan scan, final DeleteType deleteType,
      final Long timestamp, final int rowBatchSize) throws HException;
  
  /**
   * wrapped interface for increment(final Increment increment) in HTable
   */
  @DuplicationSupport
  public Result increment(String tableName, final Increment increment) throws HException;
  
  /**
   * wrapped interface for incrementColumnValue(final byte [] row...) in HTable
   */
  @DuplicationSupport
  public long incrementColumnValue(String tableName, final byte[] row, final byte[] family,
      final byte[] qualifier, final long amount) throws HException;
  
  public void createTable(final HTableDescriptor desc) throws HException;
  
  public void createTable(final String clusterName, final HTableDescriptor desc) throws HException;

  public void createTable(final HTableDescriptor desc, final byte[] startKey, final byte[] endKey,
      final int numRegions) throws HException;
  
  public void createTable(final String clusterName, final HTableDescriptor desc,
      final byte[] startKey, final byte[] endKey, final int numRegions) throws HException;
  
  public void createTable(final HTableDescriptor desc, final byte [][] splitKeys) throws HException;
  
  public void createTable(final String clusterName, final HTableDescriptor desc,
      final byte[][] splitKeys) throws HException;
  
  public void disableTable(final String tableName) throws HException;
  
  public void disableTable(final String clusterName, final String tableName) throws HException;
  
  public void deleteTable(final String tableName) throws HException;
  
  public void deleteTable(final String clusterName, final String tableName) throws HException;
  
  public void enableTable(final String tableName) throws HException;
  
  public void enableTable(final String clusterName, final String tableName) throws HException;

  public void snapshot(final String snapshotName, final String tableName,
      final HBaseProtos.SnapshotDescription.Type type) throws HException;

  public void snapshot(final String clusterName, final String snapshotName, final String tableName,
      final HBaseProtos.SnapshotDescription.Type type) throws HException;

  public void deleteSnapshot(final String snapshotName) throws HException;

  public void deleteSnapshot(final String clusterName, final String snapshotName) throws HException;

  public void listSnapshots() throws HException;

  public void listSnapshots(final String clusterName) throws HException;

  public void cloneSnapshot(final String snapshotName, final String tableName) throws HException;

  public void cloneSnapshot(final String clusterName, final String snapshotName,
      final String tableName) throws HException;

  public void modifyTable(final String tableName, final HTableDescriptor desc) throws HException;

  public void modifyTable(final String clusterName, final String tableName,
      final HTableDescriptor desc) throws HException;

  public HTableDescriptor getTableDescriptor(final String tableName) throws HException;
  
  public HTableDescriptor getTableDescriptor(final String clusterName, final String tableName) throws HException;
  
  public boolean isTableEnabled(final String tableName) throws HException;
  
  public boolean isTableEnabled(final String clusterName, final String tableName) throws HException;
  
  public boolean isTableDisabled(final String tableName) throws HException;
  
  public boolean isTableDisabled(final String clusterName, final String tableName) throws HException;
  
  public boolean tableExists(final String tableName) throws HException;
  
  public boolean tableExists(final String clusterName, final String tableName) throws HException;

  // salted table interface
  public HClientScanner getScanner(String tableName, Scan scan, byte[][] slots, boolean merged)
    throws HException;

  public List<Result> scan(String tableName, Scan scan, byte[][] slots, boolean merged)
        throws HException;

  public List<Result> scan(String tableName, Scan scan, int limit, byte[][] slots, boolean merged)
    throws HException;
  
  /**
   * add replication from masterCluster to peerCluster. The whole table will be replicated if families is null
   */
  public void appendReplicationTableCFs(String masterCluster, String peerCluster, String tableName,
      Set<String> families) throws HException;
  
  public void appendReplicationTableCFsForPeer(String masterCluster, String peerId,
      String tableName, Set<String> families) throws HException;

  /**
   * remove replication from masterCluster to peerCluster. The whole table will be removed if families is null
   */
  public void removeReplicationTableCFs(String masterCluster, String peerCluster, String tableName,
      Set<String> families) throws HException;
  
  public void removeReplicationTableCFsForPeer(String masterCluster, String peerId,
      String tableName, Set<String> families) throws HException;
}
