package com.dfire.soa.item.partner.rocketmq;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: luoshi
 * Date: 2018/7/11
 * Time: 下午2:47
 * To change this template use File | Settings | File Templates.
 * Description:
 * 事务提交后的回调
 */
public interface ITransactionEventCallBack {

    /**
     * 处理必须在事务提交后才能设置的数据（例如：查询menu需要查询item和item_cash_config）
     *
     * @param dataMap
     */
    void process(Map<String, String> dataMap);

}
