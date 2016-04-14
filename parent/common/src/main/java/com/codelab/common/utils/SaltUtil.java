package com.codelab.common.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 根据request请求参数生成校验码。
 * 目前生成规则
 * 1. 将request中key值为s的参数对去掉
 * 2. 按照key值的字母顺序给request中的参数对排序
 * 3. 生成url_encoded格式的请求字符串，然后取md5值。
 */
public class SaltUtil {

    @SuppressWarnings("unused")
    private static class SMSItem {
        String phoneNumber;

        String content;
    }

    private static final Logger logger = LoggerFactory.getLogger(SaltUtil.class.getName());

    /*
    * @Param request HttpRequest请求参数
    * @Param publishId  分配的token
    * 根据规则生成校验码，与传过来的校验码比对，判断请求是否合法
   */
    public static boolean isValidRequest(HttpServletRequest request, String uuid) {
        if (StringUtils.isBlank(request.getParameter("sign"))) {
            return false;
        }

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        for (Object keyObj : request.getParameterMap().keySet()) {
            String key = (String)keyObj;
            logger.debug("key : " + key + " param: " + request.getParameter(key));

            if (key.equalsIgnoreCase("sign")) {
                continue;
            }
            nameValuePairs.add(new BasicNameValuePair(key, request.getParameter(key)));
        }

        String keyFromParam = getKeyFromParams(nameValuePairs, uuid);
        logger.debug("sing:" + keyFromParam + " request param: " + request.getParameter("sign"));
        return request.getParameter("sign").equals(keyFromParam);
    }

    /*
    * @Param request HttpRequest请求参数
    * @Param publishId  分配的token
    * 按照规则生成校验码
   */
    public static String generateSalt(HttpServletRequest request, String uuid) {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        for (Object keyObj : request.getParameterMap().keySet()) {
            String key = (String)keyObj;
            if (key.equalsIgnoreCase("sign")) {
                continue;
            }
            nameValuePairs.add(new BasicNameValuePair(key, request.getParameter(key)));
        }
        return getKeyFromParams(nameValuePairs, uuid);
    }

    public static String getKeyFromParams(List<NameValuePair> nameValuePairs, String uuid) {
        Collections.sort(nameValuePairs, new Comparator<NameValuePair>() {
            @Override
            public int compare(NameValuePair p1, NameValuePair p2) {
                return p1.getName().compareTo(p2.getName());
            }
        });

        //*****

        StringBuilder keyBuilder = new StringBuilder();
        boolean isFirst = true;
        for (NameValuePair nvp : nameValuePairs) {
            if (!isFirst) {
                keyBuilder.append("&");
            }
            keyBuilder.append(nvp.getName()).append("=").append(nvp.getValue());
            isFirst = false;
        }

        keyBuilder.append("&").append(uuid);

        String key = keyBuilder.toString();
        if(logger.isDebugEnabled())logger.debug("key: " + key);
        byte[] keyBytes = getBytes(key);
        String base64 = new String(Base64.encodeBase64(keyBytes));
        if(logger.isDebugEnabled()) logger.debug("base64: " + base64);
        String md5 = getMd5Digest(base64);
        if(logger.isDebugEnabled()) logger.debug("md5: " + key);
        return md5;
    }

    private static byte[] getBytes(String s) {
        try {
            return s.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            return s.getBytes();
        }
    }

    private static String getMd5Digest(String pInput) {
        try {
            MessageDigest lDigest = MessageDigest.getInstance("MD5");
            lDigest.update(getBytes(pInput));
            BigInteger lHashInt = new BigInteger(1, lDigest.digest());
            return String.format("%1$032X", lHashInt);
        }
        catch (NoSuchAlgorithmException lException) {
            throw new RuntimeException(lException);
        }
    }
}
