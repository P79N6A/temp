package com.dfire.soa.item.partner.rocketmq;

/**
 * Created by yangcong on 2016/9/26 0026.
 */
public interface IItemRmqService {

    /**
     * rocketMq消息发送
     *
     * @param tag
     * @param message
     */
    public void rocketMqTransmit(String tag, Object message);

}
