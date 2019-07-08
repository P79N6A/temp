/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dfire.soa.item.partner.constant;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.rpc.*;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.alibaba.fastjson.JSON;
import com.dfire.magiceye.util.TraceUtils;
import com.dfire.soa.item.platform.constants.ErrorMessageEnum;
import com.twodfire.exception.BizException;
import com.twodfire.exception.BizValidateException;
import com.twodfire.share.result.ResultSupport;

/**
 * ExceptionInvokerFilter
 * <p/>
 * 功能：
 * <ol>
 * <li>不期望的异常打ERROR日志（Provider端）<br>
 * 不期望的日志即是，没有的接口上声明的Unchecked异常。
 * <li>异常不在API包中，则Wrap一层RuntimeException。<br>
 * RPC对于第一层异常会直接序列化传输(Cause异常会String化)，避免异常在Client出不能反序列化问题。
 * </ol>
 *
 * @author william.liangf
 * @author ding.lid
 */
@Activate(group = Constants.PROVIDER)
public class ExceptionFilter implements Filter {

    private Logger loggerERR = LoggerFactory.getLogger(CommonConstant.ERROR) ;

    private Logger loggerBiz = LoggerFactory.getLogger(CommonConstant.BIZ_LOG);

    private Logger loggerAlert = LoggerFactory.getLogger(CommonConstant.ALERT_MONITOR);

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) {
        Result r;
        com.twodfire.share.result.Result commonResult;
        try {
            Result result = invoker.invoke(invocation);
            if (result.hasException() && GenericService.class != invoker.getInterface()) {
                Throwable exception = result.getException();

                commonResult = new ResultSupport();
                commonResult.setSuccess(false);

                if (exception instanceof BizValidateException) {
                    loggerBiz.error(getInvokeInfo(invocation)
                            + ", exception: " + exception.getMessage() + ", traceId{}:" + TraceUtils.traceId(), exception);

                    commonResult.setMessage(exception.getMessage());
                    commonResult.setResultCode(((BizValidateException) exception).getCode());
                    r = new RpcResult(commonResult);
                    return r;
                } else if (exception instanceof BizException) {
                    loggerBiz.error(getInvokeInfo(invocation)
                            + ", exception: " + exception.getMessage() + ", traceId{}:" + TraceUtils.traceId(), exception);

                    commonResult.setMessage(exception.getMessage());
                    commonResult.setResultCode(((BizException) exception).getCode());
                    r = new RpcResult(commonResult);
                    return r;
                } else if (exception instanceof RpcException) {
                    loggerERR.error(getInvokeInfo(invocation)
                            + ", exception: " + exception.getMessage() + ", traceId{}:" + TraceUtils.traceId(), exception);
                    loggerAlert.error(getInvokeInfo(invocation)
                            + ", exception: " + exception.getMessage() + ", traceId{}:" + TraceUtils.traceId(), exception);
                    commonResult.setMessage(ErrorMessageEnum.SYSTEM.getMessage());
                    commonResult.setResultCode(ErrorMessageEnum.SYSTEM.getCode());
                    r = new RpcResult(commonResult);
                    return r;
                } else {
                    loggerERR.error(getInvokeInfo(invocation)
                            + ", exception: " + exception.getMessage() + ", traceId{}:" + TraceUtils.traceId(), exception);
                    loggerAlert.error(getInvokeInfo(invocation)
                            + ", exception: " + exception.getMessage() + ", traceId{}:" + TraceUtils.traceId(), exception);
                    commonResult.setMessage(ErrorMessageEnum.SYSTEM.getMessage());
                    commonResult.setResultCode(ErrorMessageEnum.SYSTEM.getCode());
                    r = new RpcResult(commonResult);
                    return r;
                }
            }
            return result;
        } catch (Exception exception) {
            loggerERR.error(getInvokeInfo(invocation)
                    + ", exception: " + exception.getMessage() + ", traceId{}:" + TraceUtils.traceId(), exception);
            loggerAlert.error(getInvokeInfo(invocation)
                    + ", exception: " + exception.getMessage() + ", traceId{}:" + TraceUtils.traceId(), exception);
            commonResult = new ResultSupport();
            commonResult.setSuccess(false);
            commonResult.setMessage(ErrorMessageEnum.SYSTEM.getMessage());
            commonResult.setResultCode(ErrorMessageEnum.SYSTEM.getCode());
            r = new RpcResult(commonResult);
            return r;
        }
    }

    private String getInvokeInfo(Invocation invocation) {
        StringBuilder sb = new StringBuilder(128);
        sb.append("invoke ")
                .append(invocation.getInvoker().getUrl().getServiceInterface())
                .append(".")
                .append(invocation.getMethodName())
                .append("(");
        Object[] args = invocation.getArguments();
        if (args != null) {
            try {
                String argsStr = JSON.toJSONString(args);
                argsStr = argsStr.substring(1, argsStr.length() - 1);
                argsStr = argsStr.replace("\"", "'");
                sb.append(argsStr);
            } catch (Exception e) {

            }
        }
        sb.append(")");
        return sb.toString();
    }
}