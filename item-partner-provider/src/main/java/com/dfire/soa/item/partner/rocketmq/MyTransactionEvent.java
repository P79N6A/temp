package com.dfire.soa.item.partner.rocketmq;

import org.springframework.context.ApplicationEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: xiaoji
 * @Date: create on 2018/7/3
 * @Describle:
 */
public class MyTransactionEvent extends ApplicationEvent {

    private String tag;
    private Map<String, String> dataMap;
    private ITransactionEventCallBack callBack;

    public MyTransactionEvent(String tag, HashMap<String, String> dataMap, Object source) {
        super(source);
        this.tag = tag;
        this.dataMap = dataMap;
    }

    public ITransactionEventCallBack getCallBack() {
        return callBack;
    }

    public void setCallBack(ITransactionEventCallBack callBack) {
        this.callBack = callBack;
    }

    public String getTag() {
        return tag;
    }

    public Map<String, String> getDataMap() {
        return dataMap;
    }
}

