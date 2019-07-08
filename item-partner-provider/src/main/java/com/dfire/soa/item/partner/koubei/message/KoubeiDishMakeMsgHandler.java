package com.dfire.soa.item.partner.koubei.message;

import com.alibaba.fastjson.JSON;
import com.dfire.rest.util.common.util.OpenRestUtil;
import com.dfire.soa.item.bo.MenuMake;
import com.dfire.soa.item.partner.constant.CommonConstant;
import com.dfire.soa.item.partner.koubei.KouBeiCheckUtil;
import com.dfire.soa.item.partner.koubei.handler.KoubeiDishHandler;
import com.dfire.soa.thirdbind.vo.ShopBindExtendFieldsVo;
import com.dfire.soa.thirdbind.vo.ShopBindVo;
import com.twodfire.async.message.client.consumer.support.ConsumerCallBack;
import com.twodfire.async.message.client.consumer.support.annotation.MessageTag;
import com.twodfire.async.message.client.to.AsyncMsg;
import com.twodfire.exception.BizException;
import com.twodfire.redis.CodisService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Objects;

/**
 * 消息处理-菜品做法
 * Created by heque on 2018/6/28 0028.
 */
@Component
@MessageTag(tag = {CommonConstant.Notify.MENU_MAKE_INSERT_TAG, CommonConstant.Notify.MENU_MAKE_UPDATE_TAG})
public class KoubeiDishMakeMsgHandler implements ConsumerCallBack {
    @Resource
    private KoubeiDishHandler koubeiDishHandler;
    @Resource
    private KouBeiCheckUtil kouBeiCheckUtil;
    @Resource
    private CodisService codisService;
//    @Resource
//    private CodisService codisService;

    /** 日志：业务 */
    private static final Logger bizLog = LoggerFactory.getLogger(CommonConstant.BIZ_LOG);

    @Override
    public boolean process(AsyncMsg msg) {
        HashMap<String, String> dataMap = null;
        String tag = null;
        try{
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
            MenuMake menuMakeAfter = JSON.parseObject(dataMap.get("data_after"), MenuMake.class);

            //批处理去重
            Long cacheFlag = codisService.setnx("DishMake"+ menuMakeAfter.getMenuId() + menuMakeAfter.getOpTime(), 5, "true");
            if (cacheFlag==null || cacheFlag<=0) {
                return true;
            }

            //口碑-菜品tag处理
            switch (tag){
                case CommonConstant.Notify.MENU_MAKE_INSERT_TAG://菜品做法-新增
                    return koubeiDishHandler.pushkoubeiDishAddAndUpdate(merchantId, shopId, menuMakeAfter.getMenuId(), menuMakeAfter.getEntityId(),0);
                case CommonConstant.Notify.MENU_MAKE_UPDATE_TAG://菜品做法-修改
                    return koubeiDishHandler.pushkoubeiDishAddAndUpdate(merchantId, shopId, menuMakeAfter.getMenuId(), menuMakeAfter.getEntityId(),0);
            }
        }catch (BizException e) {
            bizLog.warn("[kb_databack]KoubeiDishMakeMsgHandler.process(msg) BizException. tag: " + JSON.toJSONString(tag) + ", dataMap:" + JSON.toJSONString(dataMap), e);
            return true;
        }catch (Exception e) {
            bizLog.error("[kb_databack]KoubeiDishMakeMsgHandler.process(msg) Exception. tag: " + JSON.toJSONString(tag) + ", dataMap:" + JSON.toJSONString(dataMap), e);
            return false;
        }
        return true;
    }
}
