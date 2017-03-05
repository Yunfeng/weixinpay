package cn.buk.pay.web;

import cn.buk.pay.dto.*;
import cn.buk.pay.service.WxpService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by yfdai on 2017/3/2.
 */
@RestController
public class HomeController {

    private static Logger logger = Logger.getLogger(HomeController.class);

    @Autowired
    private WxpService wxpService;

    @RequestMapping("/appId")
    public String getAppid() {
        return wxpService.getAppId();
    }

    @RequestMapping("/openId")
    public WeixinOauthToken searchOpenId(@RequestParam("code") String code) {
        return wxpService.getOauthToken(code);
    }

//    @RequestMapping("/sandboxKey")
//    public String getSandBoxKey() {
//        return wxpService.getSandBoxKey();
//    }

    @RequestMapping("/access_token")
    public Token getSandBoxKey() {
        return wxpService.getToken();
    }

    @RequestMapping("/jssdk_config")
    public JsSdkParam getJsSdkConfig(@RequestParam("url") String url) {
        return wxpService.getJsSdkConfig(url);
    }

    @RequestMapping("/generatePrepayOrder")
    public JsApiParam generatePrepayOrder(HttpServletRequest request,
                                          @RequestParam("id") int id,
                                          @RequestParam("openid") String openid,
                                          @RequestParam("total_fee") int totalFee) throws Exception {
        String ip = getIpAddr(request);
        logger.info("remoteIp: " + ip);
        return wxpService.generatePrepayOrder(id, openid, totalFee, ip);
    }

    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    @RequestMapping(value = "/notify", produces = "application/xml")
    public String notify(
            @RequestBody(required = false) String xml) {
        WxpData rs0 = null;

        if (xml != null) {
            logger.info(xml);

            rs0 = wxpService.fromXml(xml);
        }

        WxpData rs = new WxpData();
        if (rs0 == null) {
            //error
            rs.setMap("return_code", "FAIL");
            rs.setMap("return_msg", "UNKNOWN ERRORS");
        } else {
            //ok
            rs.setMap("return_code", "SUCCESS");
            rs.setMap("return_msg", "OK");
        }

        return rs.toXml();
    }

}
