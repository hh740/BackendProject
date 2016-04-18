package com.xiaomi.infra.hbase.client;

public class OperationDisabledException extends HException {
  public OperationDisabledException() {
    super("Current HBase operation is disabled");
  }

  public OperationDisabledException(String op) {
    super("Current HBase operation is disabled: " + op);
  }
}
