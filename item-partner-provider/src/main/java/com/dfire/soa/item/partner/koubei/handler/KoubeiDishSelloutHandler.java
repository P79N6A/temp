package com.dfire.soa.item.partner.koubei.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dfire.open.takeout.bo.kb.KbDishSelloutRequest;
import com.dfire.open.takeout.enumeration.KouBeiDishStatusEnum;
import com.dfire.open.takeout.service.IKouBeiDishCookService;
import com.dfire.rest.util.common.exception.OpenApiException;
import com.dfire.soa.item.bo.Menu;
import com.dfire.soa.item.partner.bo.ItemMapping;
import com.dfire.soa.item.partner.constant.CommonConstant;
import com.dfire.soa.item.partner.service.internal.IItemMappingService;
import com.dfire.soa.item.service.IGetMenuService;
import com.dfire.soa.msstate.bo.MenuBalance;
import com.dfire.soa.msstate.notify.MsstateMessageVo;
import com.dfire.soa.msstate.service.IMenuBalanceClientService;
import com.twodfire.share.result.Result;
import com.twodfire.util.JsonUtil;
import com.twodfire.util.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.codehaus.jackson.type.JavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.dfire.rest.util.common.enumeration.TpTakeoutOpResultEn.error234;

/**
 * Created by GanShu on 2018/6/1 0001.
 */
@Component
public class KoubeiDishSelloutHandler {
    /**
     * 日志：业务
     */
    private static Logger bizLogger = LoggerFactory.getLogger(CommonConstant.BIZ_LOG);

    @Resource
    private IKouBeiDishCookService kouBeiDishCookService;
    @Resource
    private IItemMappingService itemMappingService;
    @Resource
    private IMenuBalanceClientService menuBalanceClientService;
    @Resource
    private IGetMenuService getMenuService;

    public boolean dishSellout(MsstateMessageVo messageVo, String merchantId, String shopId) {
        try {
            String menuIdJson = messageVo.getContent();
            JavaType javaType = JsonUtil.getCollectionType(ArrayList.class, String.class);
            List<String> menuIdList;
            String entityId = messageVo.getEntityId();
            menuIdList = JsonUtil.jsonToBean(menuIdJson, javaType);
            if(CollectionUtils.isEmpty(menuIdList)) {
                bizLogger.info("[koubei item sellout] there is no dish id list. params: {}", JSON.toJSONString(messageVo));
                return true;
            }
//            Thread.sleep(CommonConstant.SLEEP_TIME); // 等待1秒钟，等事务结束后再处理
            Result<List<MenuBalance>> menuBalanceListResult = menuBalanceClientService.getMenuBalanceList(entityId, menuIdList);
            int totalCount = menuIdList.size();
            if(!menuBalanceListResult.isSuccess()) {
                throw new OpenApiException(error234.getCode(), error234.getMessage(), "fail to get sellout dish list. result: {}", JSON.toJSONString(menuBalanceListResult));
            }
            List<MenuBalance> menuBalanceList = menuBalanceListResult.getModel();

            int successCount = 0;
            List<String> failMenuIdList = new ArrayList<>();
            KbDishSelloutRequest kbDishSelloutRequest;
            for(MenuBalance menuBalance : menuBalanceList) {
                kbDishSelloutRequest = prepareKbDishSelloutRequest(menuBalance, shopId);
                bizLogger.info("kbDishSelloutRequest1:{}", JSONObject.toJSON(kbDishSelloutRequest));
                menuIdList.remove(menuBalance.getMenuId());
                if(kbDishSelloutRequest == null) {
                    bizLogger.info("[koubei item sellout] there is no koubei dish. menuId: {}", menuBalance.getMenuId());
                    successCount ++;
                    failMenuIdList.add(menuBalance.getMenuId());
                    continue;
                }
                Result result = kouBeiDishCookService.dishSelloutSync(kbDishSelloutRequest, merchantId, "update", "estimated");
                if(!result.isSuccess()) {
                    failMenuIdList.add(menuBalance.getMenuId());
                    bizLogger.error("[koubei item sellout] fail to sync sellout dish. menuId: {}", menuBalance.getMenuId());
                } else {
                    successCount ++;
                }
            }
            for(String menuId : menuIdList) {
                kbDishSelloutRequest = prepareKbDishSelloutRequest_(entityId, menuId, shopId);
				bizLogger.info("kbDishSelloutRequest2:{}", JSONObject.toJSON(kbDishSelloutRequest));
				if(kbDishSelloutRequest == null) {
                    failMenuIdList.add(menuId);
                    successCount ++;
                    bizLogger.info("[koubei item sellout] there is no koubei dish. menuId: {}", menuId);
                    continue;
                }
                Result result = kouBeiDishCookService.dishSelloutSync(kbDishSelloutRequest, merchantId, "update", "estimated");
                if(!result.isSuccess()) {
                    failMenuIdList.add(menuId);
                    bizLogger.error("[koubei item sellout] fail to sync sellout dish. menuId: {}", menuId);
                } else {
                    successCount ++;
                }
            }
            if(successCount == totalCount) {
                bizLogger.info("[koubei item sellout] dish sellout success. params: {}", JSON.toJSONString(messageVo));
            } else {
                bizLogger.warn("[koubei item sellout] dish sellout fail. totalCount: {}, failMenuIdList: {}, params: {}", totalCount, JSON.toJSONString(failMenuIdList), JSON.toJSONString(messageVo));
                return true;
            }
        } catch (OpenApiException e) {
            bizLogger.error("[koubei item sellout] fail to sellout dish. errorMessage: {}, messageVo: {}", e.getMsg(), JSON.toJSONString(messageVo), e);
            return false;
        } catch (Exception e) {
            bizLogger.error("[koubei item sellout] fail to sellout dish. messageVo: {}", JSON.toJSONString(messageVo), e);
            return false;
        }
        return true;
    }

    public Result syncDishSellout(MenuBalance menuBalance, String merchantId, String shopId) {
        KbDishSelloutRequest request = prepareKbDishSelloutRequest(menuBalance, shopId);
        if(request == null) {
            return null;
        }
        return kouBeiDishCookService.dishSelloutSync(request, merchantId, "update", "estimated");
    }


    private KbDishSelloutRequest prepareKbDishSelloutRequest(MenuBalance menuBalance, String shopId) {
		KbDishSelloutRequest request = new KbDishSelloutRequest();
        Result<Menu> menuResult = getMenuService.findMenu(menuBalance.getEntityId(), menuBalance.getMenuId());
		if (menuResult != null && menuResult.getModel() != null) {
            if (menuResult.getModel().getIsAdditional() == Menu.IS_ADDITIONAL_YES) {
                request = prepareKbDishAdditionSelloutRequest(menuBalance, shopId);
                return request;
            }
        }
		ItemMapping itemMapping = getItemMapping(menuBalance.getEntityId(), menuBalance.getMenuId(), shopId);
		if(itemMapping==null || StringUtils.isBlank(itemMapping.getTpId())) {
			return null;
		}
        request.setShopId(shopId);
        request.setDsId(itemMapping.getTpId());
        request.setDsType("dishid");
        int balanceNum = menuBalance.getBalanceNum() == null ? 0 : menuBalance.getBalanceNum().intValue();
        request.setStatus(KouBeiDishStatusEnum.open);  // open  估清
        request.setInventory(String.valueOf(balanceNum));
        request.setUpdateUser("1");
        return request;
    }

	private KbDishSelloutRequest prepareKbDishAdditionSelloutRequest(MenuBalance menuBalance, String shopId) {
		KbDishSelloutRequest request = new KbDishSelloutRequest();
		ItemMapping itemMapping = getItemMappingForAddition(menuBalance.getEntityId(), menuBalance.getMenuId(), shopId);
		if(itemMapping==null || StringUtils.isBlank(itemMapping.getTpId())) {
			return null;
		}
		request.setShopId(shopId);
		request.setDsId(itemMapping.getTpId());
		request.setDsType("materialid");
		request.setStatus(KouBeiDishStatusEnum.open);  // open  估清
		request.setInventory("0");//加料时估清强制为0
		request.setUpdateUser("1");
		return request;
	}

    private KbDishSelloutRequest prepareKbDishSelloutRequest_(String entityId, String menuId, String shopId) {
		KbDishSelloutRequest request = new KbDishSelloutRequest();
        Result<Menu> menuResult = getMenuService.findMenu(entityId, menuId);
		if (menuResult != null && menuResult.getModel() != null) {
            if (menuResult.getModel().getIsAdditional() == Menu.IS_ADDITIONAL_YES) {
                request = prepareKbDishAdditionSelloutRequest_(entityId, menuId, shopId);
                return request;
            }
        }
		ItemMapping itemMapping = getItemMapping(entityId, menuId, shopId);
		if(itemMapping==null || StringUtils.isBlank(itemMapping.getTpId())) {
			return null;
		}
        request.setShopId(shopId);
        request.setDsId(itemMapping.getTpId());
        request.setDsType("dishid");
        request.setStatus(KouBeiDishStatusEnum.stop);  // stop  取消估清
        request.setInventory("1000");
        request.setUpdateUser("1");
        return request;
    }

	private KbDishSelloutRequest prepareKbDishAdditionSelloutRequest_(String entityId, String menuId, String shopId) {
		KbDishSelloutRequest request = new KbDishSelloutRequest();
		ItemMapping itemMapping = getItemMappingForAddition(entityId, menuId, shopId);
		if(itemMapping==null || StringUtils.isBlank(itemMapping.getTpId())) {
			return null;
		}
		request.setShopId(shopId);
		request.setDsId(itemMapping.getTpId());
		request.setDsType("materialid");
		request.setStatus(KouBeiDishStatusEnum.stop);  // stop  取消估清
		request.setInventory("1000");
		request.setUpdateUser("1");
		return request;
	}


    private ItemMapping getItemMapping(String entityId, String localItemId, String tpShopId) {
        ItemMapping itemMapping = itemMappingService.getTpId(String.valueOf(CommonConstant.KOUBEI_PLATFORM), CommonConstant.ITEM, entityId, localItemId, tpShopId);
        if (itemMapping == null) {
            return null;
        }
        return itemMapping;
    }

	private ItemMapping getItemMappingForAddition(String entityId, String localItemId, String tpShopId) {
		ItemMapping itemMapping = itemMappingService.getTpId(String.valueOf(CommonConstant.KOUBEI_PLATFORM), CommonConstant.ADDITION, entityId, localItemId, tpShopId);
		if (itemMapping == null) {
			return null;
		}
		return itemMapping;
	}

}
