package com.xiaomi.infra.hbase.client;

import com.xiaomi.miliao.dal.DAOException;

/**
 * All methods defined in HBaseClientInterface only throw HException  
 * @author cuijianwei
 *
 */
public class HException extends DAOException {
  private static final long serialVersionUID = -3034994063807520421L;

  public HException() {
    super();
  }

  public HException(String message, Throwable cause) {
    super(message, cause);
  }

  public HException(String message) {
    super(message);
  }

  public HException(Throwable cause) {
    super(cause);
  }
}
