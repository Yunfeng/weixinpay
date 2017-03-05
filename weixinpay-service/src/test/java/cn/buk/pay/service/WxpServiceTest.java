package cn.buk.pay.service;

import cn.buk.pay.dto.WxpData;
import com.tencent.common.MD5;
import org.junit.Test;

/**
 * Created by yfdai on 2017/3/2.
 */
public class WxpServiceTest {


    @Test
    public void testMD5() {
//        Sign Before MD5:
// appId=wx9a613d6e422845b9&
// nonceStr=qa4z1913dzg09usdw170akjsa4q7bsra&
// package=prepay_id=wx20170302230532109020&
// signType=MD5&
// timeStamp=1488467132&
// key=b58627bfd54315380f5c7ebc068a1844
//        Sign Result:
// 745CCCA4AF6D8D2F0F73F3763CB2C94D
        WxpData result = new WxpData();


//        result.setAppId("wx9a613d6e422845b9");
//        result.setTimeStamp("1488467132");
//        result.setNonceStr("qa4z1913dzg09usdw170akjsa4q7bsra");
//        result.setPackageA("prepay_id=wx2017030223053210902");
//        result.setSignType("MD5");
//
//        String sign1 = result.makeSign("b58627bfd54315380f5c7ebc068a1844"); //使用微信支付密码 签名
//        result.setPaySign(sign1);


    }

    @Test
    public void testMd52() {
        String aa = "appId=wx9a613d6e422845b9&nonceStr=qa4z1913dzg09usdw170akjsa4q7bsra&package=prepay_id=wx2017030223053210902&signType=MD5&timeStamp=1488467132&key=b58627bfd54315380f5c7ebc068a1844";
        String cc = "appId=wx9a613d6e422845b9&nonceStr=qa4z1913dzg09usdw170akjsa4q7bsra&package=prepay_id=wx2017030223053210902&signType=MD5&timeStamp=1488467132&key=b58627bfd54315380f5c7ebc068a1844";
        String bb = "appId=wx9a613d6e422845b9&nonceStr=qa4z1913dzg09usdw170akjsa4q7bsra&package=wx20170302230532109020&timeStamp=1488467132&key=b58627bfd54315380f5c7ebc068a1844";
        System.out.println(aa);
        System.out.println(MD5.MD5Encode(aa).toUpperCase());
    }
}