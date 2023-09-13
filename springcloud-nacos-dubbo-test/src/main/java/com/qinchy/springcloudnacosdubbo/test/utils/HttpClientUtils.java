package com.qinchy.springcloudnacosdubbo.test.utils;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.CodingErrorAction;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HttpClient工具类
 *
 * @author Administrator
 */
@Slf4j
public class HttpClientUtils {

    public final static int DefaultConnectTimeout = 120 * 1000; // 默认的超时时间
    private static PoolingHttpClientConnectionManager connManager = null;
    private static CloseableHttpClient httpclient = null;

    static {
        try {
            SSLContext sslContext = SSLContexts.custom().build();
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }}, null);
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.INSTANCE).register("https", new SSLConnectionSocketFactory(sslContext)).build();

            connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

            HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() {
                @Override
                public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                    if (executionCount > 5) {
                        return false;
                    }

                    HttpRequest request = (HttpRequest) context.getAttribute(HttpCoreContext.HTTP_REQUEST);
                    if (request.getRequestLine().getMethod().equals(HttpPost.METHOD_NAME)) {
                        try {
                            log.warn("retry --- {} --- request---->{},Data---->{}", executionCount, (HttpRequestWrapper.wrap(request)).getURI(), EntityUtils.toString((((HttpEntityEnclosingRequest) request)).getEntity()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        log.warn("retry --- {} --- request---->{}", executionCount, request.getRequestLine().getUri());
                    }

                    if (exception instanceof InterruptedIOException) {
                        // Timeout
                        return true;
                    }
                    if (exception instanceof UnknownHostException) {
                        // Unknown host
                        return true;
                    }
                    if (exception instanceof ConnectTimeoutException) {
                        // Connection refused
                        return true;
                    }
                    if (exception instanceof SSLException) {
                        // SSL handshake exception
                        return true;
                    }
                    if (exception instanceof HttpHostConnectException) {
                        return true;
                    }
                    if (exception instanceof SocketTimeoutException) {
                        return true;
                    }
                    if (exception instanceof SocketException) {
                        return true;
                    }
                    return exception instanceof IOException;
                }

            };

            httpclient = HttpClients.custom().setConnectionManager(connManager).setRetryHandler(retryHandler).build();

            // Create socket configuration
            SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).setSoTimeout(DefaultConnectTimeout).build();
            connManager.setDefaultSocketConfig(socketConfig);
            // Create message constraints
            MessageConstraints messageConstraints = MessageConstraints.custom().setMaxHeaderCount(200).setMaxLineLength(2000).build();
            // Create connection configuration
            ConnectionConfig connectionConfig = ConnectionConfig.custom().setMalformedInputAction(CodingErrorAction.IGNORE).setUnmappableInputAction(CodingErrorAction.IGNORE).setCharset(Consts.UTF_8).setMessageConstraints(messageConstraints).build();
            connManager.setDefaultConnectionConfig(connectionConfig);
            connManager.setMaxTotal(200);
            connManager.setDefaultMaxPerRoute(25);
        } catch (KeyManagementException e) {
            log.error("KeyManagementException", e);
        } catch (NoSuchAlgorithmException e) {
            log.error("NoSuchAlgorithmException", e);
        }
    }

    /**
     * 发起application/json请求
     *
     * @param url
     * @param json
     * @param encoding
     * @return
     */
    public static String postJson(String url, String json, String encoding, int timeout) {
        HttpPost post = new HttpPost(url);
        try {
            int time = DefaultConnectTimeout;
            if (timeout != 0) {
                time = timeout;
            }
            post.setHeader("Content-type", "application/json");
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(DefaultConnectTimeout).setConnectTimeout(DefaultConnectTimeout).setConnectionRequestTimeout(time).setExpectContinueEnabled(false).build();
            post.setConfig(requestConfig);
            post.setEntity(new StringEntity(json, encoding));
            log.warn("[HttpUtils Json Post] begin invoke url:" + url + " , params:" + json);
            CloseableHttpResponse response = httpclient.execute(post);
            try {
                HttpEntity entity = response.getEntity();
                try {
                    if (entity != null) {
                        String str = EntityUtils.toString(entity, encoding);
                        log.warn("[HttpUtils Json Post]Debug response, url :" + url + " , response string :" + str);
                        return str;
                    }
                } finally {
                    if (entity != null) {
                        entity.getContent().close();
                    }
                }
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        } catch (UnsupportedEncodingException e) {
            log.error("UnsupportedEncodingException", e);
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            post.releaseConnection();
        }
        return "";
    }

    public static String postJsonBody(String url, int timeout, Map<String, Object> map, String encoding) {
        HttpPost post = new HttpPost(url);
        try {
            post.setHeader("Content-type", "application/json");
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout).setConnectionRequestTimeout(timeout).setExpectContinueEnabled(false).build();
            post.setConfig(requestConfig);
            String str1 = JSON.toJSONString(map);
            post.setEntity(new StringEntity(str1, encoding));
            log.warn("[HttpUtils Post] begin invoke url:" + url + " , params:" + str1);
            CloseableHttpResponse response = httpclient.execute(post);
            try {
                HttpEntity entity = response.getEntity();
                try {
                    if (entity != null) {
                        String str = EntityUtils.toString(entity, encoding);
                        log.warn("[HttpUtils Post]Debug response, url :" + url + " , response string :" + str);
                        return str;
                    }
                } finally {
                    if (entity != null) {
                        entity.getContent().close();
                    }
                }
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        } catch (UnsupportedEncodingException e) {
            log.error("UnsupportedEncodingException", e);
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            post.releaseConnection();
        }
        return "";
    }

    public static String invokeGet(String url, Map<String, Object> params, Integer connectTimeout) {
        return invokeGet(url, params, connectTimeout, null);
    }

    public static String invokeGet(String url, Map<String, Object> params, Integer connectTimeout, String token) {
        String responseString = null;
        if (null == connectTimeout || connectTimeout < 0) {
            connectTimeout = DefaultConnectTimeout;
        }

        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(connectTimeout).setConnectTimeout(connectTimeout).setConnectionRequestTimeout(connectTimeout).build();

        StringBuilder sb = new StringBuilder();
        sb.append(url);
        int i = 0;
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (i == 0 && !url.contains("?")) {
                    sb.append("?");
                } else {
                    sb.append("&");
                }
                sb.append(entry.getKey());
                sb.append("=");
                String value = entry.getValue() + "";
                try {
                    sb.append(URLEncoder.encode(value, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    log.warn("encode http get params error, value is " + value, e);
                    sb.append(URLEncoder.encode(value));
                }
                i++;
            }
        }
        log.warn("[HttpUtils Get] begin invoke:" + sb);
        HttpGet get = new HttpGet(sb.toString());
        get.setConfig(requestConfig);
        if (StringUtils.isNotBlank(token)) {
            get.addHeader("X-Access-Token", token);
        }
        try {
            CloseableHttpResponse response = httpclient.execute(get);
            try {
                HttpEntity entity = response.getEntity();
                try {
                    if (entity != null) {
                        responseString = EntityUtils.toString(entity, "utf-8");
                    }
                } finally {
                    if (entity != null) {
                        entity.getContent().close();
                    }
                }
            } catch (Exception e) {
                log.error(String.format("[HttpUtils Get]get response error, url:%s", sb), e);
                return responseString;
            } finally {
                if (response != null) {
                    response.close();
                }
            }
            log.warn(String.format("[HttpUtils Get]Debug url:%s , response string %s:", sb, responseString));
        } catch (SocketTimeoutException e) {
            log.error(String.format("[HttpUtils Get]invoke get timout error, url:%s", sb), e);
            return responseString;
        } catch (Exception e) {
            log.error(String.format("[HttpUtils Get]invoke get error, url:%s", sb), e);
        } finally {
            get.releaseConnection();
        }
        return responseString;
    }

    /**
     * HTTPS请求
     *
     * @param reqURL
     * @param params
     * @return
     */
    public static String connectPostHttps(String reqURL, Map<String, Object> params, Integer connectTimeout) {

        String responseContent = null;
        if (null == connectTimeout || connectTimeout < 0) {
            connectTimeout = DefaultConnectTimeout;
        }

        HttpPost httpPost = new HttpPost(reqURL);
        try {
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(connectTimeout).setConnectTimeout(connectTimeout).setConnectionRequestTimeout(connectTimeout).build();

            List<NameValuePair> formParams = new ArrayList<NameValuePair>();

            httpPost.setConfig(requestConfig);
            if (params != null && !params.isEmpty()) {
                // 绑定到请求 Entry
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    String name = entry.getKey();
                    String value = entry.getValue().toString();
                    if (StringUtils.isNotBlank(value)) {
                        formParams.add(new BasicNameValuePair(name, value));
                    }
                }
            }
            httpPost.setEntity(new UrlEncodedFormEntity(formParams, Consts.UTF_8));
            log.info("requestURI : " + httpPost.getURI() + ", requestContent: " + EntityUtils.toString(httpPost.getEntity()));
            CloseableHttpResponse response = httpclient.execute(httpPost);
            try {
                // 执行POST请求
                HttpEntity entity = response.getEntity(); // 获取响应实体
                try {
                    if (null != entity) {
                        responseContent = EntityUtils.toString(entity, Consts.UTF_8);
                    }
                } finally {
                    if (entity != null) {
                        entity.getContent().close();
                    }
                }
            } finally {
                if (response != null) {
                    response.close();
                }
            }
            log.warn("requestURI : " + httpPost.getURI() + ", responseContent: " + responseContent);
        } catch (ClientProtocolException e) {
            log.error("ClientProtocolException", e);
        } catch (IOException e) {
            log.error("IOException", e);
        } finally {
            httpPost.releaseConnection();
        }
        return responseContent;
    }

//	/**
//	 * 下载文件云端文件到服务器
//	 * @param sourceUrl
//	 * @param savePath
//	 * @throws Exception
//	 */
//	public static void downloadFile(String sourceUrl, String savePath) throws Exception {
//		URL url = new URL(sourceUrl);
//		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
//		conn.setRequestMethod("GET");
//		conn.setConnectTimeout(5 * 1000);
//		InputStream inStream = conn.getInputStream();
//		byte[] data = readInputStream(inStream);
//		File file = new File(savePath);
//		if(!file.getParentFile().exists()){
//			file.getParentFile().mkdir();
//		}
//		FileOutputStream outStream = new FileOutputStream(file);
//		outStream.write(data);
//		outStream.close();
//	}

    /***
     * 下载远端文件
     * @param remoteFilePath 远端路径
     * @param localFilePath 本地路径
     */
    public static void downloadFile(String remoteFilePath, String localFilePath) {
        URL urlfile = null;
        HttpURLConnection httpUrl = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        File f = new File(localFilePath);
        try {
            urlfile = new URL(remoteFilePath);
            httpUrl = (HttpURLConnection) urlfile.openConnection();
            httpUrl.connect();
            bis = new BufferedInputStream(httpUrl.getInputStream());
            bos = new BufferedOutputStream(new FileOutputStream(f));
            int len = 2048;
            byte[] b = new byte[len];
            while ((len = bis.read(b)) != -1) {
                bos.write(b, 0, len);
            }
            bos.flush();
            bis.close();
            httpUrl.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bis.close();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 读取流
     *
     * @param inStream
     * @return
     * @throws Exception
     */
    public static byte[] readInputStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        //创建一个Buffer字符串
        byte[] buffer = new byte[1024];
        //每次读取的字符串长度，如果为-1，代表全部读取完毕
        int len = 0;
        //使用一个输入流从buffer里把数据读取出来
        while ((len = inStream.read(buffer)) != -1) {
            //用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度
            outStream.write(buffer, 0, len);
        }
        //关闭输入流
        inStream.close();
        //把outStream里的数据写入内存
        return outStream.toByteArray();
    }
}