package com.tencent.service;

import com.tencent.common.Configure;
import com.tencent.protocol.pay_query_protocol.ScanPayQueryReqData;

/**
 * User: rizenguo
 * Date: 2014/10/29
 * Time: 16:04
 */
public class ScanPayQueryService extends BaseService{

    public ScanPayQueryService() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        super(Configure.PAY_QUERY_API);
    }

    /**
     * 请求支付查询服务
     * @param scanPayQueryReqData 这个数据对象里面包含了API要求提交的各种数据字段
     * @return API返回的XML数据
     * @throws Exception
     */
    public String request(ScanPayQueryReqData scanPayQueryReqData) throws Exception {

        //--------------------------------------------------------------------
        //发送HTTPS的Post请求到API地址
        //--------------------------------------------------------------------
        String responseString = sendPost(scanPayQueryReqData);

        return responseString;
    }


}
