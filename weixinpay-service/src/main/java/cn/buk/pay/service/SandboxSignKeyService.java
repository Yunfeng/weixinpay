package cn.buk.pay.service;

import cn.buk.pay.dto.SandboxSignKeyReqData;
import com.tencent.common.Configure;
import com.tencent.service.BaseService;

/**
 * User: rizenguo
 * Date: 2014/10/29
 * Time: 16:03
 */
public class SandboxSignKeyService extends BaseService {

    public SandboxSignKeyService() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        super(Configure.SANDBOX_SIGNKEY_API);
    }

    /**
     * 请求支付服务
     * @param req 这个数据对象里面包含了API要求提交的各种数据字段
     * @return API返回的数据
     * @throws Exception
     */
    public String request(SandboxSignKeyReqData req) throws Exception {

        //--------------------------------------------------------------------
        //发送HTTPS的Post请求到API地址
        //--------------------------------------------------------------------
        String responseString = sendPost(req);

        return responseString;
    }
}
