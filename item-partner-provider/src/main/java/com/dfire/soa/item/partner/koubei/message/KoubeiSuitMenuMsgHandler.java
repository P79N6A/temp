package com.dfire.soa.item.partner.koubei.message;

import com.alibaba.fastjson.JSON;
import com.dfire.rest.util.common.util.OpenRestUtil;
import com.dfire.soa.item.bo.SuitMenuChange;
import com.dfire.soa.item.bo.SuitMenuDetail;
import com.dfire.soa.item.partner.constant.CommonConstant;
import com.dfire.soa.item.partner.koubei.KouBeiCheckUtil;
import com.dfire.soa.item.partner.koubei.handler.KoubeiSuitMenuHandler;
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
import java.util.HashMap;
import java.util.Objects;

/**
 * 消息处理-菜品组
 * Created by zhishi on 2018/5/11 0011.
 */
@Component
@MessageTag(tag = {CommonConstant.Notify.SUIT_MENU_CHANGE_INSERT_TAG, CommonConstant.Notify.SUIT_MENU_CHANGE_UPDATE_TAG, CommonConstant.Notify.SUIT_MENU_DETAIL_INSERT_TAG, CommonConstant.Notify.SUIT_MENU_DETAIL_UPDATE_TAG})
public class KoubeiSuitMenuMsgHandler implements ConsumerCallBack {
    @Resource
    private KoubeiSuitMenuHandler koubeiSuitMenuHandler;
    @Resource
    private KouBeiCheckUtil kouBeiCheckUtil;
    private static final Logger bizLog = LoggerFactory.getLogger(CommonConstant.BIZ_LOG);

    @Override
    public boolean process(AsyncMsg msg) {
        HashMap<String, String> dataMap = null;
        String tag = null;
        try {
            tag = msg.getTag();
            dataMap = msg.getContent();
            // 控制消息的超时时间
            if(OpenRestUtil.isResend(msg.getStartDeliverTime(), CommonConstant.MQ_MESSAGE_EXPIRE_TIME)) {
                bizLog.info("[kb_databack][info] msg has been expired. tag: {}, msgId: {}", tag, msg.getMsgID());
                return true;
            }

            //绑定关系校验
            ShopBindVo shopBindVo = kouBeiCheckUtil.getShopBindVo(dataMap.get("entity_id"));
            if (shopBindVo==null || shopBindVo.getBindStatus()!=1 || StringUtils.isBlank(shopBindVo.getMerchantId()) || StringUtils.isBlank(shopBindVo.getShopId()) ||
                    (StringUtils.isNotBlank(shopBindVo.getExtendFields()) && Objects.equals(JSON.parseObject(shopBindVo.getExtendFields(), ShopBindExtendFieldsVo.class).getSyncStatus(), 3))){
                return true;
            }
            String merchantId = shopBindVo.getMerchantId();
            String shopId = shopBindVo.getShopId();
            SuitMenuDetail suitMenuDetailAfter;
            SuitMenuDetail suitMenuDetailBefore;
            SuitMenuChange suitMenuChangeAfter;
            SuitMenuChange suitMenuChangeBefore;

            bizLog.info("[kb_databack][info][group]KoubeiSuitMenuHandle.process(msg) BizException. tag: " + JSON.toJSONString(tag) + ", dataMap:" + JSON.toJSONString(dataMap));


            //口碑-菜品组tag处理
            switch (tag) {
                case CommonConstant.Notify.SUIT_MENU_DETAIL_INSERT_TAG:   //菜品组-新增
                    suitMenuDetailAfter = JSON.parseObject(dataMap.get("data_after"), SuitMenuDetail.class);
                    return koubeiSuitMenuHandler.insertOrUpdate(merchantId, shopId, suitMenuDetailAfter);
                case CommonConstant.Notify.SUIT_MENU_DETAIL_UPDATE_TAG:   //菜品组-修改
                    suitMenuDetailAfter = JSON.parseObject(dataMap.get("data_after"), SuitMenuDetail.class);
                    if(suitMenuDetailAfter != null && suitMenuDetailAfter.getIsValid()==1){
                        return koubeiSuitMenuHandler.insertOrUpdate(merchantId, shopId, suitMenuDetailAfter);
                    }else if(suitMenuDetailAfter != null && suitMenuDetailAfter.getIsValid()==0){
                        return koubeiSuitMenuHandler.delete(merchantId, shopId, suitMenuDetailAfter);
                    }else {
                        suitMenuDetailBefore = JSON.parseObject(dataMap.get("data_before"), SuitMenuDetail.class);
                        return koubeiSuitMenuHandler.delete(merchantId, shopId, suitMenuDetailBefore);
                    }
                case CommonConstant.Notify.SUIT_MENU_CHANGE_INSERT_TAG:   //菜品组-可选菜新增
                    suitMenuChangeAfter = JSON.parseObject(dataMap.get("data_after"), SuitMenuChange.class);
                    //koubeiSuitMenuHandler.syncAddCookDetail(suitMenuChangeAfter);
                    return koubeiSuitMenuHandler.insertOrUpdateBySuitMenuChange(merchantId, shopId, suitMenuChangeAfter);
                case CommonConstant.Notify.SUIT_MENU_CHANGE_UPDATE_TAG:          //菜品组-可选菜修改
                    suitMenuChangeAfter = JSON.parseObject(dataMap.get("data_after"), SuitMenuChange.class);
                    if (suitMenuChangeAfter != null && suitMenuChangeAfter.getIsValid() == 1) {
                        return koubeiSuitMenuHandler.insertOrUpdateBySuitMenuChange(merchantId, shopId, suitMenuChangeAfter);
                    }else {
                        suitMenuChangeBefore = JSON.parseObject(dataMap.get("data_before"), SuitMenuChange.class);
                        return koubeiSuitMenuHandler.insertOrUpdateBySuitMenuChange(merchantId, shopId, suitMenuChangeBefore);
                    }
            }
        }catch (BizException e){
            bizLog.warn("[kb_databack]KoubeiSuitMenuHandle.process(msg) BizException. tag: " + JSON.toJSONString(tag) + ", dataMap:" + JSON.toJSONString(dataMap), e);
            return true;
        }catch (Exception e){
            bizLog.error("[kb_databack]KoubeiSuitMenuHandle.process(msg) Exception. tag: " + JSON.toJSONString(tag) + ", dataMap:" + JSON.toJSONString(dataMap), e);
            return false;
        }
        return true;
    }
}
