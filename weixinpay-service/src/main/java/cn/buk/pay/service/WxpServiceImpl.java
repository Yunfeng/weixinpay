package cn.buk.pay.service;

import cn.buk.pay.dto.*;
import cn.buk.pay.util.DateUtil;
import cn.buk.pay.util.HttpUtil;
import cn.buk.pay.util.SignUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencent.WXPay;
import com.tencent.common.RandomStringGenerator;
import com.tencent.common.XMLParser;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by yfdai on 2017/3/2.
 */
@Component
public class WxpServiceImpl implements WxpService {

    private static Logger logger = Logger.getLogger(WxpServiceImpl.class);

    /**
     * 以下三个参数是微信接口需要用到的
     */
    @Value("${Weixin_AppId}")
    private String appId;

    @Value("${Weixin_AppSecret}")
    private String appSecret;

    @Value("${Weixin_Token}")
    private String weixinToken;

    @Value("${WeixinPay_MchId}")
    private String mchId;

    @Value("${WeixinPay_CertFilename}")
    private String certFilename;

    @Value("${WeixinPay_CertPassword}")
    private String certPassword;

    /**
     * 微信支付KEY
     */
    @Value("${WeixinPay_Key}")
    private String weixinpayKey;

    private String sandboxKey;

    private Token token;

    private String getKey() {
        if (sandboxKey != null) {
            return sandboxKey;
        } else {
            return weixinpayKey;
        }
    }

    private String getWeixinPaySandboxSignKey() throws Exception {
//        String localPath = this.getClass().getResource("/").getPath();
//
//        String certLocalPath = localPath + this.certFilename;

//        private String mch_id = "";
//
//        private String nonce_str = "";
//
//        private String sign = "";


        SandboxSignKeyReqData reqData = new SandboxSignKeyReqData();
        reqData.setMch_id(mchId);
        reqData.prepare();

        String xml = WXPay.requestSandboxSignKeyService(reqData);
        logger.info(xml);

        Map<String,Object> map = XMLParser.getMapFromXML(xml);
        String content = (String)map.get("sandbox_signkey");
        logger.info("sandbox_signkey: " + content);

        return content;
    }

    public String getAppId() {
        return this.appId;
    }

    /**
     * 获取 access_token
     * @return
     */
    public Token getToken() {

        long pastSeconds = 0;
        if (token != null) {
            pastSeconds = DateUtil.getPastSeconds(token.getCreateTime());
        }

        if (token == null || pastSeconds >= token.getExpires_in()) {
            //去获取新token
            //https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET
            String url = "https://api.weixin.qq.com/cgi-bin/token?";

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("grant_type", "client_credential"));
            params.add(new BasicNameValuePair("appid", appId));
            params.add(new BasicNameValuePair("secret", appSecret));

            String jsonStr = HttpUtil.getUrl(url, params);

            //System.out.println(url);
            logger.debug(jsonStr);

            //判断返回结果
            JSONObject param = (JSONObject) JSON.parse(jsonStr);

            token = new Token();
            token.setAccess_token((String) param.get("access_token"));
            token.setExpires_in((Integer) param.get("expires_in"));
            token.setCreateTime(DateUtil.getCurDateTime());
        }
        return token;
    }

    /**
     * 获取JS SDK的CONFIG参数
     * wx.config({

     debug: true, // 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。

     appId: '', // 必填，公众号的唯一标识

     timestamp: , // 必填，生成签名的时间戳

     nonceStr: '', // 必填，生成签名的随机串

     signature: '',// 必填，签名，见附录1

     jsApiList: [] // 必填，需要使用的JS接口列表，所有JS接口列表见附录2

     });
     * @return
     */
    public JsSdkParam getJsSdkConfig(String jsapi_url) {
        JsSdkParam jsapiParam = new JsSdkParam();
        jsapiParam.setAppId(this.appId);

        // 1.get token
        Token token = getToken();

        // 2.用第一步拿到的access_token 采用http GET方式请求获得jsapi_ticket（有效期7200秒，开发者必须在自己的服务全局缓存jsapi_ticket）：
        // https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=ACCESS_TOKEN&type=jsapi
//        {
//
//            "errcode":0,
//
//                "errmsg":"ok",
//
//                "ticket":"bxLdikRXVbTPdHSM05e5u5sUoXNKd8-41ZO3MhKoyN5OfkWITDGgnr2fwJ0m9E8NYzWKVZvdVtaUgWvsdshFKA",
//
//                "expires_in":7200
//
//        }

//        成功返回如下JSON：
        //去获取新token
        //https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET
        String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?";

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("access_token", token.getAccess_token()));
        params.add(new BasicNameValuePair("type", "jsapi"));

        String jsonStr = HttpUtil.getUrl(url, params);
        logger.debug(jsonStr);

        //判断返回结果
        JSONObject param = (JSONObject) JSON.parse(jsonStr);

        String jsapi_ticket =  (String) param.get("ticket");


        // 3. 签名
        Map<String, String> ret = SignUtil.sign(jsapi_ticket, jsapi_url);
        jsapiParam.setTimestamp(ret.get("timestamp"));
        jsapiParam.setNonceStr(ret.get("nonceStr"));
        jsapiParam.setSignature(ret.get("signature"));

        jsapiParam.setUrl(jsapi_url);

        return jsapiParam;
    }

    public WeixinOauthToken getOauthToken(String weixinOauthCode) {
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?";

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("appid", appId));
        params.add(new BasicNameValuePair("secret", appSecret));
        params.add(new BasicNameValuePair("code", weixinOauthCode));
        params.add(new BasicNameValuePair("grant_type", "authorization_code"));

        String jsonStr = HttpUtil.getUrl(url, params);

        logger.info(jsonStr);

        // 判断返回结果
        JSONObject param = JSON.parseObject(jsonStr);

        if (param.get("errcode") == null) {

            WeixinOauthToken token = new WeixinOauthToken();
            // token.setAccess_token((String) param.get("access_token"));
            // token.setRefresh_token((String) param.get("refresh_token"));
            token.setOpenid((String) param.get("openid"));
            // token.setScope((String) param.get("scope"));
            // token.setExpires_in((Integer) param.get("expires_in"));

            return token;
        } else {
            return null;
        }
    }

    @Override
    public JsApiParam generatePrepayOrder(int id, String openid, int totalFee, String ip) {
        logger.info("id = " + id + ", openid = " + openid + ", total fee = " + totalFee);

        String localPath = this.getClass().getResource("/").getPath();
        String certLocalPath = localPath + certFilename;

        WXPay.initSDKConfiguration(weixinpayKey, appId, mchId, "", certLocalPath, certPassword);

        // 沙箱密钥
//        try {
//            sandboxKey = getWeixinPaySandboxSignKey();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//
//        if (this.sandboxKey == null) return null;

        //WXPay.initSDKConfiguration(sandboxKey, appId, mchId, "", certLocalPath, certPassword);

        WxpData wxpData = new WxpData();

        wxpData.setMap("device_info", "sandbox");
        wxpData.setMap("body", "test");
        wxpData.setMap("attach", "test");
        wxpData.setMap("goods_tag", "test");

        Date startTime = DateUtil.getCurDateTime();
        String temp = DateUtil.formatDate(startTime, "yyyyMMddHHmmss");
        wxpData.setMap("out_trade_no", temp + "1001");

        wxpData.setMap("time_start", DateUtil.formatDate(startTime, "yyyyMMddHHmmss"));
        Date expireTime = DateUtil.addMinutes(startTime, 10);
        wxpData.setMap("time_expire", DateUtil.formatDate(expireTime, "yyyyMMddHHmmss"));

        wxpData.setMap("total_fee", totalFee);

        wxpData.setMap("spbill_create_ip", ip);
        wxpData.setMap("notify_url", "http://wxp.90sky.com/wxp/notify");
        wxpData.setMap("trade_type", "JSAPI");
        wxpData.setMap("openid", openid);

        wxpData.setMap("appid", appId);
        wxpData.setMap("mch_id", mchId);
        wxpData.setMap("nonce_str", RandomStringGenerator.getRandomStringByLength(32));


        String sign = wxpData.makeSign(this.getKey());
        wxpData.setMap("sign", sign);


        String xml = null;

        logger.info("Unified Order Request:");

        try {
            xml = WXPay.requestUnifiedOrderService(wxpData);
        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.info("Unified Order Response:");
        System.out.println(xml);


        // 生成JS需要的签名
        JsApiParam params = new JsApiParam();
        WxpData result = new WxpData();
        try {
            UnifiedOrderResData res = UnifiedOrderResData.fromXml(xml);
            if (res.getReturn_code().equalsIgnoreCase("SUCCESS")) {
                result.setMap("appId", appId);

                final String timeStamp = "" + DateUtil.getCurDateTime().getTime() / 1000;
                result.setMap("timeStamp", timeStamp);

                final String nonceStr = RandomStringGenerator.getRandomStringByLength(32);
                result.setMap("nonceStr", nonceStr);

                final String packageA = "prepay_id=" + res.getPrepay_id();
                result.setMap("package", packageA);

                final String signType = "MD5";
                result.setMap("signType", "MD5");

                final String sign1 = result.makeSign(this.getKey()); //使用微信支付密码 签名
                result.setMap("paySign", sign1);

                logger.info("JsApi paras:");
                logger.info(result.toXml());

                params.setAppId(appId);
                params.setTimeStamp(timeStamp);
                params.setNonceStr(nonceStr);
                params.setPackageA(packageA);
                params.setSignType(signType);
                params.setPaySign(sign1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        return params;
    }

    @Override
    public WxpData fromXml(String xml) {
        WxpData wxpData = new WxpData();

        try {
            wxpData.fromXml(xml, this.getKey());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return wxpData;
    }

    @Override
    public String getSandBoxKey() {
        String localPath = this.getClass().getResource("/").getPath();
        String certLocalPath = localPath + certFilename;

        WXPay.initSDKConfiguration(weixinpayKey, appId, mchId, "", certLocalPath, certPassword);

        // 沙箱密钥
        try {
            sandboxKey = getWeixinPaySandboxSignKey();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return sandboxKey;
    }

}
