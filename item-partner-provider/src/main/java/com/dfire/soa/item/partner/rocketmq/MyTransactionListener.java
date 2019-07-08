package com.dfire.soa.item.partner.rocketmq;

import com.dfire.soa.item.platform.constants.CommonConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.annotation.Resource;

/**
 * @Author: xiaoji
 * @Date: create on 2018/7/3
 * @Describle:
 */
@Component
@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
public class MyTransactionListener {

    @Resource
    private IItemRmqService itemRmqService;

    @TransactionalEventListener(fallbackExecution = true)
    public void onApplicationEvent(MyTransactionEvent event) {
        if (event.getCallBack() != null) {
            event.getCallBack().process(event.getDataMap());
        }
        itemRmqService.rocketMqTransmit(event.getTag(), event.getDataMap());
    }
}