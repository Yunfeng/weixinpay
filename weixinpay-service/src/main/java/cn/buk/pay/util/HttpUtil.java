package cn.buk.pay.util;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.util.List;

public class HttpUtil {

    private static int CONNECTION_TIMEOUT = 100000;
    // HTTP scoket connection timeout
    private static int SO_TIMEOUT = 120000;

    public static Logger log = Logger.getRootLogger();

    public static String getUrl(String url, List<NameValuePair> params) {
        String uri = url;
        if (params != null) uri += URLEncodedUtils.format(params, "UTF-8");
        //System.out.println(uri.toString());
        log.info(uri.toString());

        HttpGet httpget = new HttpGet(uri);
        HttpClient httpclient = new DefaultHttpClient();

        httpclient.getParams().setParameter(HttpProtocolParams.HTTP_CONTENT_CHARSET, "UTF-8");
        //请求超时 ,连接超时
        httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT);
        //读取超时
        httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, SO_TIMEOUT);

        String rs = "";

        try {
            HttpResponse response = httpclient.execute(httpget);//  httpClient.executeMethod(postMethod);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                rs = EntityUtils.toString(response.getEntity(), "UTF-8");


                log.info("rs: " + rs);

                return rs;
            } else {
                System.out.println("rs: not HttpStatus.SC_OK");
            }
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            httpclient.getConnectionManager().shutdown();
        }
        return rs;
    }

    public static String postUrl(String url, String body) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);
        httppost.getParams().setParameter(HttpProtocolParams.HTTP_CONTENT_CHARSET, "UTF-8");

        //请求超时 ,连接超时
        httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT);
        //读取超时
        httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, SO_TIMEOUT);


        try {
            StringEntity entity = new StringEntity(body, "UTF-8");
            httppost.setEntity(entity);

            System.out.println(entity.toString());

            HttpResponse response = httpclient.execute(httppost);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String charsetName = EntityUtils.getContentCharSet(response.getEntity());
                //System.out.println(charsetName + "<<<<<<<<<<<<<<<<<");

                String rs = EntityUtils.toString(response.getEntity());
                //System.out.println( ">>>>>>" + rs);

                return rs;
            } else {
                //System.out.println("Eorr occus");
            }
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            httpclient.getConnectionManager().shutdown();
        }

        return "";
    }
}
