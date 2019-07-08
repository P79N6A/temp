package com.dfire.soa.item.partner.koubei.message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dfire.rest.util.common.util.OpenRestUtil;
import com.dfire.soa.item.partner.constant.CommonConstant;
import com.dfire.soa.item.partner.koubei.KouBeiCheckUtil;
import com.dfire.soa.item.partner.koubei.handler.KoubeiDishSelloutHandler;
import com.dfire.soa.msstate.notify.MsstateMessageVo;
import com.dfire.soa.thirdbind.vo.ShopBindExtendFieldsVo;
import com.dfire.soa.thirdbind.vo.ShopBindVo;
import com.twodfire.async.message.client.consumer.support.ConsumerCallBack;
import com.twodfire.async.message.client.consumer.support.annotation.MessageTag;
import com.twodfire.async.message.client.to.AsyncMsg;
import com.twodfire.exception.BizException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * Created by GanShu on 2018/6/1 0001.
 */
@Component
@MessageTag(tag = {CommonConstant.Notify.MENU_SELLOUT_TAG, CommonConstant.Notify.MENU_SELLOUT_CLOUD_TAG})
public class KoubeiDishSelloutMsgHandler implements ConsumerCallBack {

    /**
     * 日志：业务
     */
    private static Logger bizLog = LoggerFactory.getLogger(CommonConstant.BIZ_LOG);

    @Resource
    private KoubeiDishSelloutHandler koubeiDishSelloutHandler;
    @Resource
    private KouBeiCheckUtil kouBeiCheckUtil;

    @Override
    public boolean process(AsyncMsg msg) {
        MsstateMessageVo messageVo = null;
        String tag = null;
        String entityId = null;
        String merchantId = null;
        try {
            tag = msg.getTag();
            messageVo = msg.getContent();
            entityId = messageVo.getEntityId();
            // 控制消息的超时时间
            if(OpenRestUtil.isResend(msg.getStartDeliverTime(), CommonConstant.MQ_MESSAGE_EXPIRE_TIME)) {
                bizLog.info("[koubei item sellout][info] msg has been expired. tag: {}, msgId: {}", tag, msg.getMsgID());
                return true;
            }
            if (!CommonConstant.Notify.MENU_SELLOUT_TAG.equals(tag) && !CommonConstant.Notify.MENU_SELLOUT_CLOUD_TAG.equals(tag)) {
                bizLog.info("[koubei item sellout] dish sellout. tag is not match. tag: {}, msgId: {}", tag, msg.getMsgID());
                return true;
            }

			bizLog.info("[kb_databack] sync dish sellout. tag: {}, messageVo: {}, msgId: {}", tag, JSONObject.toJSON(messageVo), msg.getMsgID());

            //绑定关系校验
            ShopBindVo shopBindVo = kouBeiCheckUtil.getShopBindVo(messageVo.getEntityId());
            if (shopBindVo==null || shopBindVo.getBindStatus()!=1 || StringUtils.isBlank(shopBindVo.getMerchantId()) || StringUtils.isBlank(shopBindVo.getShopId()) ||
                    (StringUtils.isNotBlank(shopBindVo.getExtendFields()) && Objects.equals(JSON.parseObject(shopBindVo.getExtendFields(), ShopBindExtendFieldsVo.class).getSyncStatus(), 3))){
                return true;
            }
            merchantId = shopBindVo.getMerchantId();
            String shopId = shopBindVo.getShopId();
            return koubeiDishSelloutHandler.dishSellout(messageVo, merchantId, shopId);
        }catch (BizException e) {
            bizLog.warn("[koubei item sellout] KoubeiDishSelloutMsgHandler.process(msg) BizException. tag: " + JSON.toJSONString(tag) + ", messageVo:" + JSON.toJSONString(messageVo), e);
            return true;
        }catch (Exception e) {
            bizLog.error("[koubei item sellout] fail to sellout dish. entityId: {}, merchantId: {}, msgId: {}", entityId, merchantId, msg.getMsgID(), e);
            return false;
        }
    }



}
