package com.xiaomi.infra.hbase.client.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.coprocessor.example.generated.BulkDeleteProtos.BulkDeleteRequest.DeleteType;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

import com.xiaomi.infra.hbase.client.HBaseClientFactory;
import com.xiaomi.infra.hbase.client.HBaseClientInterface;
import com.xiaomi.infra.hbase.client.HException;

/**
 * Example codes for usage of APIs in HBaseClientInterface.java
 * The example is based on a user-message storage scenario.
 * Scenario: user-message storage
 * Description: we want to save user's chat messages. The message contains:
 * content, status (sent/unread/read) and readTime fields.
 * Firstly, this class will design hbase schema and create table to save such data.
 * Then, we will access hbase by invoking put/get/scan/checkAndput/delete, etc
 * defined in HBaseClientInterface.java 
 *
 */

// user message record
class Message {
  // we define the columnFamily and column name here. We define message content in one columnFamily, while
  // defining status and readTime in a different columnFamily. Family names are shorten to save memory.
  protected static final byte[] COLUMN_FAMILY_M = Bytes.toBytes("M"); // short for 'Message'
  protected static final byte[] COLUMN_FAMILY_S = Bytes.toBytes("S"); // short for 'Status'
  protected static final byte[] COLUMN_QUALIFIER_C = null; // 'null' can be the name of column qualifier 
  protected static final byte[] COLUMN_QUALIFIER_S = Bytes.toBytes("s"); // short for 'status'
  protected static final byte[] COLUMN_QUALIFIER_T = Bytes.toBytes("r"); // short for 'readTime'
  
  private long userId;     // identify a user
  private String content;  // message content
  private byte status;     // status of the message
  private long readTime;   // timestamp when this message is read

  // message status for sender
  protected static final byte MESSAGE_STATUS_SENT = 1;
  // message status for receiver 
  protected static final byte MESSAGE_STATUS_READ = 2;
  protected static final byte MESSAGE_STATUS_UNREAD = 3;
  
  public Message() {}
  
  public Message(long userId, String content, byte status, long readTime) {
    this.userId = userId;
    this.content = content;
    this.status = status;
    this.readTime = readTime;
  }
  
  public String toString() {
    return "userId=" + this.userId + ", content=" + content + ", status=" + status + ", readTime="
        + readTime;
  }
  
  public long getUserId() {
    return userId;
  }
  public void setUserId(long userId) {
    this.userId = userId;
  }
  public String getContent() {
    return content;
  }
  public void setContent(String content) {
    this.content = content;
  }
  public byte getStatus() {
    return status;
  }
  public void setStatus(byte status) {
    this.status = status;
  }
  public long getReadTime() {
    return readTime;
  }
  public void setReadTime(long readTime) {
    this.readTime = readTime;
  }
}

public class HBaseClientExample {
  private static final Log log = LogFactory.getLog(HBaseClientExample.class);

  // to run the following unit tests, we need to start an onebox hbase service in
  // HBASE_SERVICE_ADDRESS(follow http://wiki.n.miliao.com/xmg/Hbase%E5%8D%95%E6%9C%BA%E7%89%88
  // to start an onebox hbase service). If your onebox hbase service is not start at "127.0.0.1",
  // please change HBASE_SERVICE_ADDRESS to your hbase service host. if we want switch hbase
  // service host without modifying source code, we can define an specific hbase service host,
  // such as 'hbase_service_host' in /etc/hosts. Then, call
  // createConfigWithServiceHost('hbase_service_host') to create and hbase configuration and pass
  // it to getSingletonClient(config); in this case, changing the address of
  // 'hbase_service_host' in /etc/hosts will lead the client visit different hbase services.
  protected static final String HBASE_SERVICE_HOST = "127.0.0.1";
  private static final Configuration conf = HBaseClientFactory
      .createConfigWithServiceHost(HBASE_SERVICE_HOST);
  private static final String tableName = "user_message_example_table";
  private static HBaseClientInterface hbaseClient = null;
  
  // create hbase table to save user message
  public static void createMessageTable(String tableName) throws IOException {
    String clusterName = conf.get("hbase.cluster.name", "onebox");
    log.info("start to create " + tableName + " on hbase cluster " + clusterName);
    HBaseAdmin admin = new HBaseAdmin(conf);
    
    // delete table if exist
    if (admin.tableExists(tableName)) {
      log.info(tableName + " exist, delete first");
      if (admin.isTableEnabled(tableName)) {
        log.info(tableName + " enabled, disable first");
        admin.disableTable(tableName);
        log.info(tableName + " disabled");
      }
      admin.deleteTable(tableName);
      log.info(tableName + " deleted");
    }
    
    // make table descriptor and create table
    HTableDescriptor htd = new HTableDescriptor(tableName);
    HColumnDescriptor hcdMessage = new HColumnDescriptor(Message.COLUMN_FAMILY_M);
    htd.addFamily(hcdMessage);
    HColumnDescriptor hcdStatus = new HColumnDescriptor(Message.COLUMN_FAMILY_S);
    htd.addFamily(hcdStatus);
    admin.createTable(htd);
    log.info(tableName + " created");
    admin.close();
  }
  
  // example code for put
  public static void saveMessageByPut() throws HException {
    log.info("Put example:");
    // construct a message record
    Message message = new Message(1000, "test_message_0", Message.MESSAGE_STATUS_SENT, 0);
    Put put = new Put(Bytes.toBytes(String.valueOf(message.getUserId())));
    put.add(Message.COLUMN_FAMILY_M, Message.COLUMN_QUALIFIER_C, Bytes.toBytes(message.getContent()));
    put.add(Message.COLUMN_FAMILY_S, Message.COLUMN_QUALIFIER_S, new byte[] {message.getStatus()});
    hbaseClient.put(tableName, put);
  }
  
  // example code for multi-put
  public static void saveMesssagesByMultiPut() throws HException {
    log.info("Multi-Put example:");
    List<Put> puts = new ArrayList<Put>(2);
    Message message = new Message(1001, "test_message_1", Message.MESSAGE_STATUS_READ,
        1368524398253l);
    Put put = new Put(Bytes.toBytes(String.valueOf(message.getUserId())));
    put.add(Message.COLUMN_FAMILY_M, Message.COLUMN_QUALIFIER_C, Bytes.toBytes(message.getContent()));
    put.add(Message.COLUMN_FAMILY_S, Message.COLUMN_QUALIFIER_S, new byte[] {message.getStatus()});
    put.add(Message.COLUMN_FAMILY_S, Message.COLUMN_QUALIFIER_T, Bytes.toBytes(message.getReadTime()));
    puts.add(put);
    
    message = new Message(2002, "test_message_2", Message.MESSAGE_STATUS_UNREAD, 0); 
    put = new Put(Bytes.toBytes(String.valueOf(message.getUserId())));
    put.add(Message.COLUMN_FAMILY_M, Message.COLUMN_QUALIFIER_C, Bytes.toBytes(message.getContent()));
    put.add(Message.COLUMN_FAMILY_S, Message.COLUMN_QUALIFIER_S, new byte[] {message.getStatus()});
    puts.add(put);
    
    hbaseClient.put(tableName, puts);
  }
  
  // build message from hbase Result
  public static Message buildMessageFromResult(Result result) throws HException {
    Message message = new Message();
    message.setUserId(Long.parseLong(Bytes.toString(result.getRow())));
    // column 'M:' and 'S:s' mustn't be null, we convert byte array to corresponding type directly
    message.setContent(Bytes.toString(result.getValue(Message.COLUMN_FAMILY_M, Message.COLUMN_QUALIFIER_C)));
    message.setStatus(result.getValue(Message.COLUMN_FAMILY_S, Message.COLUMN_QUALIFIER_S)[0]);
    byte[] readTimeAsByteArray = result.getValue(Message.COLUMN_FAMILY_S, Message.COLUMN_QUALIFIER_T);
    // column 'S:r' could be null, we must check firstly
    if (readTimeAsByteArray != null) {
      message.setReadTime(Bytes.toLong(readTimeAsByteArray));
    }
    return message;
  }
  
  // example code for get
  public static void getMessage() throws HException {
    log.info("Get example:");
    Get get = new Get(Bytes.toBytes(String.valueOf(1000)));
    get.addFamily(Message.COLUMN_FAMILY_M);
    get.addFamily(Message.COLUMN_FAMILY_S);
    Result result = hbaseClient.get(tableName, get);
    Message message = buildMessageFromResult(result);
    log.info("Get message : " + message.toString());
  }
  
  // example code for multi-get
  public static void multiGetMessages() throws HException {
    log.info("Multi-Get example:");
    List<Get> gets = new ArrayList<Get>(2);
    Get get = new Get(Bytes.toBytes(String.valueOf(1001)));
    gets.add(get);
    get = new Get(Bytes.toBytes(String.valueOf(2002)));
    gets.add(get);
    Result[] results = hbaseClient.get(tableName, gets);
    for (Result result : results) {
      log.info(buildMessageFromResult(result));
    }
  }
  
  // example code for parallel-get
  /*
  public static void parallelGetMessages() throws HException {
    log.info("Parallel-Get example:");
    List<Get> gets = new ArrayList<Get>(2);
    Get get = new Get(Bytes.toBytes(String.valueOf(1001)));
    gets.add(get);
    get = new Get(Bytes.toBytes(String.valueOf(2002)));
    gets.add(get);
    Result[] results = hbaseClient.parallelGet(tableName, gets);
    for (Result result : results) {
      log.info(buildMessageFromResult(result));
    }
  }
  */
  
  // example code for various scans
  public static void scanAllMessages() throws HException {
    log.info("Scan-all example:");
    Scan scan = new Scan();
    scan.addFamily(Message.COLUMN_FAMILY_M);
    scan.addFamily(Message.COLUMN_FAMILY_S);
    List<Result> results = hbaseClient.scan(tableName, scan);
    for (Result result : results) {
      log.info(buildMessageFromResult(result));
    }
  }
  
  public static void scanLimitMessages() throws HException {
    log.info("Scan-limit example:");
    Scan scan = new Scan();
    scan.addFamily(Message.COLUMN_FAMILY_M);
    scan.addFamily(Message.COLUMN_FAMILY_S);
    int limit = 2;
    List<Result> results = hbaseClient.scan(tableName, scan, limit);
    for (Result result : results) {
      log.info(buildMessageFromResult(result));
    }
  }
  
  public static void scanWithStartAndEndKey() throws HException {
    log.info("Scan-with-startKey-endKey example:");
    Scan scan = new Scan(Bytes.toBytes(String.valueOf(1001)), Bytes.toBytes(String.valueOf(2002)));
    scan.addFamily(Message.COLUMN_FAMILY_M);
    scan.addFamily(Message.COLUMN_FAMILY_S);
    List<Result> results = hbaseClient.scan(tableName, scan);
    for (Result result : results) {
      log.info(buildMessageFromResult(result));
    }
  }
  
  public static void scanWithPrefixFilter() throws HException {
    log.info("Scan-with-prefixFilter example:");
    Scan scan = new Scan();
    // return rows with rowkeys of which start with "100"
    scan.setFilter(new PrefixFilter(Bytes.toBytes("100")));
    List<Result> results = hbaseClient.scan(tableName, scan);
    for (Result result : results) {
      log.info(buildMessageFromResult(result));
    }
  }
  
  public static void scanWithSingleColumnValuePrefix() throws HException {
    log.info("Scan-with-single-column-value-prefix example:");
    Scan scan = new Scan();
    // return rows with value of readTime column = 0
    scan.setFilter(new SingleColumnValueFilter(Message.COLUMN_FAMILY_S, Message.COLUMN_QUALIFIER_T,
        CompareOp.EQUAL, Bytes.toBytes(0l)));
    List<Result> results = hbaseClient.scan(tableName, scan);
    for (Result result : results) {
      log.info(buildMessageFromResult(result));
    }
  }
  
  public static void scanWithPrefixList() throws HException {
    log.info("Scan-with-prefixlist example:");
    Scan scan = new Scan();
    // combine PrefixFilter and SingleColumnValueFilter
    FilterList filters = new FilterList();
    filters.addFilter(new PrefixFilter(Bytes.toBytes("100")));
    filters.addFilter(new SingleColumnValueFilter(Message.COLUMN_FAMILY_S, Message.COLUMN_QUALIFIER_T,
        CompareOp.EQUAL, Bytes.toBytes(0l)));
    scan.setFilter(filters);
    List<Result> results = hbaseClient.scan(tableName, scan);
    for (Result result : results) {
      log.info(buildMessageFromResult(result));
    }
  }
  
  // example code for max
  public static void maxReadTime() throws HException {
    log.info("max-readtime example:");
    Scan scan = new Scan();
    scan.addColumn(Message.COLUMN_FAMILY_S, Message.COLUMN_QUALIFIER_T);
    long maxReadTime = hbaseClient.max(tableName, scan);
    log.info("MaxReadTime=" + maxReadTime);
  }
  
  // example code for checkAndPut
  public static void checkStatusUnReadAndUpdate() throws HException {
    // example code for checkAndPut Fail
    log.info("checkAndPut fail example:");
    Put put = new Put(Bytes.toBytes(String.valueOf(1001)));
    put.add(Message.COLUMN_FAMILY_S, Message.COLUMN_QUALIFIER_S,
      new byte[] {Message.MESSAGE_STATUS_READ});
    put.add(Message.COLUMN_FAMILY_S, Message.COLUMN_QUALIFIER_T,
      Bytes.toBytes(1368524398256l));
    log.info("Before checkAndPut for userId=1001");
    Get get = new Get(Bytes.toBytes(String.valueOf(1001)));
    log.info(buildMessageFromResult(hbaseClient.get(tableName, get)));
    boolean result = hbaseClient.checkAndPut(tableName, put.getRow(), Message.COLUMN_FAMILY_S,
      Message.COLUMN_QUALIFIER_S, CompareOp.EQUAL, new byte[]{Message.MESSAGE_STATUS_UNREAD},
      put);
    log.info("After checkAndPut for userId=1001, result=" + result);
    get = new Get(Bytes.toBytes(String.valueOf(1001)));
    log.info(buildMessageFromResult(hbaseClient.get(tableName, get)));
    
    // example code for checkAndPut Success
    log.info("checkAndPut success example:");
    put = new Put(Bytes.toBytes(String.valueOf(2002)));
    put.add(Message.COLUMN_FAMILY_S, Message.COLUMN_QUALIFIER_S,
      new byte[] {Message.MESSAGE_STATUS_READ});
    put.add(Message.COLUMN_FAMILY_S, Message.COLUMN_QUALIFIER_T,
      Bytes.toBytes(1368524398256l));
    log.info("Before checkAndPut for userId=2002");
    get = new Get(Bytes.toBytes(String.valueOf(2002)));
    log.info(buildMessageFromResult(hbaseClient.get(tableName, get)));
    result = hbaseClient.checkAndPut(tableName, put.getRow(), Message.COLUMN_FAMILY_S,
      Message.COLUMN_QUALIFIER_S, CompareOp.EQUAL, new byte[]{Message.MESSAGE_STATUS_UNREAD},
      put);
    log.info("After checkAndPut for userId=2002, result=" + result);
    get = new Get(Bytes.toBytes(String.valueOf(2002)));
    log.info(buildMessageFromResult(hbaseClient.get(tableName, get)));
  }
  
  // example code for delete
  public static void deleteOneUserMessage() throws HException {
    log.info("Delete example:");
    Delete delete = new Delete(Bytes.toBytes(String.valueOf(2002)));
    delete.deleteFamily(Message.COLUMN_FAMILY_M);
    delete.deleteFamily(Message.COLUMN_FAMILY_S);
    hbaseClient.delete(tableName, delete);
    
    Get get = new Get(Bytes.toBytes(String.valueOf(2002)));
    Result result = hbaseClient.get(tableName, get);
    log.info("Get result after delete :" + result);
  }
  
  // example code for bulkDelete
  public static void bulkDeleteUserMessage() throws HException {
    log.info("BulkDelete example:");
    Scan scan = new Scan(Bytes.toBytes(String.valueOf(1000)));
    List<Result> results = hbaseClient.scan(tableName, scan);
    log.info("Scan result size before bulk delete, size=" + results.size());
    hbaseClient.bulkDelete(tableName, scan, DeleteType.ROW, null, 10);
    results = hbaseClient.scan(tableName, scan);
    log.info("Scan result size after bulk delete, size=" + results.size());
  }
  
  public static void main(String args[]) throws IOException, HException {
    // create table if necessary
    createMessageTable(tableName);
    
    // create HBaseClient
    hbaseClient = HBaseClientFactory.getSingletonClient(conf);
    
    // example code for write
    log.info("\nExample code and result for 'Put' section\n");
    saveMessageByPut();
    saveMesssagesByMultiPut();
    
    log.info("\nExample code and result for 'Get' section\n");
    // example code for random read
    getMessage();
    multiGetMessages();
//    parallelGetMessages();
    
    log.info("\nExample code and result for 'Scan' section\n");
    // example code for scan
    scanAllMessages();
    scanLimitMessages();
    scanWithStartAndEndKey();
    scanWithPrefixFilter();
    scanWithSingleColumnValuePrefix();
    scanWithPrefixList();
    
    log.info("\nExample code and result for 'AggregationOperation' section\n");
    // example code for max
    maxReadTime();
    
    log.info("\nExample code and result for 'CheckAndPut' section\n");
    // example code for checkAndPut
    checkStatusUnReadAndUpdate();
    
    log.info("\nExample code and result for 'Delete' section\n");
    // example code for delete
    deleteOneUserMessage();
    bulkDeleteUserMessage();
    
    // close HBaseClient
    HBaseClientFactory.closeSingletonClient();
  }
}