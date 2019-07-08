package com.dfire.soa.item.partner.koubei.message;

import com.alibaba.fastjson.JSON;
import com.dfire.rest.util.common.util.OpenRestUtil;
import com.dfire.soa.item.bo.MenuAddition;
import com.dfire.soa.item.partner.constant.CommonConstant;
import com.dfire.soa.item.partner.constant.LockCacheKey;
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
 * @Author: xiaoji
 * @Date: create on 2018/11/6
 * @Describle:处理菜品加料的关联关系
 */
@Component
@MessageTag(tag = {CommonConstant.Notify.MENU_ADDITION_INSERT_TAG, CommonConstant.Notify.MENU_ADDITION_UPDATE_TAG})
public class KoubeiDishMenuAdditionMsgHandler implements ConsumerCallBack {

	@Resource
	private KoubeiDishHandler koubeiDishHandler;
	@Resource
	private KouBeiCheckUtil kouBeiCheckUtil;
	@Resource
	private CodisService codisService;

	/** 日志：业务 */
	private static final Logger bizLog = LoggerFactory.getLogger(CommonConstant.BIZ_LOG);

	@Override
	public boolean process(AsyncMsg msg) {
		HashMap<String, String> dataMap = null;
		String tag = null;
		Boolean flag = Boolean.TRUE;
		try {
			tag = msg.getTag();
			dataMap = msg.getContent();
			// 控制消息的超时时间
			/*if (OpenRestUtil.isResend(msg.getStartDeliverTime(), CommonConstant.MQ_MESSAGE_EXPIRE_TIME)) {
				bizLog.info("[kb_databack][info] msg has been expired. tag: {}, msgId: {}", tag, msg.getMsgID());
				return flag;
			}*/

			//绑定关系校验
			ShopBindVo shopBindVo = kouBeiCheckUtil.getShopBindVo(dataMap.get("entity_id"));
			if (shopBindVo == null || shopBindVo.getBindStatus() != 1 || StringUtils.isBlank(shopBindVo.getMerchantId()) || StringUtils.isBlank(shopBindVo.getShopId()) ||
					(StringUtils.isNotBlank(shopBindVo.getExtendFields()) && Objects.equals(JSON.parseObject(shopBindVo.getExtendFields(), ShopBindExtendFieldsVo.class).getSyncStatus(), 3))) {
				return flag;
			}
			String merchantId = shopBindVo.getMerchantId();
			String shopId = shopBindVo.getShopId();
			MenuAddition menuAdditionAfter = JSON.parseObject(dataMap.get("data_after"), MenuAddition.class);

			bizLog.info("[kb_databack] sync dish menuAddition. tag: {}, entityId: {}, menuId: {}, additionId: {}, msgId: {}", tag, menuAdditionAfter.getEntityId(), menuAdditionAfter.getMenuId(), menuAdditionAfter.getAdditionId(), msg.getMsgID());

			//口碑-菜品加料tag处理
			flag = addLockForMenuAddition(tag, menuAdditionAfter, merchantId, shopId, msg);

		} catch (BizException e) {
			bizLog.warn("[kb_databack]KoubeiDishMenuAdditionMsgHandler.process(msg) BizException. tag: " + JSON.toJSONString(tag) + ", dataMap:" + JSON.toJSONString(dataMap), e);
			return flag;
		} catch (Exception e) {
			bizLog.error("[kb_databack]KoubeiDishMenuAdditionMsgHandler.process(msg) Exception. tag: " + JSON.toJSONString(tag) + ", dataMap:" + JSON.toJSONString(dataMap), e);
			return false;
		}
		return flag;
	}

	/**
	 * 导数据时加锁
	 *
	 * @param menuAdditionAfter
	 * @param merchantId
	 * @param shopId
	 * @param msg
	 * @return
	 */
	private Boolean addLockForMenuAddition(String tag,MenuAddition menuAdditionAfter, String merchantId, String shopId, AsyncMsg msg) {
		Boolean flag = Boolean.TRUE;
		try {
			//加锁
			String entityId = menuAdditionAfter.getEntityId();
			String menuId = menuAdditionAfter.getMenuId();
			String lockKey = new LockCacheKey().builderAddMenuAdditionLock(entityId, menuId);
			Boolean result = isLock(lockKey);
			long waitingTime = 0L;
			while (result && waitingTime <= 200) {
				try {
					Thread.sleep(50L);
					waitingTime += 50L;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				result = isLock(lockKey);
			}
			try {
				switch (tag) {
					case CommonConstant.Notify.MENU_ADDITION_UPDATE_TAG://菜品加料删除
						flag = koubeiDishHandler.pushkoubeiDishAddAndUpdate(merchantId, shopId, menuAdditionAfter.getMenuId(), menuAdditionAfter.getEntityId(),1);
					case CommonConstant.Notify.MENU_ADDITION_INSERT_TAG://菜品加料新增
						flag = koubeiDishHandler.pushkoubeiDishAddAndUpdate(merchantId, shopId, menuAdditionAfter.getMenuId(), menuAdditionAfter.getEntityId(),1);
				}
			} catch (Exception e) {
				bizLog.error("菜品关联加料消息消费失败!", e);
			} finally {
				//释放锁
				unLock(lockKey);
			}
		} catch (Exception e) {
			bizLog.error("同步商品加料消息消费失败! data:" + JSON.toJSONString(msg.getContent()), e);
			return Boolean.FALSE;
		}
		return flag;
	}

	private Boolean isLock(String lockKey) {
		Long lockResult = codisService.setnx(lockKey, 1, "1");
		if (lockResult == 1L) {
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	void unLock(String lockKey) {
		try {
			codisService.del(lockKey);
		} catch (Exception e) {
			bizLog.error("redis clearCache error:", e);
		}
	}
}
