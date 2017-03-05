<%--
  Created by IntelliJ IDEA.
  User: gezhi
  Date: 2017/3/5
  Time: 18:17
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=utf-8"
         pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page isELIgnored="false" %>

<html>
<head>
    <title>Title</title>

    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=0">
    <title>Weixin Pay demo</title>
    <link rel="stylesheet" href="http://cdn.staticfile.org/twitter-bootstrap/4.0.0-alpha.5/css/bootstrap.min.css" />
</head>
<body>

<div class="container">
    <div class="row">
        <div class="card">
            <div class="card-block">
                <button class="btn btn-warning" onclick="callJsApi();return false;">立即支付</button>
            </div>
        </div>
    </div>
</div>


<script>
    function callJsApi() {
        alert("aa")
        onBridgeReady()
    }
    function onBridgeReady(){
        WeixinJSBridge.invoke(
            'getBrandWCPayRequest',
            <c:out value="${wxparam}" escapeXml="false" />,
        function(res){
            if(res.err_msg == "get_brand_wcpay_request：ok" ) {}     // 使用以上方式判断前端返回,微信团队郑重提示：res.err_msg将在用户支付成功后返回    ok，但并不保证它绝对可靠。
        }
    );
    }
    if (typeof WeixinJSBridge == "undefined"){
        if( document.addEventListener ){
            document.addEventListener('WeixinJSBridgeReady', onBridgeReady, false);
        }else if (document.attachEvent){
            document.attachEvent('WeixinJSBridgeReady', onBridgeReady);
            document.attachEvent('onWeixinJSBridgeReady', onBridgeReady);
        }
    }else{
        //onBridgeReady();
    }
</script>

</body>
</html>
