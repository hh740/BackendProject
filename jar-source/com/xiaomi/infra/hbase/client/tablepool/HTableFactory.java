package com.xiaomi.infra.hbase.client.tablepool;

import java.io.IOException;

import com.xiaomi.infra.hbase.client.InternalHBaseClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.util.Bytes;

import com.xiaomi.infra.hbase.salted.KeySalter;
import com.xiaomi.infra.hbase.salted.SaltedHTable;

public class HTableFactory extends org.apache.hadoop.hbase.client.HTableFactory {
  private static final Log LOG = LogFactory.getLog(HTableFactory.class);
  
  @Override
  public HTableInterface createHTableInterface(Configuration config,
      byte[] tableName) {
    HTable hTable = (HTable)super.createHTableInterface(config, tableName);
    hTable.setAutoFlush(config.getBoolean(InternalHBaseClient.HTABLE_AUTOFLUSH, true));
    try {
      KeySalter salter = SaltedHTable.getKeySalter(hTable);
      if (salter != null) {
        LOG.info("Create SaltedHTable in HTableFactory, tableName=" + Bytes.toString(tableName));
        SaltedHTable saltedTable = new SaltedHTable(hTable, salter);
        return saltedTable;
      }
      return hTable;
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }  
}
