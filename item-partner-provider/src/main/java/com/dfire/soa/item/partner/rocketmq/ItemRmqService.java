package com.dfire.soa.item.partner.rocketmq;

import com.dfire.soa.item.partner.constant.CommonConstant;
import com.twodfire.async.message.client.sender.SendManagerFacade;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

@Aspect
public class ItemRmqService implements IItemRmqService {

	private static Logger rocketMqLogger = LoggerFactory.getLogger(CommonConstant.ROCKET_MQ);

	private SendManagerFacade rmqSendManager;

	@Async("onsAsyncExecutor")
	@Override
	public void rocketMqTransmit(String tag, Object message) {
		String msgId = rmqSendManager.sendMsg(tag, message, Boolean.FALSE);
		if (msgId == null) {
			rocketMqLogger.info("rocket_mq消息发送失败！tag:" + tag + ",message:" + message);
		} else {
			rocketMqLogger.warn("rocket_mq消息发送成功！msgId:" + msgId + ",tag:" + tag + ",message:" + message);
		}
	}

	public void setRmqSendManager(SendManagerFacade rmqSendManager) {
		this.rmqSendManager = rmqSendManager;
	}
}
