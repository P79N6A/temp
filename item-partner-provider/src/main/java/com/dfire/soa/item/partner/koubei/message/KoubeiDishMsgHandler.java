package com.dfire.soa.item.partner.koubei.message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dfire.rest.util.common.util.OpenRestUtil;
import com.dfire.soa.item.bo.Menu;
import com.dfire.soa.item.partner.bo.CookDetailBO;
import com.dfire.soa.item.partner.bo.query.CookDetailQuery;
import com.dfire.soa.item.partner.constant.CommonConstant;
import com.dfire.soa.item.partner.koubei.KouBeiCheckUtil;
import com.dfire.soa.item.partner.koubei.handler.KoubeiAdditionHandler;
import com.dfire.soa.item.partner.koubei.handler.KoubeiDishHandler;
import com.dfire.soa.item.partner.service.internal.ICookDetailInService;
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
import java.util.List;
import java.util.Objects;

/**
 * 消息处理-菜品
 * Created by heque on 2018/5/9 0009.
 */
@Component
@MessageTag(tag = {CommonConstant.Notify.MENU_DELETE_TAG, CommonConstant.Notify.MENU_INSERT_TAG, CommonConstant.Notify.MENU_UPDATE_TAG})
public class KoubeiDishMsgHandler implements ConsumerCallBack {

    @Resource
    private KoubeiDishHandler koubeiDishHandler;
    @Resource
    private KouBeiCheckUtil kouBeiCheckUtil;
	@Resource
	private ICookDetailInService cookDetailInService;
	@Resource
	private KoubeiAdditionHandler koubeiAdditionHandler;

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

			Menu menuAfter = JSON.parseObject(dataMap.get("data_after"), Menu.class);
			Menu menuBefore = JSON.parseObject(dataMap.get("data_before"), Menu.class);
			if(menuAfter.getType() == 1) {
				return true;
			}

			if (menuAfter.getIsAdditional() == Menu.IS_ADDITIONAL_YES) {
				if (shopBindVo == null || shopBindVo.getBindStatus() != 1 || StringUtils.isBlank(shopBindVo.getMerchantId()) || StringUtils.isBlank(shopBindVo.getShopId())) {
					return true;
				}
			} else {
				if (shopBindVo == null || shopBindVo.getBindStatus() != 1 || StringUtils.isBlank(shopBindVo.getMerchantId()) || StringUtils.isBlank(shopBindVo.getShopId()) ||
						(StringUtils.isNotBlank(shopBindVo.getExtendFields()) && Objects.equals(JSON.parseObject(shopBindVo.getExtendFields(), ShopBindExtendFieldsVo.class).getSyncStatus(), 3))) {
					return true;
				}
			}

            String merchantId = shopBindVo.getMerchantId();
            String shopId = shopBindVo.getShopId();

			bizLog.info("[kb_databack] sync dish menu. tag: {}, entityId: {}, menuAfter:{}, msgId: {}", tag, menuAfter.getEntityId(), JSONObject.toJSON(menuAfter), msg.getMsgID());

			//口碑-菜品tag处理
            switch (tag){
                case CommonConstant.Notify.MENU_DELETE_TAG://菜品-删除 | 加料菜删除
					if (menuAfter.getIsAdditional() == Menu.IS_ADDITIONAL_YES) {
						return koubeiAdditionHandler.delete(merchantId, shopId, menuAfter);
					}else{
						syncDeleteCookDetail(menuAfter);
						return koubeiDishHandler.pushkoubeiDishDelete(shopId, merchantId, menuAfter);
					}
                case CommonConstant.Notify.MENU_INSERT_TAG://菜品-新增 | 加料菜新增
					if (menuAfter.getIsAdditional() == Menu.IS_ADDITIONAL_YES) {
						return koubeiAdditionHandler.insertOrUpdate(merchantId, shopId, menuAfter);
					}else{
						return koubeiDishHandler.pushkoubeiDishAddAndUpdate(merchantId, shopId, menuAfter.getId(),menuAfter.getEntityId(),0);
					}
                case CommonConstant.Notify.MENU_UPDATE_TAG://菜品-修改
                    if (menuAfter != null && menuAfter.getIsValid() == 1) {
                        syncUpdateCookDetail(menuAfter);
                        return koubeiDishHandler.pushkoubeiDishAddAndUpdate(merchantId, shopId, menuAfter.getId(),menuAfter.getEntityId(),0);
                    }else {
						syncDeleteCookDetail(menuAfter);
                        return koubeiDishHandler.pushkoubeiDishDelete(shopId, merchantId, menuBefore);
                    }

            }
        }catch (BizException e) {
            bizLog.warn("[kb_databack]KoubeiDishMsgHandler.process(msg) BizException. tag: " + JSON.toJSONString(tag) + ", dataMap:" + JSON.toJSONString(dataMap), e);
            return true;
        }catch (Exception e) {
            bizLog.error("[kb_databack]KoubeiDishMsgHandler.process(msg) Exception. tag: " + JSON.toJSONString(tag) + ", dataMap:" + JSON.toJSONString(dataMap), e);
            return false;
        }
        return true;
    }

	/**
	 * 同步更新cookdetail
	 *
	 * @param menuAfter
	 */
	private void syncUpdateCookDetail(Menu menuAfter) {
		CookDetailQuery cookDetailQuery = new CookDetailQuery(menuAfter.getEntityId());
		cookDetailQuery.setMenuId(menuAfter.getId());
		List<CookDetailBO> cookDetailBOList = cookDetailInService.selectByQuery(cookDetailQuery);
		if (null != cookDetailBOList) {
			for (CookDetailBO cookDetailBO : cookDetailBOList) {
				if (cookDetailBO.getUsePriceSwitch() == 1) {
					if (cookDetailBO.getPrice() != menuAfter.getPrice() || cookDetailBO.getMemberPrice() != menuAfter.getMemberPrice()) {
						cookDetailBO.setPrice(menuAfter.getPrice());
						cookDetailBO.setMemberPrice(menuAfter.getMemberPrice());
						cookDetailInService.updateById(cookDetailBO);
					}
				}
			}
		}
	}

	/**
	 * 同步删除cookDetail
	 *
	 * @param menuAfter
	 */
	private void syncDeleteCookDetail(Menu menuAfter) {
		//删之前先判断是否菜谱有这个菜
		CookDetailQuery cookDetailQuery = new CookDetailQuery(menuAfter.getEntityId());
		cookDetailQuery.setMenuId(menuAfter.getId());
		List<CookDetailBO> cookDetailBOList = cookDetailInService.selectByQuery(cookDetailQuery);
		if (null != cookDetailBOList) {
			cookDetailInService.deleteByMenuId(menuAfter.getEntityId(), menuAfter.getId());
		}
	}
}
