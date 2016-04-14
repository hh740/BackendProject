package com.codelab.common.utils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Base64工具类
 */
public class Base64Util {

    /**
     * 编码
     *
     * @param key
     * @return
     */
    public static String encrypt(String key) {
        return (new BASE64Encoder()).encodeBuffer(key.getBytes());
    }

    /**
     * 编码
     * @param bytes
     * @return
     */
    public static String encrypt(byte[] bytes){
        return (new BASE64Encoder()).encodeBuffer(bytes);
    }

    /**
     * 解码
     * @param data
     * @return
     * @throws IOException
     */
    public static String decryptToString(String data) throws IOException {
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] bytes = decoder.decodeBuffer(data);
        return new String(bytes);
    }

    /**
     * 解码
     * @param data
     * @return
     * @throws IOException
     */
    public static byte[] decryptToByte(String data) throws IOException {
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] bytes = decoder.decodeBuffer(data);
        return bytes;
    }

    /**
     * strean转byte
     * @param input
     * @return
     * @throws IOException
     */
    public static byte[] steamToByte(InputStream input) throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int len = 0;
        byte[] b = new byte[1024];
        while ((len = input.read(b, 0, b.length)) != -1) {
            baos.write(b, 0, len);
        }
        byte[] buffer =  baos.toByteArray();
        return buffer;
    }

    /**
     * byte转stream
     * @param buf
     * @return
     */
    public static final InputStream byteTostream(byte[] buf) {
        return new ByteArrayInputStream(buf);
    }
}
