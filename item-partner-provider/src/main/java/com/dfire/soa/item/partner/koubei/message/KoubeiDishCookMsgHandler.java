package com.dfire.soa.item.partner.koubei.message;

import com.alibaba.fastjson.JSON;
import com.dfire.rest.util.common.util.OpenRestUtil;
import com.dfire.soa.item.partner.constant.CommonConstant;
import com.dfire.soa.item.partner.koubei.KouBeiCheckUtil;
import com.dfire.soa.item.partner.koubei.handler.KoubeiCookDishHandler;
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
 * 处理菜谱信息
 * Created by GanShu on 2018/5/8 0008.
 */
@Component
@MessageTag(tag = {CommonConstant.Notify.COOK_INSERT_TAG, CommonConstant.Notify.COOK_UPDATE_TAG, CommonConstant.Notify.COOK_DELETE_TAG, CommonConstant.Notify.COOK_DETAIL_INSERT_TAG, CommonConstant.Notify.COOK_DETAIL_UPDATE_TAG, CommonConstant.Notify.COOK_DETAIL_DELETE_TAG})
public class KoubeiDishCookMsgHandler implements ConsumerCallBack {

	/**
	 * 日志：业务
	 */
	private static Logger bizLog = LoggerFactory.getLogger(CommonConstant.BIZ_LOG);

	@Resource
	private KoubeiCookDishHandler koubeiCookDishHandler;

	@Resource
	private KouBeiCheckUtil kouBeiCheckUtil;

	@Override
	public boolean process(AsyncMsg msg) {
		HashMap<String, String> dataMap = null;
		String tag = null;
		String cookId;
		String menuId;
		String entityId;
		try {
			tag = msg.getTag();
			dataMap = msg.getContent();
			dataMap.put("msgId", msg.getMsgID());
			entityId = dataMap.get("entity_id");
			cookId = dataMap.get("cook_id");
            menuId = dataMap.get("menu_id");
			//todo 控制消息的超时时间
			if(OpenRestUtil.isResend(msg.getStartDeliverTime(), CommonConstant.MQ_MESSAGE_EXPIRE_TIME)) {
				bizLog.info("[kb_databack][info] msg has been expired. tag: {}, entityId: {}, menuId:{}, msgId: {}", tag, entityId, menuId, msg.getMsgID());
				return true;
			}

			//绑定关系校验
			ShopBindVo shopBindVo = kouBeiCheckUtil.getShopBindVo(entityId);
			if (shopBindVo==null || shopBindVo.getBindStatus()!=1 || StringUtils.isBlank(shopBindVo.getMerchantId()) || StringUtils.isBlank(shopBindVo.getShopId()) ||
					(StringUtils.isNotBlank(shopBindVo.getExtendFields()) && Objects.equals(JSON.parseObject(shopBindVo.getExtendFields(), ShopBindExtendFieldsVo.class).getSyncStatus(), 3))){
				return true;
			}
			String merchantId = shopBindVo.getMerchantId();
			String shopId = shopBindVo.getShopId();

			bizLog.info("[kb_databack] sync dish cook. tag: {}, entityId: {}, cookId: {}, menuId: {}, msgId: {}", tag, entityId, cookId, menuId, msg.getMsgID());
			switch (tag) {
				case CommonConstant.Notify.COOK_INSERT_TAG:
					return koubeiCookDishHandler.addCook(dataMap, merchantId, shopId);
				case CommonConstant.Notify.COOK_UPDATE_TAG:
					return koubeiCookDishHandler.updateCook(dataMap, merchantId, shopId);
				case CommonConstant.Notify.COOK_DELETE_TAG:
					return koubeiCookDishHandler.deleteCook(dataMap, merchantId, shopId);
				case CommonConstant.Notify.COOK_DETAIL_INSERT_TAG:
					return koubeiCookDishHandler.addCookDetail(dataMap, merchantId, shopId);
				case CommonConstant.Notify.COOK_DETAIL_UPDATE_TAG:
					return koubeiCookDishHandler.updateCookDetail(dataMap, merchantId, shopId);
				case CommonConstant.Notify.COOK_DETAIL_DELETE_TAG:
					return koubeiCookDishHandler.deleteCookDetail(dataMap, merchantId, shopId);
			}
		}catch (BizException e) {
			bizLog.warn("[kb_databack]KoubeiDishCookMsgHandler.process(msg) BizException. tag: " + JSON.toJSONString(tag) + ", dataMap:" + JSON.toJSONString(dataMap), e);
			return true;
		}catch (Exception e) {
			bizLog.error("[kb_databack]KoubeiDishCookMsgHandler.process(msg) Exception. tag: " + JSON.toJSONString(tag) + ", dataMap:" + JSON.toJSONString(dataMap), e);
			return false;
		}
		return true;
	}
}
