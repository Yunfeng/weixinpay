package cn.buk.pay.web;

import cn.buk.pay.dto.JsApiParam;
import cn.buk.pay.service.WxpService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by yfdai on 2017/3/5.
 */
@Controller
@RequestMapping("/test")
public class JspController {

    private static Logger logger = Logger.getLogger(JspController.class);

    @Autowired
    private WxpService wxpService;

    @RequestMapping(value = "/wxp-jsp")
    public String home(HttpServletRequest request,
                       @RequestParam("code") String code,
                       Model model) {

        String openid =  wxpService.getOauthToken(code).getOpenid();

        String ip = getIpAddr(request);
        logger.info("remoteIp: " + ip);
        JsApiParam jsparam = wxpService.generatePrepayOrder(1, openid, 101, ip);

//        String jsonParam = JSON.toJSONString(jsparam);
        String jsonParam = jsparam.toJson();
        logger.debug(jsonParam);

        model.addAttribute("wxparam", jsonParam);

        return "home";
    }

    @RequestMapping(value = "/wxp-jsp-test")
    public String home(HttpServletRequest request,
                       Model model) {

        String ip = getIpAddr(request);
        logger.debug("remoteIp: " + ip);
        JsApiParam jsparam = new JsApiParam();
        jsparam.setAppId("APPID");
        jsparam.setNonceStr("AAA");
        jsparam.setPackageA("PACKAGEA");
        jsparam.setPaySign("AAA");

//        String jsonParam = JSON.toJSONString(jsparam);
        String jsonParam = jsparam.toJson();
        logger.debug(jsonParam);

        model.addAttribute("wxparam", jsonParam);

//        List<JsApiParam> list = new ArrayList<>();
//        for(int i = 0; i < 10; i++) {
//            JsApiParam p = new JsApiParam();
//            p.setAppId("APPID-" + i);
//
//            list.add(p);
//        }
//        model.addAttribute("mylist", list);

        return "home";
    }

    private static String getIpAddr(HttpServletRequest request) {
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
}
