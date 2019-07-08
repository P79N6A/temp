package com.dfire.soa.item.partner.koubei.message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dfire.rest.util.common.util.OpenRestUtil;
import com.dfire.soa.item.bo.KindMenu;
import com.dfire.soa.item.partner.constant.CommonConstant;
import com.dfire.soa.item.partner.koubei.KouBeiCheckUtil;
import com.dfire.soa.item.partner.koubei.handler.KoubeiKindMenuHandler;
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
 * 消息处理-菜类
 */
@Component
@MessageTag(tag = {CommonConstant.Notify.KIND_MENU_DELETE_TAG, CommonConstant.Notify.KIND_MENU_INSERT_TAG, CommonConstant.Notify.KIND_MENU_UPDATE_TAG})
public class KoubeiKindMenuMsgHandler implements ConsumerCallBack {
    @Resource
    private KoubeiKindMenuHandler koubeiKindMenuHandler;
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
            KindMenu kindMenuBefore = JSON.parseObject(dataMap.get("data_before"), KindMenu.class);
            KindMenu kindMenuAfter = JSON.parseObject(dataMap.get("data_after"), KindMenu.class);
            boolean isDeleteTag = Objects.equals(tag, CommonConstant.Notify.KIND_MENU_DELETE_TAG) || (Objects.equals(tag, CommonConstant.Notify.KIND_MENU_UPDATE_TAG) && kindMenuAfter==null);

            //绑定关系校验
            ShopBindVo shopBindVo = kouBeiCheckUtil.getShopBindVo(dataMap.get("entity_id"));
            if (shopBindVo==null || shopBindVo.getBindStatus()!=1 || StringUtils.isBlank(shopBindVo.getMerchantId()) || StringUtils.isBlank(shopBindVo.getShopId()) ||
                    (StringUtils.isNotBlank(shopBindVo.getExtendFields()) && Objects.equals(JSON.parseObject(shopBindVo.getExtendFields(), ShopBindExtendFieldsVo.class).getSyncStatus(), 3) && !isDeleteTag)){
                return true;
            }
            String merchantId = shopBindVo.getMerchantId();
            String shopId = shopBindVo.getShopId();

			bizLog.info("[kb_databack] sync dish kindMenu. tag: {}, kindMenuAfter:{}, msgId: {}", tag, JSONObject.toJSON(kindMenuAfter), msg.getMsgID());

            //口碑-菜类tag处理
            switch (tag) {
                case CommonConstant.Notify.KIND_MENU_INSERT_TAG://菜类-新增
					if (kindMenuAfter.getIsInclude() != KindMenu.TYPE_ADDITION) {
						return koubeiKindMenuHandler.insertOrUpdate(merchantId, shopId, kindMenuAfter);
					}
                case CommonConstant.Notify.KIND_MENU_UPDATE_TAG://菜类-更新
					if (kindMenuAfter.getIsInclude() != KindMenu.TYPE_ADDITION) {
						if (kindMenuAfter != null && kindMenuAfter.getIsValid() == 1) {
							return koubeiKindMenuHandler.insertOrUpdate(merchantId, shopId, kindMenuAfter);
						} else {
							return koubeiKindMenuHandler.delete(merchantId, shopId, kindMenuBefore);
						}
					}
                case CommonConstant.Notify.KIND_MENU_DELETE_TAG://菜类-删除
					if (kindMenuAfter.getIsInclude() != KindMenu.TYPE_ADDITION) {
						return koubeiKindMenuHandler.delete(merchantId, shopId, kindMenuAfter);
					}
            }
        }catch (BizException e) {
            bizLog.warn("[kb_databack]KoubeiKindMenuMsgHandler.process(msg) BizException. tag: " + JSON.toJSONString(tag) + ", dataMap:" + JSON.toJSONString(dataMap), e);
            return true;
        }catch (Exception e) {
            bizLog.error("[kb_databack]KoubeiKindMenuMsgHandler.process(msg) Exception. tag: " + JSON.toJSONString(tag) + ", dataMap:" + JSON.toJSONString(dataMap), e);
            return false;
        }
        return true;
    }
}
