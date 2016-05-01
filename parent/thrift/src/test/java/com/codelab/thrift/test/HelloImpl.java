package com.codelab.thrift.test;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * implaments by the Iface
 */
@Service
public class HelloImpl implements Hello.Iface{

    private static Logger  logger = LoggerFactory.getLogger(HelloImpl.class);

    @Override
    public String helloString(String para) throws TException {

        return null;
    }

    @Override
    public int helloInt(int para) throws TException {
        logger.debug("========================================================hello Int is called:{}",para);
        return 10;

    }

    @Override
    public boolean helloBoolean(boolean para) throws TException {
        return false;
    }

    @Override
    public void helloVoid() throws TException {

    }

    @Override
    public String helloNull() throws TException {
        return null;
    }
}
