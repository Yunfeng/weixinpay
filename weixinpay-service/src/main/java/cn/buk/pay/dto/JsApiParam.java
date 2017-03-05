package cn.buk.pay.dto;

/**
 * Created by yfdai on 2017/3/4.
 */
public class JsApiParam {
    private String appId;
    private String timeStamp;
    private String nonceStr;
    private String packageA;
    private String signType;
    private String paySign;

    public String toJson() {
        String json = "{";

        json += "\"appId\": " + "\"" + appId + "\",";
        json += "\"timeStamp\": " + "\"" + timeStamp + "\",";
        json += "\"nonceStr\": " + "\"" + nonceStr + "\",";
        json += "\"package\": " + "\"" + packageA + "\",";
        json += "\"signType\": " + "\"" + signType + "\",";
        json += "\"paySign\": " + "\"" + paySign + "\"";

        json += "}";
        return json;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppId() {
        return appId;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setNonceStr(String nonceStr) {
        this.nonceStr = nonceStr;
    }

    public String getNonceStr() {
        return nonceStr;
    }

    public void setPackageA(String packageA) {
        this.packageA = packageA;
    }

    public String getPackageA() {
        return packageA;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public String getSignType() {
        return signType;
    }

    public void setPaySign(String paySign) {
        this.paySign = paySign;
    }

    public String getPaySign() {
        return paySign;
    }
}
