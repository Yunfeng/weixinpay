package com.tencent.business;

import com.tencent.common.*;
import com.tencent.common.report.ReporterFactory;
import com.tencent.common.report.protocol.ReportReqData;
import com.tencent.common.report.service.ReportService;
import com.tencent.protocol.refund_query_protocol.RefundOrderData;
import com.tencent.protocol.refund_query_protocol.RefundQueryReqData;
import com.tencent.protocol.refund_query_protocol.RefundQueryResData;
import com.tencent.service.RefundQueryService;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

/**
 * User: rizenguo
 * Date: 2014/12/2
 * Time: 18:51
 */
public class RefundQueryBusiness {

    public RefundQueryBusiness() throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        refundQueryService = new RefundQueryService();
    }

    public interface ResultListener{
        //API返回ReturnCode不合法，支付请求逻辑错误，请仔细检测传过去的每一个参数是否合法，或是看API能否被正常访问
        void onFailByReturnCodeError(RefundQueryResData refundQueryResData);

        //API返回ReturnCode为FAIL，支付API系统返回失败，请检测Post给API的数据是否规范合法
        void onFailByReturnCodeFail(RefundQueryResData refundQueryResData);

        //支付请求API返回的数据签名验证失败，有可能数据被篡改了
        void onFailBySignInvalid(RefundQueryResData refundQueryResData);

        //退款查询失败
        void onRefundQueryFail(RefundQueryResData refundQueryResData);

        //退款查询成功
        void onRefundQuerySuccess(RefundQueryResData refundQueryResData);

    }

    //打log用
    private static Log log = new Log(LoggerFactory.getLogger(RefundQueryBusiness.class));

    //执行结果
    private static String result = "";

    //查询到的结果
    private static String orderListResult = "";

    private RefundQueryService refundQueryService;

    public String getOrderListResult() {
        return orderListResult;
    }

    public void setOrderListResult(String orderListResult) {
        RefundQueryBusiness.orderListResult = orderListResult;
    }

    /**
     * 运行退款查询的业务逻辑
     * @param refundQueryReqData 这个数据对象里面包含了API要求提交的各种数据字段
     * @param resultListener 商户需要自己监听被扫支付业务逻辑可能触发的各种分支事件，并做好合理的响应处理
     * @throws Exception
     */
    public void run(RefundQueryReqData refundQueryReqData, RefundQueryBusiness.ResultListener resultListener) throws Exception {

        //--------------------------------------------------------------------
        //构造请求“退款查询API”所需要提交的数据
        //--------------------------------------------------------------------

        //接受API返回
        String refundQueryServiceResponseString;

        long costTimeStart = System.currentTimeMillis();

        //表示是本地测试数据
        log.i("退款查询API返回的数据如下：");
        refundQueryServiceResponseString = refundQueryService.request(refundQueryReqData);

        long costTimeEnd = System.currentTimeMillis();
        long totalTimeCost = costTimeEnd - costTimeStart;
        log.i("api请求总耗时：" + totalTimeCost + "ms");

        log.i(refundQueryServiceResponseString);

        //将从API返回的XML数据映射到Java对象
        RefundQueryResData refundQueryResData = (RefundQueryResData) Util.getObjectFromXML(refundQueryServiceResponseString, RefundQueryResData.class);

        ReportReqData reportReqData = new ReportReqData(
                refundQueryReqData.getDevice_info(),
                Configure.REFUND_QUERY_API,
                (int) (totalTimeCost),//本次请求耗时
                refundQueryResData.getReturn_code(),
                refundQueryResData.getReturn_msg(),
                refundQueryResData.getResult_code(),
                refundQueryResData.getErr_code(),
                refundQueryResData.getErr_code_des(),
                refundQueryResData.getOut_trade_no(),
                Configure.getIP()
        );

        long timeAfterReport;
        if(Configure.isUseThreadToDoReport()){
            ReporterFactory.getReporter(reportReqData).run();
            timeAfterReport = System.currentTimeMillis();
            Util.log("pay+report总耗时（异步方式上报）："+(timeAfterReport-costTimeStart) + "ms");
        }else{
            ReportService.request(reportReqData);
            timeAfterReport = System.currentTimeMillis();
            Util.log("pay+report总耗时（同步方式上报）："+(timeAfterReport-costTimeStart) + "ms");
        }


        if (refundQueryResData == null || refundQueryResData.getReturn_code() == null) {
            setResult("Case1:退款查询API请求逻辑错误，请仔细检测传过去的每一个参数是否合法，或是看API能否被正常访问",Log.LOG_TYPE_ERROR);
            resultListener.onFailByReturnCodeError(refundQueryResData);
            return;
        }

        //Debug:查看数据是否正常被填充到scanPayResponseData这个对象中
        //Util.reflect(refundQueryResData);

        if (refundQueryResData.getReturn_code().equals("FAIL")) {
            ///注意：一般这里返回FAIL是出现系统级参数错误，请检测Post给API的数据是否规范合法
            setResult("Case2:退款查询API系统返回失败，请检测Post给API的数据是否规范合法",Log.LOG_TYPE_ERROR);
            resultListener.onFailByReturnCodeFail(refundQueryResData);
        } else {
            log.i("退款查询API系统成功返回数据");
            //--------------------------------------------------------------------
            //收到API的返回数据的时候得先验证一下数据有没有被第三方篡改，确保安全
            //--------------------------------------------------------------------

            if (!Signature.checkIsSignValidFromResponseString(refundQueryServiceResponseString)) {
                setResult("Case3:退款查询API返回的数据签名验证失败，有可能数据被篡改了",Log.LOG_TYPE_ERROR);
                resultListener.onFailBySignInvalid(refundQueryResData);
                return;
            }

            if (refundQueryResData.getResult_code().equals("FAIL")) {
                Util.log("出错，错误码：" + refundQueryResData.getErr_code() + "     错误信息：" + refundQueryResData.getErr_code_des());
                setResult("Case4:【退款查询失败】",Log.LOG_TYPE_ERROR);
                resultListener.onRefundQueryFail(refundQueryResData);
                //退款失败时再怎么延时查询退款状态都没有意义，这个时间建议要么再手动重试一次，依然失败的话请走投诉渠道进行投诉
            } else {
                //退款成功
                getRefundOrderListResult(refundQueryServiceResponseString);
                setResult("Case5:【退款查询成功】",Log.LOG_TYPE_INFO);
                resultListener.onRefundQuerySuccess(refundQueryResData);
            }
        }
    }

    /**
     * 打印出服务器返回的订单查询结果
     * @param refundQueryResponseString 退款查询返回API返回的数据
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private void getRefundOrderListResult(String refundQueryResponseString) throws ParserConfigurationException, SAXException, IOException {
        List<RefundOrderData> refundOrderList = XMLParser.getRefundOrderList(refundQueryResponseString);
        int count = 1;
        for(RefundOrderData refundOrderData : refundOrderList){
            Util.log("退款订单数据NO" + count + ":");
            Util.log(refundOrderData.toMap());
            orderListResult += refundOrderData.toMap().toString();
            count++;
        }
        log.i("查询到的结果如下：");
        log.i(orderListResult);
    }

    public void setRefundQueryService(RefundQueryService service) {
        refundQueryService = service;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        RefundQueryBusiness.result = result;
    }

    public void setResult(String result,String type){
        setResult(result);
        log.log(type,result);
    }

}
