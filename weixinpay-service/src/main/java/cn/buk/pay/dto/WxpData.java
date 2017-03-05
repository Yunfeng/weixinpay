package cn.buk.pay.dto;

import com.tencent.common.MD5;
import com.tencent.common.XMLParser;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yfdai on 2017/2/28.
 */
public class WxpData {

    private static Logger logger = Logger.getLogger(WxpData.class);

    private Map<String, Object> map = new HashMap<>();

    public void setMap(String key, Object value) {
        if (key != null && value != null) {
            map.put(key, value);
        }
    }

    public Object getMap(String key) {
        return map.get(key);
    }

    public String makeSign(String apiKey) {
        String result = this.toUrl();
        result += "key=" + apiKey;

        System.out.println("Sign Before MD5: \n" + result);

        result = MD5.MD5Encode(result).toUpperCase();
        System.out.println("Sign Result: \n" + result);

        return result;
    }

    public String toXml() {
        //数据为空时不能转化为xml格式
        if (0 == this.map.size()) {
            logger.error(this.getClass().toString() + " WxPayData数据为空!");
            return null;
        }
        String xml = "<xml>";
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() == null) {
                logger.error(this.getClass().toString() + "WxPayData内部含有值为null的字段!");
                return null;
            }

            if (entry.getValue().getClass() == Integer.class) {
                xml += "<" + entry.getKey() + ">" + entry.getValue() + "</" + entry.getKey() + ">";
            } else if (entry.getValue().getClass() == String.class) {
                xml += "<" + entry.getKey() + ">" + "<![CDATA[" + entry.getValue() + "]]></" + entry.getKey() + ">";
            } else {
                logger.error(this.getClass().toString() + "WxPayData字段数据类型错误!");
                return null;
            }
        }

        xml += "</xml>";
        return xml;
    }

    public void fromXml(String xml, String key) throws Exception {
        try {
            this.map = XMLParser.getMapFromXML(xml);

            //错误消息是没有签名的
            String returnCode = (String)map.get("return_code");
            if( returnCode.equalsIgnoreCase("SUCCESS") && key != null ) {
                checkSign(key);//验证签名,不通过会抛异常
            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * 检测签名是否正确
     * 正确返回true，错误抛异常
     */
    private boolean checkSign(String key) throws Exception {
        //如果没有设置签名，则跳过检测
        if (this.map.get("sign") == null)
        {
            logger.error(this.getClass().toString() +  " WxPayData sign is not existed!");
            throw new Exception("WxPayData sign is not existed!");
        }

        //获取接收到的签名
        String return_sign = (String)this.map.get("sign");
        //在本地计算新的签名
        String cal_sign = makeSign(key);
        if (cal_sign.equalsIgnoreCase(return_sign))
        {
            logger.debug("sign check passed.");
            return true;
        }
        logger.error(this.getClass().toString() + " WxPayData签名验证错误!");
        throw new Exception("WxPayData签名验证错误!");
    }

    /**
     *
     * @ return url格式串, 该串不包含sign字段值
     */
    private String toUrl()
    {
        ArrayList<String> list = new ArrayList<>();

        for(Map.Entry<String,Object> entry: this.map.entrySet()) {
            if (entry.getValue() != null && !entry.getKey().equalsIgnoreCase("sign") ) {
                list.add(entry.getKey() + "=" + entry.getValue() + "&");
            }
        }

        int size = list.size();
        String[] arrayToSort = list.toArray(new String[size]);

        Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);

        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < size; i ++) {
            sb.append(arrayToSort[i]);
        }
        String result = sb.toString();
        return result;
    }


}
