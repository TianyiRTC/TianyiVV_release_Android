package com.sip.rtcclient.http.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import jni.http.DefaultHttpsClient;
import jni.http.HttpResult;
import jni.http.RTCException;
import jni.util.Utils;

//示范应用服务器请求
public class AppHttpUtils {
    private static AppHttpUtils instance;
    private String LOGTAG="AppHttpUtils";
    private int timeout = 10; //s
    public static AppHttpUtils getInstance() {
        if(instance == null)
        {
            instance = new AppHttpUtils();
        }
        return instance;
    }
  //  private static String rooturl = "http://101.36.88.8/rtcclient/";

    private static String rooturl = "http://test2.chinartc.com/vv/";
    public static String loginurl = rooturl+"login.php";
    public static String getlisturl = rooturl+"getUserList.php";
    public static String updateurl = rooturl+"check_update.php";
    
    public HttpResult apppost(String posturl,String params)
    {
        HttpResult httpresult = new HttpResult(); 
        int result = -1; //用于http请求的结果
        HttpPost httpPost = new HttpPost(posturl);
        httpPost.setHeader("Content-Type", "application/json");
       try {
            Utils.PrintLog(5, LOGTAG, "apppost  setEntity--0");
            httpPost.setEntity(new StringEntity(params,HTTP.UTF_8));
            Utils.PrintLog(5, LOGTAG, "apppost  setEntity--1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            httpresult.setStatus(RTCException.E_HTTP);
            httpresult.setErrorMsg("UnsupportedEncodingException error");
        }
        HttpResponse httpResponse = null;
        String responseStr = "";
        String resultString = null;

        HttpClient defaultHttpClient = null;
        try {
            URL tempurl= new URL(posturl);
            if(tempurl.getProtocol().equals("https"))
                defaultHttpClient = DefaultHttpsClient.create(tempurl.getPort(), null);//new DefaultHttpClient();
            else 
            {
                defaultHttpClient = new DefaultHttpClient();
                defaultHttpClient.getParams().setParameter(
                        CoreConnectionPNames.SO_TIMEOUT, timeout * 1000);    //请求超时

                defaultHttpClient.getParams().setParameter(
                        CoreConnectionPNames.CONNECTION_TIMEOUT, timeout * 1000);    //连接超时
            }
      
        } catch (MalformedURLException e1) {
            // TODO Auto-generated catch block
            httpresult.setStatus(RTCException.E_HTTP);
            httpresult.setErrorMsg("MalformedURLException error");
        }
       try {
            // 取得HTTP response
            httpResponse = defaultHttpClient.execute(httpPost);
            // 若状态码200 ok
            int responseCode = httpResponse.getStatusLine().getStatusCode();
            Utils.PrintLog(1, LOGTAG, "ResponseCode:" + responseCode);
            httpresult.setStatus(responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 取出回应字串
                HttpEntity entity = httpResponse.getEntity();
                if (entity != null) {
                    responseStr = EntityUtils.toString(entity, "utf-8");
                }
                Utils.PrintLog(1, LOGTAG, "下行大小：" + responseStr.length());
                resultString = responseStr;
                httpresult.setObject(resultString);
            }
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            httpresult.setStatus(RTCException.E_HTTP);
            httpresult.setErrorMsg("ClientProtocolException error");
           // throw new RTCException("ClientProtocolException error", RTCException.E_HTTP);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            httpresult.setStatus(RTCException.E_HTTP);
            httpresult.setErrorMsg("IOException error");
            //throw new RTCException("IOException error", RTCException.E_HTTP);
        }finally{
            // 关闭连接
            httpPost.abort();
            defaultHttpClient.getConnectionManager().shutdown();
            defaultHttpClient = null;
        }
        Utils.PrintLog(1, LOGTAG, "[" + posturl + "]接口返回的初始值：");
        Utils.PrintLog(1, LOGTAG, "->" + resultString);
        return httpresult;

    }
}
