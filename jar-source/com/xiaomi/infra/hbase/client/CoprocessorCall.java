package com.xiaomi.infra.hbase.client;

import com.google.protobuf.ServiceException;
import org.apache.hadoop.hbase.ipc.CoprocessorRpcChannel;

public interface CoprocessorCall<R> {
  R call(CoprocessorRpcChannel channel) throws ServiceException;
}
