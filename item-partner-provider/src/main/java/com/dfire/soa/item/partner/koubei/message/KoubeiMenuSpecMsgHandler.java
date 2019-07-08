package com.dfire.soa.item.partner.koubei.message;

import com.alibaba.fastjson.JSON;
import com.dfire.rest.util.common.util.OpenRestUtil;
import com.dfire.soa.item.bo.MenuSpecDetail;
import com.dfire.soa.item.partner.bo.CookDetailBO;
import com.dfire.soa.item.partner.bo.SpecBO;
import com.dfire.soa.item.partner.bo.SpecExtBO;
import com.dfire.soa.item.partner.bo.query.CookDetailQuery;
import com.dfire.soa.item.partner.constant.CommonConstant;
import com.dfire.soa.item.partner.constant.LockCacheKey;
import com.dfire.soa.item.partner.koubei.KouBeiCheckUtil;
import com.dfire.soa.item.partner.koubei.handler.KoubeiMenuSpecHandler;
import com.dfire.soa.item.partner.service.internal.ICookDetailInService;
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
import java.util.*;

/**
 * 消息处理--规格
 * Created by heque on 2018/5/16 0016.
 */
@Component
@MessageTag(tag = {CommonConstant.Notify.MENU_SPEC_DETAIL_INSERT_TAG, CommonConstant.Notify.MENU_SPEC_DETAIL_UPDATE_TAG})
public class KoubeiMenuSpecMsgHandler implements ConsumerCallBack {
    @Resource
    private KoubeiMenuSpecHandler koubeiMenuSpecHandler;
    @Resource
    private KouBeiCheckUtil kouBeiCheckUtil;

	@Resource
	private ICookDetailInService cookDetailInService;

	@Resource
	private CodisService codisService;

//	@Resource
//	private CodisService codisService;

	/** 日志：业务 */
    private static final Logger bizLog = LoggerFactory.getLogger(CommonConstant.BIZ_LOG);


    @Override
    public boolean process(AsyncMsg msg) {
		String tag = msg.getTag();
		Map<String, String> dataMap = msg.getContent();
		MenuSpecDetail specAfter = JSON.parseObject(dataMap.get("data_after"), MenuSpecDetail.class);

		// 控制消息的超时时间
		if (OpenRestUtil.isResend(msg.getStartDeliverTime(), CommonConstant.MQ_MESSAGE_EXPIRE_TIME)) {
			bizLog.info("[kb_databack][info] msg has been expired. tag: {}, msgId: {}", tag, msg.getMsgID());
			return true;
		}

		//同步菜品sku
		boolean flag1 = syncMenuSpecDetail(specAfter, tag, msg);
		//修改cookDetail、推送cookDetail消息
		boolean flag2 = syncCookDetailSpecDetail(specAfter, tag, msg);

		return flag1 && flag2;
	}

	/**
	 * 同步菜品sku
	 */
    private boolean syncMenuSpecDetail(MenuSpecDetail specAfter,String tag, AsyncMsg msg){
		Map<String, String> dataMap = msg.getContent();
		try {
			//绑定关系校验
			ShopBindVo shopBindVo = kouBeiCheckUtil.getShopBindVo(dataMap.get("entity_id"));
			if (shopBindVo==null || shopBindVo.getBindStatus()!=1 || StringUtils.isBlank(shopBindVo.getMerchantId()) || StringUtils.isBlank(shopBindVo.getShopId()) ||
					(StringUtils.isNotBlank(shopBindVo.getExtendFields()) && Objects.equals(JSON.parseObject(shopBindVo.getExtendFields(), ShopBindExtendFieldsVo.class).getSyncStatus(), 3))){
				return true;
			}
			String merchantId = shopBindVo.getMerchantId();
			String shopId = shopBindVo.getShopId();

			Boolean flag;
			//口碑-sku tag处理
			switch (tag) {
				case CommonConstant.Notify.MENU_SPEC_DETAIL_INSERT_TAG://菜品规格关联-新增
					return koubeiMenuSpecHandler.pushkoubeiMenuSpecAddAndUpdate(merchantId, shopId, specAfter);

				case CommonConstant.Notify.MENU_SPEC_DETAIL_UPDATE_TAG://菜品规格关联-修改（含删除）
					if(specAfter != null && specAfter.getIsValid() == 1 ){//更新
						return koubeiMenuSpecHandler.pushkoubeiMenuSpecAddAndUpdate(merchantId, shopId, specAfter);
					}else if(specAfter != null && specAfter.getIsValid() == 0 ){//删除
						MenuSpecDetail specBefore = JSON.parseObject(dataMap.get("data_before"), MenuSpecDetail.class);
						flag = koubeiMenuSpecHandler.pushkoubeiMenuSpecDelete(merchantId, shopId,specBefore);
						return flag;
					}
			}
		}catch (BizException e) {
			bizLog.warn("[kb_databack]KoubeiMenuSpecMsgHandler.process(msg) BizException. tag: " + JSON.toJSONString(tag) + ", dataMap:" + JSON.toJSONString(dataMap), e);
			return true;
		}catch (Exception e) {
			bizLog.error("[kb_databack]KoubeiMenuSpecMsgHandler.process(msg) Exception. tag: " + JSON.toJSONString(tag) + ", dataMap:" + JSON.toJSONString(dataMap), e);
			return false;
		}
		return true;
	}

	/**
	 * 修改cookDetail、推送cookDetail消息
	 */
    private Boolean syncCookDetailSpecDetail(MenuSpecDetail specAfter,String tag,AsyncMsg msg){
		try {
			//加锁
			String entityId = specAfter.getEntityId();
			String menuId = specAfter.getMenuId();
			String lockKey = new LockCacheKey().builderAddMenuSpecDetailLock(entityId, menuId);
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
				CookDetailQuery cookDetailQuery = new CookDetailQuery(entityId);
				cookDetailQuery.setMenuId(menuId);
				List<CookDetailBO> cookDetailBOList = cookDetailInService.selectByQuery(cookDetailQuery);

				switch (tag) {
					case CommonConstant.Notify.MENU_SPEC_DETAIL_INSERT_TAG:
						if (org.apache.commons.collections.CollectionUtils.isNotEmpty(cookDetailBOList)) {
							for (CookDetailBO cookDetailBO : cookDetailBOList) {
								SpecExtBO specExtBO = cookDetailBO.getSpecExtBO();
								List<SpecBO> specBOList = specExtBO.getSpecBOList();
								if (null == specBOList) {
									specBOList = new ArrayList<>();
								}
								SpecBO specBO = new SpecBO();
								specBO.setSpecId(specAfter.getSpecDetailId());
								double specPrice = cookDetailBO.getPrice() + specAfter.getPriceScale();
								specBO.setSpecPrice(specPrice);
								specBOList.add(specBO);
								specExtBO.setSpecBOList(specBOList);
								cookDetailBO.setSpecExtBO(specExtBO);
								cookDetailInService.updateById(cookDetailBO);

							}
						}
						break;
					case CommonConstant.Notify.MENU_SPEC_DETAIL_UPDATE_TAG:
						if (org.apache.commons.collections.CollectionUtils.isNotEmpty(cookDetailBOList)) {
							if (specAfter.getIsValid() == 0) {
								for (CookDetailBO cookDetailBO : cookDetailBOList) {
									SpecExtBO specExtBO = cookDetailBO.getSpecExtBO();
									List<SpecBO> specBOList = specExtBO.getSpecBOList();
									if (specBOList != null) {
										Iterator<SpecBO> iterator = specBOList.iterator();
										while (iterator.hasNext()) {
											SpecBO specBO = iterator.next();
											if (specAfter.getSpecDetailId().equals(specBO.getSpecId())) {
												iterator.remove();
											}
										}
									}
									specExtBO.setSpecBOList(specBOList);
									cookDetailBO.setSpecExtBO(specExtBO);
									cookDetailInService.updateById(cookDetailBO);
								}
								break;
							} else {
								for (CookDetailBO cookDetailBO : cookDetailBOList) {
									SpecExtBO specExtBO = cookDetailBO.getSpecExtBO();
									List<SpecBO> specBOList = specExtBO.getSpecBOList();
									for (SpecBO specBO : specBOList) {
										if (specAfter.getSpecDetailId().equals(specBO.getSpecId())) {
											double specPrice = cookDetailBO.getPrice() + specAfter.getPriceScale();
											specBO.setSpecPrice(specPrice);
										}
									}
									specExtBO.setSpecBOList(specBOList);
									cookDetailBO.setSpecExtBO(specExtBO);
									cookDetailInService.updateById(cookDetailBO);
								}
							}

						}
				}
			} catch (Exception e) {
				bizLog.error("添加关联规格失败!", e);
			} finally {
				//释放锁
				unLock(lockKey);
			}

		} catch (Exception e) {
			bizLog.error("菜谱明细同步规格价格消息消费失败! data:" + JSON.toJSONString(msg.getContent()), e);
			return Boolean.FALSE;
		}

		return Boolean.TRUE;
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
