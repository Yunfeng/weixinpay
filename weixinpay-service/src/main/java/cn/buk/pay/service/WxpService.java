package cn.buk.pay.service;

import cn.buk.pay.dto.*;

/**
 * Created by yfdai on 2017/3/2.
 */
public interface WxpService {

    String getAppId();

    WeixinOauthToken getOauthToken(String weixinOauthCode);

    JsApiParam generatePrepayOrder(int id, String openid, int totalFee, String ip);

    WxpData fromXml(String xml);

    String getSandBoxKey();


    Token getToken();

    JsSdkParam getJsSdkConfig(String jsapi_url);
}
