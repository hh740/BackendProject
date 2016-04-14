package com.codelab.common.utils;

import com.codelab.common.constants.Confure;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 使用httpclient提供公共操作工具
 */
public class HttpUtil {

    // 日志工具
    private static Logger logger = LoggerFactory.getLogger(HttpUtil.class);


    /**
     * 使用Get方法请求获取String类型结果
     *
     * @param url
     * @return
     */
    public static String reqStringByGet(String url) {
        // 结果字符串
        String resultStr = null;
        // 得到浏览器
        HttpClient httpClient = HttpUtil.genHttpClient();
        // 指定请求方式
        HttpGet httpGet = new HttpGet(url);

        try {
            // 执行请求
            HttpResponse httpResponse = httpClient.execute(httpGet);
            // 判断是否请求成功
            int status = httpResponse.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK) {
                // 读取响应内容
                resultStr = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            }
            else {
                // 打印响应码
                logger.info("HTTP request fails in method reqJsonByGet, resonse code :" + status);
            }
        }
        catch (Exception e) {
            logger.error("Exception happens when deal with http request in method reqJsonByGet: " + e.getCause());
            logger.error(e.getMessage());
        }
        finally {
            // 关闭链接
            httpGet.releaseConnection();
        }

        return resultStr;
    }

    /**
     * 使用POST方法请求获取String类型结果
     *
     * @param url
     * @param paramMap
     * @return
     */
    public static String reqStringByPost(String url, Map<String, String> paramMap, Map<String, String> entityMap) {
        // 响应结果
        String resultStr = null;
        // 得到浏览器
        HttpClient httpClient = HttpUtil.genHttpClient();
        // 指定请求方法
        HttpPost httpPost = new HttpPost(url);

        try {
            // 如果表单参数不为空，则设置参数
            if (null != paramMap) {
                // 指定post的表单
                HttpParams httpParams = new BasicHttpParams();
                //填充表单
                for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                    httpParams.setParameter(entry.getKey(), entry.getValue());
                }
                httpPost.setParams(httpParams);
            }

            // 如何entity参数不为空，则设置参数
            if (null != entityMap) {
                List<BasicNameValuePair> nvPair = new ArrayList<BasicNameValuePair>();
                // 遍历entityMap，填充参数
                for (Map.Entry<String, String> entity : entityMap.entrySet()) {
                    nvPair.add(new BasicNameValuePair(entity.getKey(), entity.getValue()));
                }
                // 设置表单提交编码为UTF-8
                httpPost.setEntity(new UrlEncodedFormEntity(nvPair, "UTF-8"));
            }

            // 执行post方法
            HttpResponse httpResponse = httpClient.execute(httpPost);
            // 判断执行是否成
            int status = httpResponse.getStatusLine().getStatusCode();
            if (HttpStatus.SC_OK == status) {
                // 读取响应数据
                resultStr = new String(EntityUtils.toString(httpResponse.getEntity()).getBytes(), "utf-8");
            }
            else {
                // 打印响应码
                logger.info("HTTP request fails in method reqJsonByPost, resonse code :" + status);
            }
        }
        catch (Exception e) {
            logger.error("Exception happens when deal with http request in method reqJsonByPost: " + e.getCause());
        }

        return resultStr;
    }

    /**
     * 使用POST方法请求获取String类型结果(8859-1)
     *
     * @param url
     * @param paramMap
     * @return
     */
    public static String reqStringByPostForISO8859(String url, Map<String, String> paramMap,
        Map<String, String> entityMap) {
        // 响应结果
        String resultStr = null;
        // 得到浏览器
        HttpClient httpClient = HttpUtil.genHttpClient();
        // 指定请求方法
        HttpPost httpPost = new HttpPost(url);

        try {
            // 如果表单参数不为空，则设置参数
            if (null != paramMap) {
                // 指定post的表单
                HttpParams httpParams = new BasicHttpParams();
                //填充表单
                for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                    httpParams.setParameter(entry.getKey(), entry.getValue());
                }
                httpPost.setParams(httpParams);
            }

            // 如何entity参数不为空，则设置参数
            if (null != entityMap) {
                List<BasicNameValuePair> nvPair = new ArrayList<BasicNameValuePair>();
                // 遍历entityMap，填充参数
                for (Map.Entry<String, String> entity : entityMap.entrySet()) {
                    nvPair.add(new BasicNameValuePair(entity.getKey(), entity.getValue()));
                }
                // 设置表单提交编码为UTF-8
                httpPost.setEntity(new UrlEncodedFormEntity(nvPair, "UTF-8"));
            }

            // 执行post方法
            HttpResponse httpResponse = httpClient.execute(httpPost);
            // 判断执行是否成
            int status = httpResponse.getStatusLine().getStatusCode();
            if (HttpStatus.SC_OK == status) {
                // 读取响应数据
                resultStr = new String(EntityUtils.toString(httpResponse.getEntity()).getBytes("ISO8859-1"), "utf-8");
            }
            else {
                // 打印响应码
                logger.info("HTTP request fails in method reqJsonByPost, resonse code :" + status);
            }
        }
        catch (Exception e) {
            logger.error("Exception happens when deal with http request in method reqJsonByPost: " + e.getCause());
        }

        return resultStr;
    }

    /**
     * 生成一个设定好的httpClient实例
     *
     * @return
     */
    private static HttpClient genHttpClient() {
        // HttpClient配置
        HttpParams mHttpParams = new BasicHttpParams();

        //设置网络链接超时
        HttpConnectionParams.setConnectionTimeout(mHttpParams, Confure.HTTP_TIME_OUT);

        //设置socket响应超时
        HttpConnectionParams.setSoTimeout(mHttpParams, Confure.SOCKET_TIME_OUT);

        //设置socket缓存大小
        HttpConnectionParams.setSocketBufferSize(mHttpParams, Confure.SOCKET_BUFFER_SIZE);

        // 按照配置生成HttpClient实例
        HttpClient httpClient = new DefaultHttpClient(mHttpParams);

        return httpClient;
    }
}
