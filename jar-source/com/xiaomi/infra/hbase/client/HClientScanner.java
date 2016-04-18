package com.xiaomi.infra.hbase.client;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.ClientScanner;
import org.apache.hadoop.hbase.client.ClientSmallScanner;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.ReversedClientScanner;
import org.apache.hadoop.hbase.client.Scan;

import com.xiaomi.infra.hbase.client.InternalHBaseClient.SaltedScanOptions;
import com.xiaomi.infra.hbase.salted.SaltedHTable;

/**
 * <p>Wrapped class of org.apache.hadoop.hbase.client.ClientScanner. The aim of this class is:
 * <p>1. all methods only throw HException
 * <p>2. coprresonding HTable will return to HTablePool when invoking close() method
 * @author cuijianwei
 *
 */
public class HClientScanner {
  private final HTableInterface hTable;
  private final ResultScanner scanner;

  protected HClientScanner() {
    hTable = null;
    scanner = null;
  }

  // TODO: tableName is not needed?
  protected HClientScanner(final Configuration conf, final Scan scan, final byte[] tableName,
      SaltedScanOptions options, HTableInterface hTable) throws IOException, HException {
    if (hTable instanceof SaltedHTable) {
      this.scanner = createSaltedScanner((SaltedHTable)hTable, scan, options);
    } else {
      if (scan.isReversed()) {
        this.scanner = new ReversedClientScanner(conf, scan, TableName.valueOf(tableName),
            HConnectionManager.getConnection(conf));
      } else {
        if (scan.isSmall()) {
          this.scanner = new ClientSmallScanner(conf, scan, TableName.valueOf(tableName),
              HConnectionManager.getConnection(conf));
        } else {
          this.scanner = new ClientScanner(conf, scan, tableName);
        }
      }
    }
    
    this.hTable = hTable;
  }
  
  protected static ResultScanner createSaltedScanner(SaltedHTable hTable, Scan scan,
      SaltedScanOptions options) throws IOException, HException {
    if (options == null) {
      return hTable.getScanner(scan);
    } else {
      return hTable.getScanner(scan, options.slots, options.keepSalted, options.merged);
    }
  }

  public void close() throws HException {
    if (scanner != null) {
      scanner.close();
    }
    InternalHBaseClient.closeHTable(hTable);
  }

  public Result next() throws HException {
    try {
      return scanner.next();
    } catch (IOException e) {
      throw new HException(e);
    }
  }
}
