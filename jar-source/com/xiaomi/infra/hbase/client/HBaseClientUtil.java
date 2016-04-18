package com.xiaomi.infra.hbase.client;

import java.io.IOException;
import java.util.Arrays;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.util.Bytes;

import com.xiaomi.infra.base.nameservice.NameService;
import com.xiaomi.infra.base.nameservice.NameServiceEntry;

// implement utility class for hbase and business
public class HBaseClientUtil {
  public static final int LONG_STRING_MAX_LENGTH = String.valueOf(Long.MAX_VALUE).length();
  
  // transfer the long value to its reversed string
  public static String reverseLong(long value) {
    StringBuilder builder = new StringBuilder(String.valueOf(value));
    return builder.reverse().toString();
  }
  
  // get stopRow of prefix by add one from right to left of prefix
  public static byte[] getStopRowByPrefixForForwardScan(byte[] prefix) throws HException {
    // In PrefixFilter, prefix = null will filter all rows. Here, if we
    // return null, user may use null to set stopRow of scan, which means
    // scan to the end of the table. To avoid this, we throw an exception
    if (prefix == null) {
      throw new HException("prefix is null when getStopRowByPrefix");
    }
    // rowkey = HConstants.EMPTY_BYTE_ARRAY is valid for hbase, and
    // prefix = HConstants.EMPTY_BYTE_ARRAY will return result for
    // rowkey = HConstants.EMPTY_BYTE_ARRAY. In this case, we should
    // avoid to return HConstants.EMPTY_BYTE_ARRAY to scan to the end of table
    if (Bytes.equals(prefix, HConstants.EMPTY_BYTE_ARRAY)) {
      return new byte[]{(byte)0x01};
    }
    
    byte[] stopRow = new byte[prefix.length];
    boolean shouldAddByte = true;
    for (int i = prefix.length - 1; i >= 0; --i) {
      if (shouldAddByte) {
        if (prefix[i] == (byte)0xff) {
          stopRow[i] = 0;
        } else {
          int tempValue = prefix[i] & 0xff;
          ++tempValue;
          stopRow[i] = (byte)tempValue;
          shouldAddByte = false;
        }
      } else {
        stopRow[i] = prefix[i];
      }
    }
    if (shouldAddByte) {
      return HConstants.EMPTY_END_ROW;
    }
    return stopRow;
  }
  
  // return the "next-rowkey" of the given rowkey. "next-rowkey" means no rowkeys
  // could be found in (rowkey, next-rowkey)
  public static byte[] nextRowkey(byte[] rowkey) {
    if (rowkey == null) {
      return rowkey;
    }
    return Bytes.add(rowkey, new byte[]{0});
  }
  
  public static byte[] prevRowkey(byte[] rowkey, int maxRowKeyLen) throws HException {
    if (rowkey == null || rowkey.length == 0) {
      return null;
    }
    if (rowkey.length > maxRowKeyLen) {
      throw new HException("row key length is larger than max row key length");
    }
    if (rowkey[rowkey.length - 1] == (byte) 0x00) {
      return Bytes.head(rowkey, rowkey.length - 1);
    } else {
      byte[] prev = new byte[maxRowKeyLen];
      Arrays.fill(prev, (byte) 0xff);
      System.arraycopy(rowkey, 0, prev, 0, rowkey.length - 1);
      prev[rowkey.length - 1] = (byte) (rowkey[rowkey.length - 1] - 1);
      return prev;
    }
  }

  public static String leftPadLongWithZero(long value) throws HException {
    return padLongWithZero(value, true);
  }
  
  public static String rightPadLongWithZero(long value) throws HException {
    return padLongWithZero(value, false);
  }
  
  protected static String padLongWithZero(long value, boolean leftPad) throws HException {
    if (value < 0) {
      throw new HException("value to pad zero can not be negative, value=" + value);
    }
    if (leftPad) {
      return StringUtils.leftPad(String.valueOf(value), LONG_STRING_MAX_LENGTH, '0');
    } else {
      return StringUtils.rightPad(String.valueOf(value), LONG_STRING_MAX_LENGTH, '0');
    }
  }
  
  public static String getClusterUri(String clusterName) {
    String clusterUri = clusterName;
    if (!clusterUri.startsWith("hbase://")) {
      clusterUri = "hbase://" + clusterName;
    }
    return clusterUri;
  }
  
  public static Configuration getConfigurationByClusterName(Configuration conf, String clusterName)
      throws IOException {
    if (clusterName == null || clusterName.equals("")) {
      return new Configuration(conf);
    }
    
    // "localhost://zkParent" will treated as cluster name to connection onebox cluster for unit test
    // TODO: nameservice should be compatible with onebox cluster
    if (clusterName.startsWith("localhost://")) {
      String[] tokens = clusterName.split("//");
      String zkParent = tokens[tokens.length - 1];
      Configuration result = new Configuration(conf);
      result.set(HConstants.ZOOKEEPER_ZNODE_PARENT, zkParent);
      return result;
    }
    
    String clusterUri = getClusterUri(clusterName);
    NameServiceEntry entry = NameService.resolve(clusterUri, conf);
    if (entry.getScheme() == null || !entry.getScheme().equals("hbase")) {
      throw new IOException("Unrecognized scheme: " + entry.getScheme() + ", scheme must be hbase");
    }
    return entry.createClusterConf(conf);
  }
}
