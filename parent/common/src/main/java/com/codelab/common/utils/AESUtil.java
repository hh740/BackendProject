package com.codelab.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Random;

/**
 * AES加解密算法
 */

public class AESUtil {

    private static Logger logger = LoggerFactory.getLogger(AESUtil.class);

    // 加密
    public static String encrypt(String sSrc, String sKey)
        throws Exception {
        if (sKey == null) {
            logger.error("AES ENCRYPT : sKey is null");
            return null;
        }
        // 判断Key是否为16位
        if (sKey.length() != 16) {
            logger.error("AES ENCRYPT : sKey's length is not 16");
            return null;
        }
        byte[] raw = sKey.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");//"算法/模式/补码方式"
        IvParameterSpec iv = new IvParameterSpec("0102030405060708".getBytes());//使用CBC模式，需要一个向量iv，可增加加密算法的强度
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes());

        return new BASE64Encoder().encode(encrypted);//此处使用BASE64做转码功能，同时能起到2次加密的作用。
    }

    // 解密
    public static String decrypt(String sSrc, String sKey)
        throws Exception {
        try {
            // 判断Key是否正确
            if (sKey == null) {
                logger.error("AES ENCRYPT : sKey is null");
                return null;
            }
            // 判断Key是否为16位
            if (sKey.length() != 16) {
                logger.error("AES ENCRYPT : sKey's length is not 16");
                return null;
            }
            byte[] raw = sKey.getBytes("ASCII");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec("0102030405060708"
                .getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] encrypted1 = new BASE64Decoder().decodeBuffer(sSrc);//先用base64解密
            try {
                byte[] original = cipher.doFinal(encrypted1);
                String originalString = new String(original);
                return originalString;
            }
            catch (Exception e) {
                logger.error("AESUtil : ", e);
                return null;
            }
        }
        catch (Exception ex) {
            logger.error("AESUtil : ", ex);
            return null;
        }
    }

    /**
     * 获取明文AES 的密钥(随机)
     *
     * @return
     */
    public static String getAESKeyPlaintext() {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 16; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        /*
         * 加密用的Key 可以用26个字母和数字组成，最好不要用保留字符，虽然不会错，至于怎么裁决，个人看情况而定
         * 此处使用AES-128-CBC加密模式，key需要为16位。
         */
        String cKey = "1234567890123456";
        // 需要加密的字串
        String cSrc = "Hello, This is a aes test.";
        System.out.println(cSrc);
        // 加密
        String enString = AESUtil.encrypt(cSrc, cKey);
        System.out.println("加密后的字串是：" + enString);

        // 解密
        String DeString = AESUtil.decrypt(enString, cKey);
        System.out.println("解密后的字串是：" + DeString);
    }
}