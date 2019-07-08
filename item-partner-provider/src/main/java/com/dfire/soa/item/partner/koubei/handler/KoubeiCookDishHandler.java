package com.dfire.soa.item.partner.koubei.handler;

import com.alibaba.fastjson.JSON;
import com.dfire.open.takeout.bo.kb.*;
import com.dfire.open.takeout.enumeration.*;
import com.dfire.open.takeout.service.IKouBeiDishCookService;
import com.dfire.rest.util.common.constant.CacheConstants;
import com.dfire.rest.util.common.constant.TimeFormatConstants;
import com.dfire.rest.util.common.enumeration.TpTakeoutOpResultEn;
import com.dfire.rest.util.common.exception.OpenApiException;
import com.dfire.rest.util.common.util.DateUtil;
import com.dfire.rest.util.common.util.OpenRestUtil;
import com.dfire.soa.item.bo.KindMenu;
import com.dfire.soa.item.bo.Menu;
import com.dfire.soa.item.bo.SpecDetail;
import com.dfire.soa.item.partner.bo.*;
import com.dfire.soa.item.partner.bo.query.CookDetailQuery;
import com.dfire.soa.item.partner.constant.CommonConstant;
import com.dfire.soa.item.partner.constants.CommonConstants;
import com.dfire.soa.item.partner.domain.CookDO;
import com.dfire.soa.item.partner.domain.CookDetailDO;
import com.dfire.soa.item.partner.enums.EnumCookType;
import com.dfire.soa.item.partner.koubei.KouBeiCheckUtil;
import com.dfire.soa.item.partner.service.internal.ICookDetailInService;
import com.dfire.soa.item.partner.service.internal.ICookInService;
import com.dfire.soa.item.partner.service.internal.IItemMappingService;
import com.dfire.soa.item.partner.service.internal.IItemMenuMappingService;
import com.dfire.soa.item.partner.util.TransformUtil;
import com.dfire.soa.item.service.IGetMenuService;
import com.dfire.soa.item.service.IGetSpecDetailService;
import com.twodfire.exception.BizException;
import com.twodfire.redis.CodisService;
import com.twodfire.share.result.Result;
import com.twodfire.share.result.ResultSupport;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

import static com.dfire.rest.util.common.enumeration.TpTakeoutOpResultEn.*;
import static com.dfire.soa.item.partner.constant.CommonConstant.ERROR_MESSAGE_1002;


/**
 * Created by GanShu on 2018/5/9 0009.
 */
@Component
public class KoubeiCookDishHandler {

	/**
	 * 日志：业务
	 */
	private static Logger bizLogger = LoggerFactory.getLogger(CommonConstant.BIZ_LOG);

	@Resource
	private IKouBeiDishCookService kouBeiDishCookService;

	@Resource
	private IItemMappingService itemMappingService;

	@Resource
	private IGetMenuService getMenuService;

	@Resource
	private IGetSpecDetailService getSpecDetailService;

	@Resource
	private CodisService codisService;

	@Resource
	private ICookInService cookInService;

	@Resource
	private ICookDetailInService cookDetailInService;

	@Resource
	private IItemMenuMappingService itemMenuMappingService;

    @Resource
    private KouBeiCheckUtil kouBeiCheckUtil;

//	@Resource
//	private CodisService codisService;

	/**
	 * 添加菜谱
	 *
	 * @param params
	 * @param merchantId
	 * @return
	 */
	public boolean addCook(Map<String, String> params, String merchantId, String shopId) {
		bizLogger.debug("[kb_databack][debug] add cook. entityId: {}", params.get("entity_id"));
		String entityId = params.get("entity_id");
		String dataAfterStr = params.get("data_after");
		boolean flag;
		Long localCookId = null;
		try {
			CookDO afterCookDO = JSON.parseObject(dataAfterStr, CookDO.class);
			bizLogger.debug("[kb_databack] dataAfterStr: {}", dataAfterStr);
			localCookId = afterCookDO.getId();
//            String key = OpenRestUtil.generateCacheKey(CacheConstants.KOUBEI_DATA_BACK, localCookId, String.valueOf(KbCommonConstant.KOUBEI_PLATFORM), KouBeiSyncTypeEnum.add.getCode());
//            if(codisService.setnx(key, CacheConstants.EXPIRE_ONE_SECOND, "1") == 0L) {
//                bizLogger.debug("[kb_databack][debug] multiple menu duplicate add. dataAfterStr: {}", dataAfterStr);
//                return true;
//            }
            if (EnumCookType.KOUBEI.getCode() != afterCookDO.getType()) {
                bizLogger.info("[kb_databack] do not deal with it. it is not koubei cook. entityId: {}, merchantId: {}, cookId: {}", entityId, merchantId, localCookId);
                return true;
            }
			CookBO cookBO = getCookBO(entityId, localCookId, shopId);
			if (afterCookDO.getLastVer() < cookBO.getLastVer()) {
				bizLogger.info("[kb_databack][debug] afterCookBO's last-version is less than cookBO's . entityId: {}, merchantId: {}", entityId, merchantId);
				return true;
			}
			ItemMapping itemMapping = getItemMapping(entityId, CommonConstant.ITEM_COOK, String.valueOf(localCookId), true, shopId);
			if (itemMapping != null && StringUtils.isNotEmpty(itemMapping.getTpId())) {
				flag = updateCookBODetail(cookBO, entityId, merchantId, itemMapping.getTpId(), shopId);
				if (!flag) {
					bizLogger.error("[kb_databack] fail to update dish cook. entityId: {}, merchantId: {}, localMultipleMenuId: {}", entityId, merchantId, localCookId);
				}
				return flag;
			}

			//添加itemMapping映射关系
			flag = syncCook(cookBO, merchantId, entityId, shopId);
		} catch (BizException e) {
            bizLogger.warn("[kb_databack] fail to add dish cook. entityId: {}, localMultipleMenuId: {}", entityId, localCookId, e);
		    prepareItemMappingForCook(entityId, String.valueOf(localCookId), shopId, "", CommonConstants.SyncStatus.FAIL, e.getMessage().replaceAll("\\[kb_databack\\]\\[error\\]", ""));
		    return false;
        } catch (Exception e) {
			bizLogger.error("[kb_databack] fail to add dish cook. entityId: {}, localMultipleMenuId: {}", entityId, localCookId, e);
			prepareItemMappingForCook(entityId, String.valueOf(localCookId), shopId, "", CommonConstants.SyncStatus.FAIL, ERROR_MESSAGE_1002);
			return false;
		}
		return flag;
	}


	public boolean syncCook(CookBO cookBO, String merchantId, String entityId, String shopId) {
		ItemMapping itemMapping = getItemMapping(entityId, CommonConstant.ITEM_COOK, String.valueOf(cookBO.getId()), true, shopId);
		if (itemMapping != null && StringUtils.isNotEmpty(itemMapping.getTpId())) {
			bizLogger.info("[kb_databack][debug] dish cook has been already exist. entityId: {}, merchantId: {}, cookId: {}", entityId, merchantId, cookBO.getId());
			return true;
		}

		//  设置防重
		String key = OpenRestUtil.generateCacheKey(CacheConstants.KOUBEI_DATA_BACK, String.valueOf(cookBO.getId()), String.valueOf(CommonConstant.KOUBEI_PLATFORM), KouBeiSyncTypeEnum.add.getCode());
		if (codisService.setnx(key, CacheConstants.EXPIRE_ONE_SECOND, "1") == 0L) {
			bizLogger.info("[kb_databack] multiple menu duplicate add. localCookId: {}", cookBO.getId()); // 主键id 就是 菜谱id
			return true;
		}

		KbDishCookRequest request = new KbDishCookRequest();
		KbDishCook kbDishCook = new KbDishCook();
		request.setKbDishCook(kbDishCook);
		prepareCookBO(cookBO, kbDishCook, merchantId, null, shopId);
		Result<KbDishCookResponse> result = kouBeiDishCookService.dishCookSync(request, KouBeiSyncTypeEnum.add, KouBeiCookBizTypeEnum.cook);
		if (!result.isSuccess()) {
			bizLogger.error("[kb_databack] fail to add dish cook. entityId: {}, resultCode: {}, resultMsg: {}", entityId, result.getResultCode(), result.getMessage());
			prepareItemMappingForCook(entityId, String.valueOf(cookBO.getId()), shopId, null, CommonConstants.SyncStatus.FAIL, result.getMessage());
			return false;
		}
		bizLogger.info("[kb_databack][debug] add dish cook success. entityId: {}, merchantId: {}, result: {}", entityId, merchantId, JSON.toJSONString(result));
		KbDishCookResponse kbDishCookResponse = result.getModel();
		KbDishCook kbDishCook_ = kbDishCookResponse.getKbDishCook();
		String koubeiDishCookId = kbDishCook_.getCookId();
		boolean flag = prepareItemMappingForCook(entityId, String.valueOf(cookBO.getId()), shopId, koubeiDishCookId, CommonConstants.SyncStatus.SUCCESS, "");
		if (!flag) {
			bizLogger.error("[kb_databack] fail to save itemMapping. entityId: {}, localMultipleMenuId: {}", entityId, cookBO.getId());
			return false;
		}
		return true;
	}

	private boolean prepareItemMappingForCook(String entityId, String localId, String tpShopId, String tpId, int syncStatus, String syncResult) {
		ItemMapping itemMapping = itemMappingService.getTpId(String.valueOf(CommonConstant.KOUBEI_PLATFORM), CommonConstant.ITEM_COOK, entityId, localId, tpShopId);
		int count = 0;
		int retryCount = 0;
		do {
		    //  失败时，重试三次
            try {
                retryCount++;
                if (itemMapping != null) {
                    itemMapping.setSyncStatus(syncStatus);
                    itemMapping.setSyncResult(syncResult);
                    count = itemMappingService.updateItemMapping(itemMapping);
                    if (count == 0) {
                        bizLogger.error("[kb_databack] fail to update item mapping. entityId: {}, localId: {}, tpShopId: {}, tpId: {}", entityId, localId, tpShopId, tpId);
                    }
                } else {
                    itemMapping = new ItemMapping();
                    itemMapping.setEntityId(entityId);
                    itemMapping.setLocalId(localId);
                    itemMapping.setPlatCode(String.valueOf(CommonConstant.KOUBEI_PLATFORM));
                    itemMapping.setIdType((int) CommonConstant.ITEM_COOK);
                    itemMapping.setTpShopId(tpShopId);
                    itemMapping.setTpId(tpId);
                    itemMapping.setSyncStatus(syncStatus);
                    itemMapping.setSyncResult(syncResult);
                    count = itemMappingService.saveItemMapping(itemMapping);
                    if (count == 0) {
                        bizLogger.error("[kb_databack] fail to save item mapping. entityId: {}, localId: {}, tpShopId: {}, tpId: {}", entityId, localId, tpShopId, tpId);
                    }
                }
            } catch (Exception e) {
                bizLogger.error("[kb_databack] fail to save item mapping. entityId: {}, localId: {}, tpShopId: {}, tpId: {}", entityId, localId, tpShopId, tpId, e);
            }
        } while (count == 0 && retryCount < 4);
		if(count == 0) {
            bizLogger.error("[kb_databack] fail to save item mapping. entityId: {}, localId: {}, tpShopId: {}, tpId: {}, retryCount: {}", entityId, localId, tpShopId, tpId, retryCount);
            return false;
        }
		return true;
	}


	/**
	 * 更新菜谱
	 *
	 * @param params
	 * @param merchantId
	 * @return
	 */
	public boolean updateCook(Map<String, String> params, String merchantId, String shopId) {
		bizLogger.debug("[kb_databack][debug] update multiple menu. entityId: {}", params.get("entity_id"));
		String entityId = params.get("entity_id");
		String dataBeforeStr = params.get("data_before");
//        MultipleMenu beforeMultipleMenu = JSON.parseObject(dataBeforeStr, MultipleMenu.class);
		String dataAfterStr = params.get("data_after");
		Long localCookId = null;
		boolean flag;
		try {
			CookDO afterCookDO = JSON.parseObject(dataAfterStr, CookDO.class);
			localCookId = afterCookDO.getId();
//            String key = OpenRestUtil.generateCacheKey(CacheConstants.KOUBEI_DATA_BACK, localCookId, String.valueOf(KbCommonConstant.KOUBEI_PLATFORM), KouBeiSyncTypeEnum.update.getCode());
//            if(codisService.setnx(key, CacheConstants.EXPIRE_ONE_SECOND, "1") == 0L) {
//                bizLogger.debug("[kb_databack][debug] multiple menu duplicate update.  dataBeforeStr: {}, dataAfterStr: {}", dataBeforeStr, dataAfterStr);
//                return true;
//            }
			bizLogger.debug("[kb_databack][debug] multiple menu.  dataBeforeStr: {}, dataAfterStr: {}", dataBeforeStr, dataAfterStr);
            if (EnumCookType.KOUBEI.getCode() != afterCookDO.getType()) {
                bizLogger.info("[kb_databack] do not deal with it. it is not koubei cook. entityId: {}, merchantId: {}, cookId: {}", entityId, merchantId, localCookId);
                return true;
            }
			CookBO cookBO = getCookBO(entityId, localCookId, shopId);
			if (afterCookDO.getLastVer() < cookBO.getLastVer()) {
				bizLogger.debug("[kb_databack][debug] afterMultipleMenu's last-version is less than multipleMenu's . entityId: {}, merchantId: {}, localMultipleMenuId: {}", entityId, merchantId, localCookId);
				return true;
			}
			ItemMapping itemMapping = getItemMapping(entityId, CommonConstant.ITEM_COOK, String.valueOf(localCookId), true, shopId);
			if (itemMapping == null || StringUtils.isEmpty(itemMapping.getTpId())) {  //  如果菜谱关系不存在就新增
				flag = syncCook(cookBO, merchantId, entityId, shopId);
				return flag;
			}
			String tpCookId = itemMapping.getTpId();
			flag = updateCookBODetail(cookBO, entityId, merchantId, tpCookId, shopId);
			if (!flag) {
				bizLogger.error("[kb_databack] fail to update dish cook. entityId: {}, merchantId: {}, localCookId: {}", entityId, merchantId, localCookId);
			}
			return flag;
		} catch (BizException e) {
            bizLogger.warn("[kb_databack] fail to update dish cook. entityId: {}, localCookId: {}", entityId, localCookId, e);
            prepareItemMappingForCook(entityId, String.valueOf(localCookId), shopId, "", CommonConstants.SyncStatus.FAIL, e.getMessage().replaceAll("\\[kb_databack\\]\\[error\\]", ""));
            return false;
        } catch (OpenApiException e) {
			bizLogger.warn("[kb_databack] fail to update dish cook. entityId: {}, localCookId: {}", entityId, localCookId, e);
            prepareItemMappingForCook(entityId, String.valueOf(localCookId), shopId, "", CommonConstants.SyncStatus.FAIL, ERROR_MESSAGE_1002);
			return false;
		} catch (Exception e) {
			bizLogger.error("[kb_databack] fail to update dish cook. entityId: {}, merchantId: {}, localMultipleMenuId: {}", entityId, merchantId, localCookId, e);
            prepareItemMappingForCook(entityId, String.valueOf(localCookId), shopId, "", CommonConstants.SyncStatus.FAIL, ERROR_MESSAGE_1002);
			return false;
		}
	}

	private boolean updateCookBODetail(CookBO cookBO, String entityId, String merchantId, String tpCookId, String shopId) {
		KbDishCookRequest request = new KbDishCookRequest();
		KbDishCook kbDishCook = new KbDishCook();
		request.setKbDishCook(kbDishCook);
		prepareCookBO(cookBO, kbDishCook, merchantId, tpCookId, shopId);
		Result result = kouBeiDishCookService.dishCookSync(request, KouBeiSyncTypeEnum.update, KouBeiCookBizTypeEnum.cook);
		if (!result.isSuccess()) {
			bizLogger.error("[kb_databack] fail to update dish cook. entityId: {}, localCookId: {}, resultCode: {}, resultMsg: {}", entityId, cookBO.getId(), result.getResultCode(), result.getMessage());
			prepareItemMappingForCook(entityId, String.valueOf(cookBO.getId()), shopId, tpCookId, CommonConstants.SyncStatus.FAIL, result.getMessage());
			return false;
		}
		bizLogger.info("[kb_databack][debug] update dish cook success. entityId: {}, merchantId: {}, result: {}", entityId, merchantId, JSON.toJSONString(result));
		return true;
	}

	private CookBO getCookBO(String entityId, Long localCookId, String shopId) throws OpenApiException {
		CookBO cookBO = cookInService.queryById(entityId, localCookId);
		if (null == cookBO) {
			throw new OpenApiException(TpTakeoutOpResultEn.error214.getCode(), TpTakeoutOpResultEn.error214.getMessage(), " fail to get cook. entityId: {},cookId: {}", entityId, localCookId);
		}
		return cookBO;
	}

	/**
	 * 删除菜谱
	 *
	 * @param params
	 * @param merchantId
	 * @return
	 */
	public boolean deleteCook(Map<String, String> params, String merchantId, String shopId) {
		bizLogger.debug("[kb_databack][debug] del multiple menu. entityId: {}", params.get("entity_id"));
		String entityId = params.get("entity_id");
		String dataBeforeStr = params.get("data_before");
		String dataAfterStr = params.get("data_after");
		Long localCookId = null;
		Result<KbDishCookResponse> result = null;
		try {
			CookDO afterCookDO = JSON.parseObject(dataAfterStr, CookDO.class);
			localCookId = afterCookDO.getId();
            if (EnumCookType.KOUBEI.getCode() != afterCookDO.getType()) {
                bizLogger.info("[kb_databack] do not deal with it. it is not koubei cook. entityId: {}, merchantId: {}, cookId: {}", entityId, merchantId, localCookId);
                return true;
            }
			String key = OpenRestUtil.generateCacheKey(CacheConstants.KOUBEI_DATA_BACK, String.valueOf(localCookId), String.valueOf(CommonConstant.KOUBEI_PLATFORM), KouBeiSyncTypeEnum.del.getCode());
			if (codisService.setnx(key, CacheConstants.EXPIRE_ONE_SECOND, "1") == 0L) {
				bizLogger.debug("[kb_databack][debug] multiple menu duplicate elimination.  dataBeforeStr: {}, dataAfterStr: {}", dataBeforeStr, dataAfterStr);
				return true;
			}
			bizLogger.debug("[kb_databack][debug] multiple menu.  dataBeforeStr: {}, dataAfterStr: {}", dataBeforeStr, dataAfterStr);
//            MultipleMenu multipleMenu = getMultipleMenu(entityId, localCookId);
//            if(afterMultipleMenu.getLastVer() < multipleMenu.getLastVer()) {
//                bizLogger.info("[kb_databack][info]afterMultipleMenu's last-version is less than multipleMenu's . entityId: {}, merchantId: {}", entityId, merchantId);
//                return true;
//            }
			return deleteMultipleMenuDetail(merchantId, entityId, localCookId, shopId);
		} catch (OpenApiException e) {
			bizLogger.warn("[kb_databack] fail to del dish cook. entityId: {}, localCookId: {}", entityId, localCookId, e);
            prepareItemMappingForCook(entityId, String.valueOf(localCookId), shopId, "", 0, ERROR_MESSAGE_1002);
			return false;
		} catch (Exception e) {
			bizLogger.error("[kb_databack] fail to del dish cook. merchantId: {}, entityId: {}, localCookId: {}", merchantId, entityId, localCookId, e);
			return false;
		}
	}

	public boolean deleteMultipleMenuDetail(String merchantId, String entityId, Long localCookId, String shopId) throws OpenApiException {
		Result<KbDishCookResponse> result;
		ItemMapping itemMapping = getItemMapping(entityId, CommonConstant.ITEM_COOK, String.valueOf(localCookId), true, shopId);
		if (itemMapping == null) {
			bizLogger.info("[kb_databack][info] item mapping is not exist. entityId: {}, merchantId: {}, localCookId: {}", entityId, merchantId, localCookId);
			return true;
		}
		if(StringUtils.isEmpty(itemMapping.getTpId())) {
            bizLogger.info("[kb_databack][info] there is no tpId. entityId: {}, merchantId: {}, localCookId: {}", entityId, merchantId, localCookId);
            return true;
        }
		String tpCookId = itemMapping.getTpId();
		KbDishCookRequest request = new KbDishCookRequest();
		KbDishCook kbDishCook = new KbDishCook();
		request.setKbDishCook(kbDishCook);
		kbDishCook.setCreateUser("1");
		kbDishCook.setUpdateUser("1");
		kbDishCook.setCookId(tpCookId);
		kbDishCook.setMerchantId(merchantId);
		result = kouBeiDishCookService.dishCookSync(request, KouBeiSyncTypeEnum.del, KouBeiCookBizTypeEnum.cook);
		if (!result.isSuccess()) {
			bizLogger.error("[kb_databack] fail to del dish cook. entityId: {}, localCookId: {}, resultCode: {}, resultMsg: {}", entityId, localCookId, result.getResultCode(), result.getMessage());
			return false;
		}
		itemMapping.setIsValid(0);
		updateItemMapping(itemMapping);
		bizLogger.info("[kb_databack][debug] del dish cook success. entityId: {}, merchantId: {}, localCookId: {}, result: {}", entityId, merchantId, localCookId, JSON.toJSONString(result));
		return true;
	}

	/**
	 * @param cookBO
	 * @param kbDishCook
	 * @param merchantId
	 * @param cookId     口碑的菜谱id
	 * @throws OpenApiException
	 */
	private void prepareCookBO(CookBO cookBO, KbDishCook kbDishCook, String merchantId, String cookId, String shopId) {
		kbDishCook.setCookId(cookId);
		kbDishCook.setCookName(cookBO.getName());
		byte status = (byte) cookBO.getStatus();
		kbDishCook.setStatus(status == 0 ? KouBeiDishStatusEnum.stop : KouBeiDishStatusEnum.open);

		kbDishCook.setPeriodType(KouBeiPeriodTypeEnum.forever);

//		prepareRules(kbDishCook);  //   使用forever菜谱

		/**  cook_channel  默认都填  kbb2c
		 Byte type = multipleMenu.getType();
		 if (EnumMultiMenuType.DINE_IN.getCode() == type) {
		 kbDishCook.setCookChannel(KouBeiCookChannelEnum.eatin);
		 } else if(EnumMultiMenuType.TAKEOUT.getCode() == type) {
		 kbDishCook.setCookChannel(KouBeiCookChannelEnum.takeout);
		 } else {
		 kbDishCook.setCookChannel(KouBeiCookChannelEnum.paipai);  //  默认都是扫码
		 }
		 **/

		kbDishCook.setCookChannel(KouBeiCookChannelEnum.kbb2c);  //  默认都是扫码

		kbDishCook.setCookVersion(cookBO.getLastVer() == null ? "1" : String.valueOf(cookBO.getLastVer()));
		kbDishCook.setMerchantId(merchantId);
		kbDishCook.setCreateUser("1");
		kbDishCook.setUpdateUser("1");
//        kbDishCook.getShopList();
		kbDishCook.setSourceFrom(CommonConstant.SOURCE_FROM);

		List<String> shopIdList = new ArrayList<>();
		shopIdList.add(shopId);
		kbDishCook.setShopList(shopIdList);

		/**
		 * 口碑菜谱明细
		 */
		List<KbDishCookDetail> kbCookDetailList = new ArrayList<>();
		kbDishCook.setKbCookDetailList(kbCookDetailList);

	}

	private void prepareRules(KbDishCook kbDishCook) {
		kbDishCook.setStartTime("00:00");
		kbDishCook.setEndTime("23:59");
		StringBuilder areaSeatSB = new StringBuilder();
		areaSeatSB.append("无");
		kbDishCook.setArea(areaSeatSB.toString());
		long time = new Date().getTime();
		time = time - 24 * 60 * 60 * 1000L;
		kbDishCook.setStartDate(DateUtil.stampToTime(time, TimeFormatConstants.YYYY_MM_DD));
		time = 365 * 24 * 60 * 60 * 1000L + time;
		kbDishCook.setEndDate(DateUtil.stampToTime(time, TimeFormatConstants.YYYY_MM_DD));
		kbDishCook.setPeriodType(KouBeiPeriodTypeEnum.week);
		kbDishCook.setPeriodValue("1,2,3,4,5,6,7");
		kbDishCook.setArea(areaSeatSB.toString());
	}

	/**
	 * @param entityId
	 * @param idType
	 * @param localId
	 * @param isLocalCookId
	 * @return
	 * @throws OpenApiException
	 */
	private ItemMapping getItemMapping(String entityId, byte idType, String localId, boolean isLocalCookId, String shopId) {
		ItemMapping itemMapping;
		if (isLocalCookId) {
            itemMapping = itemMappingService.getTpId(String.valueOf(CommonConstant.KOUBEI_PLATFORM), idType, entityId, localId, shopId);
		} else {
            itemMapping = itemMappingService.getLocalId(String.valueOf(CommonConstant.KOUBEI_PLATFORM), idType, entityId, localId, shopId);
		}
		if (null == itemMapping) {
			return null;
		}
		return itemMapping;
	}

	private void updateItemMapping(ItemMapping itemMapping) throws OpenApiException {
		int result = itemMappingService.updateItemMapping(itemMapping);
		if (result == 0) {
			throw new OpenApiException(TpTakeoutOpResultEn.error213.getCode(), TpTakeoutOpResultEn.error213.getMessage(), "fail to update dish object mapping. dishObjectMapping: {}, result: {}", JSON.toJSONString(itemMapping), JSON.toJSONString(result));
		}
	}

	/**
	 * 添加菜谱明细
	 *
	 * @param params
	 * @param merchantId
	 * @return
	 */
	public boolean addCookDetail(Map<String, String> params, String merchantId, String shopId) {
		String msgId = params.get("msgId");
		String entityId = params.get("entity_id");
		boolean flag;
		String localItemId = null;
        Long localCookId = 0L;
		try {
			String dataAfterStr = params.get("data_after");
			CookDetailDO afterCookDetailDO = JSON.parseObject(dataAfterStr, CookDetailDO.class);
			localCookId = afterCookDetailDO.getCookId();
			localItemId = afterCookDetailDO.getMenuId();
//            String key = OpenRestUtil.generateCacheKey(CacheConstants.KOUBEI_DATA_BACK, localCookId, localItemId, String.valueOf(KbCommonConstant.KOUBEI_PLATFORM), KouBeiSyncTypeEnum.add.getCode());
//            if(codisService.setnx(key, CacheConstants.EXPIRE_ONE_SECOND, "1") == 0L) {
//                bizLogger.debug("[kb_databack][debug] multiple menu element duplicate add. dataAfterStr: {}", dataAfterStr);
//                return true;
//            }
            CookBO cookBO = getCookBO(entityId, localCookId, shopId);
            if (EnumCookType.KOUBEI.getCode() != cookBO.getType()) {
                bizLogger.info("[kb_databack] do not deal with it. it is not koubei cook. entityId: {}, merchantId: {}, cookId: {}, localItemId: {}", entityId, merchantId, localCookId, localItemId);
                return true;
            }
			bizLogger.debug("[kb_databack][debug] add multiple menu element. dataAfterStr: {}, msgId: {}", dataAfterStr, msgId);
			CookDetailBO cookDetailBO = getMultipleMenuElement(entityId, localCookId, localItemId);
//            if(afterMultipleMenuElement.getLastVer() < multipleMenuElement.getLastVer()) {
//                bizLogger.debug("[kb_databack][debug] afterMultipleMenuElement's last-version is less than multipleMenuElement's . entityId: {}, merchantId: {}, localItemId: {}", entityId, merchantId, localItemId);
//                return true;
//            }
			flag = addCookDetailBODetail(cookDetailBO, entityId, merchantId, localCookId, shopId);
			if (flag) {
				bizLogger.info("[kb_databack][debug] add multiple menu element success. entityId: {}, merchantId: {}, localItemId: {}", entityId, merchantId, localItemId);
			} else {
				bizLogger.error("[kb_databack] fail to add multiple menu element. entityId: {}, merchantId: {}, localItemId: {}", entityId, merchantId, localItemId);
			}
		} catch (BizException e) {
            bizLogger.warn("[kb_databack] fail to prepare object KbDishCookRequest. entityId: {}, merchantId: {}, localItemId: {}", entityId, merchantId, localItemId, e);
            prepareItemMenuMapping(entityId, shopId, String.valueOf(localCookId), "", localItemId, "", CommonConstants.SyncStatus.FAIL, e.getMessage().replaceAll("\\[kb_databack\\]\\[error\\]", ""));
            return false;
        } catch (OpenApiException e) {
			bizLogger.warn("[kb_databack] fail to prepare object KbDishCookRequest. entityId: {}, merchantId: {}, localItemId: {}", entityId, merchantId, localItemId, e);
            prepareItemMenuMapping(entityId, shopId, String.valueOf(localCookId), "", localItemId, "", CommonConstants.SyncStatus.FAIL, ERROR_MESSAGE_1002);
			return error223.getCode().equals(e.getErrorCode()) || TpTakeoutOpResultEn.error224.getCode().equals(e.getErrorCode());
		} catch (Exception e) {
			bizLogger.error("[kb_databack] fail to add multiple menu element. entityId: {}, merchantId: {}, localItemId: {}", entityId, merchantId, localItemId, e);
            prepareItemMenuMapping(entityId, shopId, String.valueOf(localCookId), "", localItemId, "", CommonConstants.SyncStatus.FAIL, ERROR_MESSAGE_1002);
			return false;
		}
		return flag;
	}

	public boolean addCookDetailBODetail(CookDetailBO cookDetailBO, String entityId, String merchantId, Long localCookId, String shopId) throws OpenApiException {
        String localMenuId = cookDetailBO.getMenuId();
        //  防重处理
        String key = OpenRestUtil.generateCacheKey(CacheConstants.KOUBEI_DATA_BACK, String.valueOf(localCookId), localMenuId, String.valueOf(CommonConstant.KOUBEI_PLATFORM), KouBeiSyncTypeEnum.add.getCode());
        if(codisService.setnx(key, CacheConstants.EXPIRE_ONE_SECOND, "1") == 0L) {
            bizLogger.info("[kb_databack] multiple cook detail duplicate add. localMenuId: {}", localMenuId);
            return false;
        }
		//先查菜谱映射关系
		ItemMapping itemMapping = getItemMapping(entityId, CommonConstant.ITEM_COOK, String.valueOf(localCookId), true, shopId);
		if (itemMapping == null || StringUtils.isEmpty(itemMapping.getTpId())) {  //  如果菜谱不存在，先新增菜谱
			CookBO cookBO = getCookBO(entityId, localCookId, shopId);
			boolean flag = syncCook(cookBO, merchantId, entityId, shopId);
			if(!flag) {
				bizLogger.error("[kb_databack] fail to sync koubei cook. entityId: {}, merchantId: {}, localCookId: {}", entityId, merchantId, localCookId);
				return false;
			}
			itemMapping = getItemMapping(entityId, CommonConstant.ITEM_COOK, String.valueOf(localCookId), true, shopId);
		}
		String localItemId = cookDetailBO.getMenuId();
		String kbCookId = itemMapping.getTpId();
		KbDishCookRequest request = prepareMultipleMenuElement(cookDetailBO, merchantId, kbCookId, shopId, false);
		String kbDishId = request.getKbDishCook().getKbCookDetailList().get(0).getDishId();
		Result<KbDishCookResponse> kbDishCookResponseResult = kouBeiDishCookService.queryDishCookByCookIdAndDishId(merchantId, kbCookId, kbDishId);
		if (!kbDishCookResponseResult.isSuccess()) {
			throw new OpenApiException(error227.getCode(), error223.getMessage(), "fail to query dish in cook. kbDishCookResponseResult: {}", JSON.toJSONString(kbDishCookResponseResult));
		}
		Result<KbDishCookResponse> result = new ResultSupport<>();
		if (kbDishCookResponseResult.getModel() == null) {
			result = kouBeiDishCookService.dishCookSync(request, KouBeiSyncTypeEnum.add, KouBeiCookBizTypeEnum.detail);
		} else {
			KbDishCookResponse kbDishCookResponse = kbDishCookResponseResult.getModel();
			boolean delFlag = delDishCookRelDetail(kbDishCookResponse, entityId, kbDishId, cookDetailBO.getMenuId());
//                result = kouBeiDishCookService.dishCookSync(request, KouBeiSyncTypeEnum.del, KouBeiCookBizTypeEnum.detail);  //
			if (delFlag) {
				bizLogger.info("[kb_databack][info] del multiple menu element success. entityId: {}, merchantId: {}, localItemId: {}, resultCode: {}, resultMsg: {}", entityId, merchantId, localItemId, result.getResultCode(), result.getMessage());
				result = kouBeiDishCookService.dishCookSync(request, KouBeiSyncTypeEnum.add, KouBeiCookBizTypeEnum.detail);
			} else {
				bizLogger.error("[kb_databack] fail to del multiple menu element. entityId: {}, merchantId: {}, localItemId: {}, resultCode: {}, resultMsg: {}", entityId, merchantId, localItemId, result.getResultCode(), result.getMessage());
				return false;
			}
		}
		if (!result.isSuccess()) {
			bizLogger.error("[kb_databack] fail to add multiple menu element. entityId: {}, merchantId: {}, localItemId: {}, resultCode: {}, resultMsg: {}", entityId, merchantId, localItemId, result.getResultCode(), result.getMessage());
            prepareItemMenuMapping(entityId, shopId, String.valueOf(localCookId),kbCookId, localItemId, kbDishId, CommonConstants.SyncStatus.FAIL, result.getMessage());
			return false;
		}
		KbDishCookResponse response = result.getModel();
		bizLogger.info("[kb_databack][info] add multiple menu element success. entityId: {}, merchantId: {}, localItemId: {}", entityId, merchantId, localItemId);
//        prepareCookIdList(entityId, localCookId, localItemId, false);
        boolean flag = prepareItemMenuMapping(entityId, shopId, String.valueOf(localCookId), kbCookId, localItemId, kbDishId, CommonConstants.SyncStatus.SUCCESS, "");
        if(!flag) {
            bizLogger.error("[kb_databack] fail to save item menu mapping. entityId: {}, merchantId: {}, localItemId: {}, localCookId: {}", entityId, merchantId, localItemId, localCookId);
        }
		return true;
	}

	private CookDetailBO getMultipleMenuElement(String entityId, Long localCookId, String localItemId) throws OpenApiException {
		CookDetailQuery cookDetailQuery = new CookDetailQuery(entityId);
		cookDetailQuery.setCookId(localCookId);
		cookDetailQuery.setMenuId(localItemId);
		List<CookDetailBO> cookDetailBOList = cookDetailInService.selectByQuery(cookDetailQuery);
		if (CollectionUtils.isEmpty(cookDetailBOList)) {
			throw new OpenApiException(TpTakeoutOpResultEn.error215.getCode(), TpTakeoutOpResultEn.error215.getMessage(), " fail to get multiple menu element. result: {}", JSON.toJSONString(cookDetailBOList));
		}
		CookDetailBO cookDetailBO = cookDetailBOList.get(0);
		return cookDetailBO;
	}


	public boolean updateCookDetail(Map<String, String> params, String merchantId, String shopId) {
		String msgId = params.get("msgId");
		String entityId = params.get("entity_id");
		KbDishCookRequest request;
		Result<KbDishCookResponse> result = null;
		boolean flag;
		Long localCookId = null;
		String localItemId = null;
		try {
			String dataBeforeStr = params.get("data_before");
			String dataAfterStr = params.get("data_after");
			CookDetailDO afterCookDetailDO = JSON.parseObject(dataAfterStr, CookDetailDO.class);
			localCookId = afterCookDetailDO.getCookId();
			localItemId = afterCookDetailDO.getMenuId();
//            String key = OpenRestUtil.generateCacheKey(CacheConstants.KOUBEI_DATA_BACK, localCookId, localItemId, String.valueOf(KbCommonConstant.KOUBEI_PLATFORM), KouBeiSyncTypeEnum.update.getCode());
//            if(codisService.setnx(key, CacheConstants.EXPIRE_ONE_SECOND, "1") == 0L) {
//                bizLogger.debug("[kb_databack][debug] multiple menu element duplicate update. dataAfterStr: {}", dataAfterStr);
//                return true;
//            }

            CookBO cookBO = getCookBO(entityId, localCookId, shopId);
            if (EnumCookType.KOUBEI.getCode() != cookBO.getType()) {
                bizLogger.info("[kb_databack] do not deal with it. it is not koubei cook. entityId: {}, merchantId: {}, cookId: {}, localItemId: {}", entityId, merchantId, localCookId, localItemId);
                return true;
            }

			bizLogger.debug("[kb_databack][debug] update multiple menu element.  dataBeforeStr: {}, dataAfterStr: {}, msgId: {}", dataBeforeStr, dataAfterStr, msgId);
			CookDetailBO cookDetailBO = getMultipleMenuElement(entityId, localCookId, localItemId);
			if (afterCookDetailDO.getLastVer() < cookDetailBO.getLastVer()) {
				bizLogger.info("[kb_databack][info] afterMultipleMenuElement's last-version is less than multipleMenuElement's. entityId: {}, merchantId: {}, localItemId: {}", entityId, merchantId, localItemId);
				return true;
			}
            flag = addCookDetailBODetail(cookDetailBO, entityId, merchantId, localCookId, shopId);
		} catch(BizException e) {
            bizLogger.warn("[kb_databack] fail to update multiple menu element. entityId: {}, merchantId: {}, localCookId: {}, localItemId: {}, result: {}", entityId, merchantId, localCookId, localItemId, JSON.toJSONString(result), e);
            prepareItemMenuMapping(entityId, shopId, String.valueOf(localCookId), "", localItemId, "", CommonConstants.SyncStatus.FAIL, e.getMessage().replaceAll("\\[kb_databack\\]\\[error\\]", ""));
            return false;
        } catch (OpenApiException e) {
			bizLogger.warn("[kb_databack] fail to prepare object KbDishCookRequest. entityId: {}, merchantId: {}, localCookId: {}, localItemId: {}", entityId, merchantId, localCookId, localItemId, e);
            prepareItemMenuMapping(entityId, shopId, String.valueOf(localCookId), "", localItemId, "", CommonConstants.SyncStatus.FAIL, ERROR_MESSAGE_1002);
			return error223.getCode().equals(e.getErrorCode()) || TpTakeoutOpResultEn.error224.getCode().equals(e.getErrorCode());
		} catch (Exception e) {
			bizLogger.error("[kb_databack] fail to update multiple menu element. entityId: {}, merchantId: {}, localCookId: {}, localItemId: {}, result: {}", entityId, merchantId, localCookId, localItemId, JSON.toJSONString(result), e);
            prepareItemMenuMapping(entityId, shopId, String.valueOf(localCookId), "", localItemId, "", CommonConstants.SyncStatus.FAIL, ERROR_MESSAGE_1002);
			return false;
		}
		return flag;
	}


	private boolean prepareItemMenuMapping(String entityId, String tpShopId, String localCookId, String tpCookId, String localItemId, String tpItemId, int syncStatus, String syncResult) {
        ItemMenuMapping itemMenuMapping = itemMenuMappingService.getItemMenuMappingByLocalId(entityId, tpShopId, String.valueOf(CommonConstant.KOUBEI_PLATFORM), localCookId, localItemId);
        int count = 0;
        int retryCount = 0;
        do {
            try {
                retryCount++;
                if (itemMenuMapping != null) {
                    itemMenuMapping.setSyncStatus(syncStatus);
                    itemMenuMapping.setSyncResult(syncResult);
                    itemMenuMapping.setTpItemId(tpItemId);
                    itemMenuMapping.setTpMenuId(tpCookId);
                    count = itemMenuMappingService.updateItemMenuMapping(itemMenuMapping);
                    if (count == 0) {
                        bizLogger.error("[kb_databack] fail to update item menu mapping. entityId: {}, localCookId: {}, localItemId: {}, tpShopId: {}, tpId: {}", entityId, localCookId, localItemId, tpShopId, tpCookId);
                    }
                } else {
                    itemMenuMapping = new ItemMenuMapping();
                    itemMenuMapping.setEntityId(entityId);
                    itemMenuMapping.setTpShopId(tpShopId);
                    itemMenuMapping.setPlatCode(String.valueOf(CommonConstant.KOUBEI_PLATFORM));
                    itemMenuMapping.setLocalMenuId(localCookId);
                    itemMenuMapping.setTpMenuId(tpCookId);
                    itemMenuMapping.setLocalItemId(localItemId);
                    itemMenuMapping.setTpItemId(tpItemId);
                    itemMenuMapping.setSyncStatus(syncStatus);
                    itemMenuMapping.setSyncResult(syncResult);
                    itemMenuMapping.setIsValid(CommonConstants.IsValid.VALID);
                    count = itemMenuMappingService.saveItemMenuMapping(itemMenuMapping);
                    if (count == 0) {
                        bizLogger.error("[kb_databack] fail to save item menu mapping. entityId: {}, localCookId: {}, localItemId: {}, tpShopId: {}, tpId: {}", entityId, localCookId, localItemId, tpShopId, tpCookId);
                    }
                }
            } catch (Exception e) {
                bizLogger.error("[kb_databack] fail to save item menu mapping. entityId: {}, localCookId: {}, localItemId: {}, tpShopId: {}, tpId: {}", entityId, localCookId, localItemId, tpShopId, tpCookId, e);
            }
        } while (count == 0 && retryCount < 4);
        if(count == 0) {
            bizLogger.error("[kb_databack] fail to save item menu mapping. entityId: {}, localCookId: {}, localItemId: {}, tpShopId: {}, tpId: {}, retryCount: {}", entityId, localCookId, localItemId, tpShopId, tpCookId, retryCount);
            return false;
        }
        return true;
    }


	public boolean deleteCookDetail(Map<String, String> params, String merchantId, String shopId) {
		String msgId = params.get("msgId");
		String entityId = params.get("entity_id");
		KbDishCookRequest request;
		Result<KbDishCookResponse> result = null;
		String localItemId = null;
        Long localCookId = 0L;
        try {
			String dataBeforeStr = params.get("data_before");
			String dataAfterStr = params.get("data_after");
			CookDetailDO beforeCookDetailDO = JSON.parseObject(dataBeforeStr, CookDetailDO.class);
			CookDetailDO afterCookDetailDO = JSON.parseObject(dataAfterStr, CookDetailDO.class);
			localCookId = afterCookDetailDO.getCookId();
			localItemId = afterCookDetailDO.getMenuId();
//            String key = OpenRestUtil.generateCacheKey(CacheConstants.KOUBEI_DATA_BACK, localCookId, localItemId, String.valueOf(KbCommonConstant.KOUBEI_PLATFORM), KouBeiSyncTypeEnum.del.getCode());
//            if(codisService.setnx(key, CacheConstants.EXPIRE_ONE_SECOND, "1") == 0L) {
//                bizLogger.debug("[kb_databack][debug] multiple menu element duplicate del. dataAfterStr: {}", dataAfterStr);
//                return true;
//            }
            CookBO cookBO = getCookBO(entityId, localCookId, shopId);
            if (EnumCookType.KOUBEI.getCode() != cookBO.getType()) {
                bizLogger.info("[kb_databack] do not deal with it. it is not koubei cook. entityId: {}, merchantId: {}, cookId: {}, localItemId: {}", entityId, merchantId, localCookId, localItemId);
                return true;
            }

			bizLogger.debug("[kb_databack][debug] del multiple menu element. dataBeforeStr: {}, dataAfterStr: {}", dataBeforeStr, dataAfterStr, msgId);
//            MultipleMenuElement multipleMenuElement = getMultipleMenuElement(entityId, localCookId, localItemId);
//            if(afterMultipleMenuElement.getLastVer() < multipleMenuElement.getLastVer()) {
//                bizLogger.info("afterMultipleMenuElement's last-version is less than multipleMenuElement's. entityId: {}, merchantId: {}, localItemId: {}", entityId, merchantId, localItemId);
//                return true;
//            }
			ItemMapping itemMapping = getItemMapping(entityId, CommonConstant.ITEM_COOK, String.valueOf(localCookId), true, shopId);
			if (itemMapping == null) {
				bizLogger.error("[kb_databack] item mapping is not exist. entityId: {}, merchantId: {}", entityId, merchantId);
				return true;
			}
			if(StringUtils.isEmpty(itemMapping.getTpId())) {
                bizLogger.error("[kb_databack] there is no tpId. entityId: {}, merchantId: {}", entityId, merchantId);
                return true;
            }
            CookDetailBO afterCookDetailBO =  TransformUtil.toCookDetailBO(afterCookDetailDO);
			request = prepareMultipleMenuElement(afterCookDetailBO, merchantId, itemMapping.getTpId(), shopId, true);
			result = kouBeiDishCookService.dishCookSync(request, KouBeiSyncTypeEnum.del, KouBeiCookBizTypeEnum.detail);
			if (!result.isSuccess()) {
				bizLogger.error("[kb_databack] fail to del multiple menu element. entityId: {}, merchantId: {}, localItemId: {}, resultCode: {}, resultMsg: {}", entityId, merchantId, localItemId, result.getResultCode(), result.getMessage());
				return false;
			}
			bizLogger.info("[kb_databack][info] del multiple menu element success. entityId: {}, merchantId: {}, localItemId: {}", entityId, merchantId, localItemId);
//            prepareCookIdList(entityId, localCookId, localItemId, true);
			//删除菜谱明细映射关系
			if (result.isSuccess()) {
				ItemMenuMapping itemMenuMapping = itemMenuMappingService.getItemMenuMappingByLocalId(entityId, shopId, String.valueOf(CommonConstant.KOUBEI_PLATFORM), String.valueOf(afterCookDetailDO.getCookId()), afterCookDetailDO.getMenuId());
				if(itemMenuMapping == null) {
                    bizLogger.error("[kb_databack] there is no item mapping. entityId: {}, merchantId: {}, localItemId: {}", entityId, merchantId, localItemId);
                    return true;
                }
				itemMenuMappingService.deleteItemMenuMappingById(entityId, itemMenuMapping.getId());
			}
		} catch(BizException e) {
            bizLogger.warn("[kb_databack] fail to del multiple menu element. entityId: {}, merchantId: {}, localItemId: {}", entityId, merchantId, localItemId, e);
            prepareItemMenuMapping(entityId, shopId, String.valueOf(localCookId), "", localItemId, "", CommonConstants.SyncStatus.FAIL, e.getMessage().replaceAll("\\[kb_databack\\]\\[error\\]", ""));
            return false;
        } catch (OpenApiException e) {
			bizLogger.warn("[kb_databack] fail to prepare object KbDishCookRequest. entityId: {}, merchantId: {}, localItemId: {}", entityId, merchantId, localItemId, e);
            prepareItemMenuMapping(entityId, shopId, String.valueOf(localCookId), "", localItemId, "", CommonConstants.SyncStatus.FAIL, ERROR_MESSAGE_1002);
			return false;
		} catch (Exception e) {
			bizLogger.error("[kb_databack] fail to del multiple menu element. entityId: {}, merchantId: {}, localItemId: {}", entityId, merchantId, localItemId, e);
            prepareItemMenuMapping(entityId, shopId, String.valueOf(localCookId), "", localItemId, "", CommonConstants.SyncStatus.FAIL, ERROR_MESSAGE_1002);
			return false;
		}
		return true;
	}


	/**
	 * 产出单个sku
	 *
	 * @param entityId
	 * @param merchantId
	 * @param shopId
	 * @param menuId
	 * @param localSpecId
	 * @return
	 * @throws OpenApiException
	 */
	public boolean deleteMultipleMenuElementSku(String entityId, String merchantId, String shopId, String menuId, String localSpecId) throws OpenApiException {
		bizLogger.debug("[kb_databack][debug] del cook detail start. entityId: {}, merchantId: {}, localItemId: {}", entityId, merchantId, menuId);

        String key = OpenRestUtil.generateCacheKey(CacheConstants.KOUBEI_DATA_BACK, menuId, localSpecId, String.valueOf(CommonConstant.KOUBEI_PLATFORM), KouBeiSyncTypeEnum.del.getCode());
        if(codisService.setnx(key, CacheConstants.EXPIRE_ONE_SECOND, "1") == 0L) {
            bizLogger.info("[kb_databack] cook detail duplicate update. menuId: {}, localSpecId: {}", menuId, localSpecId);
            return true;
        }

		//查詢菜譜菜品
		CookDetailQuery cookDetailQuery = new CookDetailQuery(entityId);
		cookDetailQuery.setMenuId(menuId);
		List<CookDetailBO> cookDetailBOList = cookDetailInService.selectByQuery(cookDetailQuery);
		if (CollectionUtils.isEmpty(cookDetailBOList)) {
			bizLogger.info("[kb_databack][info] there is no cook details. entityId: {}, merchantId: {}, menuId: {}, localSpecId: {}", entityId, merchantId, menuId, localSpecId);
			return true;
		}
		int successCount = 0;
		int count = cookDetailBOList.size();
		for (CookDetailBO cookDetailBO : cookDetailBOList) {
			Long localCookId = cookDetailBO.getCookId();
			ItemMapping itemMapping = getItemMapping(entityId, CommonConstant.ITEM_COOK, String.valueOf(localCookId), true, shopId);
			if (itemMapping == null) {
				bizLogger.error("[kb_databack] item mapping is not exist. entityId: {}, merchantId: {}, localCookId: {}", entityId, merchantId, localCookId);
				continue;
			}
            if(StringUtils.isEmpty(itemMapping.getTpId())) {
                bizLogger.info("[kb_databack][info] there is no tpId. entityId: {}, merchantId: {}, localCookId: {}", entityId, merchantId, localCookId);
                continue;
            }
			String kbCookId = itemMapping.getTpId();
			KbDishCookRequest request = prepareMultipleMenuElementSku(merchantId, shopId, cookDetailBO, kbCookId, localSpecId);
			Result<KbDishCookResponse> result = kouBeiDishCookService.dishCookSync(request, KouBeiSyncTypeEnum.del, KouBeiCookBizTypeEnum.detail);
			if (!result.isSuccess()) {
				bizLogger.error("[kb_databack] fail to del cook detail. entityId: {}, merchantId: {}, localItemId: {}, resultCode: {}, resultMsg: {}", entityId, merchantId, menuId, result.getResultCode(), result.getMessage());
				continue;
			}
			successCount++;
		}
		bizLogger.info("[kb_databack][info] del cook detail success. entityId: {}, merchantId: {}, localItemId: {}, successCount: {}, count: {}", entityId, merchantId, menuId, successCount, count);
		return true;
	}

	private KbDishCookRequest prepareMultipleMenuElementSku(String merchantId, String shopId, CookDetailBO cookDetailBO, String kbCookId, String localSpecId) throws OpenApiException {

		KbDishCookRequest request = new KbDishCookRequest();
		KbDishCook kbDishCook = new KbDishCook();
		request.setKbDishCook(kbDishCook);

		String entityId = cookDetailBO.getEntityId();
		Long cookId = cookDetailBO.getCookId();
		prepareKoubeiCook(kbDishCook, merchantId, entityId, cookId, kbCookId, shopId);

		kbDishCook.setMerchantId(merchantId);

		kbDishCook.setSourceFrom(CommonConstant.SOURCE_FROM);
		String localItemId = cookDetailBO.getMenuId();

		ItemMapping itemMapping = getItemMapping(entityId, CommonConstant.ITEM, localItemId, true, shopId);
		String tpItemId;
		if (itemMapping == null) {
			throw new OpenApiException(TpTakeoutOpResultEn.error212.getCode(), TpTakeoutOpResultEn.error212.getMessage(), "there is no item relation vo.");
		} else {
            tpItemId = kouBeiCheckUtil.checkDishId(merchantId, shopId, entityId, localItemId, false, null, null);
			if (StringUtils.isEmpty(tpItemId)) {
				throw new OpenApiException(error225.getCode(), error225.getMessage(), "fail to get tp item id.");
			}
			tpItemId = itemMapping.getTpId();
		}

        ItemMapping itemSkuMapping = itemMappingService.getTpId(String.valueOf(CommonConstant.KOUBEI_PLATFORM), CommonConstant.ITEM_SKU, entityId, localItemId, localSpecId, shopId);
        if (itemSkuMapping == null) {
            throw new OpenApiException(error232.getCode(), error232.getMessage(), "there is no spec mapping record. localItemId: {}, localSpecId: {}", localItemId, localSpecId);
        }
        if(itemMapping.getSyncStatus() == CommonConstants.SyncStatus.FAIL) {
            throw new OpenApiException(error232.getCode(), error232.getMessage(), "the sync result is fail. localItemId: {}, localSpecId: {}", localItemId, localSpecId);
        }
        String tpSkuId = itemSkuMapping.getTpId();
        Result<KbDishCookResponse> kbDishCookResponseResult = kouBeiDishCookService.queryDishCookByCookIdAndDishIdAndSkuId(merchantId, String.valueOf(cookId), tpItemId, tpSkuId);
        if(!kbDishCookResponseResult.isSuccess()) {
            throw new OpenApiException(error247.getCode(), error247.getMessage(), "fail to query dish cook sku. localItemId: {}, localSpecId: {}, result: {}", localItemId, localSpecId, JSON.toJSONString(kbDishCookResponseResult));
        }
        bizLogger.info("[kb_databack] cook dish sku. entityId: {}, shopId: {}, itemId: {}, dishId: {}, result: {}", entityId, shopId, localItemId, tpItemId, JSON.toJSONString(kbDishCookResponseResult));
        KbDishCookResponse kbDishCookResponse = kbDishCookResponseResult.getModel();
        if(kbDishCookResponse == null) {
            throw new OpenApiException(error247.getCode(), error247.getMessage(), "there is no dish cook sku. localItemId: {}, localSpecId: {}", localItemId, localSpecId);
        }
        kbDishCook = kbDishCookResponse.getKbDishCook();
        request.setKbDishCook(kbDishCook);
        return request;
	}




	private boolean delDishCookRelDetail(KbDishCookResponse kbDishCookResponse, String entityId, String kbDishId, String menuId) {
		KbDishCookRequest kbDishCookRequest = new KbDishCookRequest();
		KbDishCook kbDishCook = kbDishCookResponse.getKbDishCook();
		String merchantId = kbDishCook.getMerchantId();
		List<KbDishCookDetail> kbDishCookDetailList = kbDishCook.getKbCookDetailList();
		if (CollectionUtils.isEmpty(kbDishCookDetailList)) {
			bizLogger.error("[kb_databack] kbDishCookDetailList is empty. merchantId: {}, entityId: {}, kbDishId: {}, menuId: {}", merchantId, entityId, kbDishId, menuId);
			return true;
		}
        kbDishCookDetailList.removeIf(kbDishCookDetail -> !kbDishId.equals(kbDishCookDetail.getDishId()));
		kbDishCookRequest.setKbDishCook(kbDishCook);
		bizLogger.info("[kb_databack][info] kbDishCook. merchantId: {}, entityId: {}, kbDishId: {}, kbDishCook: {}, menuId: {}", merchantId, entityId, kbDishId, JSON.toJSONString(kbDishCook), menuId);
		Result<KbDishCookResponse> kbDishCookResponseResult = kouBeiDishCookService.dishCookSync(kbDishCookRequest, KouBeiSyncTypeEnum.del, KouBeiCookBizTypeEnum.detail);
		if (!kbDishCookResponseResult.isSuccess()) {
			bizLogger.error("[kb_databack] fail to del dish in cook. merchantId: {}, entityId: {}, kbDishId: {}, menuId: {}, kbDishCookResponseResult: {}", merchantId, entityId, kbDishId, menuId, JSON.toJSONString(kbDishCookResponseResult));
			return false;
		} else {
			bizLogger.info("[kb_databack][info] del dish in cook successful. merchantId: {}, entityId: {}, kbDishId: {}, menuId: {}", merchantId, entityId, kbDishId, menuId);
		}
		return true;
	}


	public boolean delDishCookRel(String shopId, String merchantId, String entityId, String menuId) {
		ItemMapping itemMapping = getItemMapping(entityId, CommonConstant.ITEM, menuId, true, shopId);
		if (itemMapping == null) {
			bizLogger.error("[kb_databack] there is no item relation vo. merchantId: {}, entityId: {}, menuId: {}", merchantId, entityId, menuId);
			return false;
		}
		String kbDishId = itemMapping.getTpId();
		Result<List<KbDishCookResponse>> kbDishCookResponseListResult = kouBeiDishCookService.queryCookByDishId(merchantId, shopId, kbDishId);
		if (!kbDishCookResponseListResult.isSuccess()) {
			bizLogger.error("[kb_databack] fail to query dish cook. merchantId: {}, entityId: {}, kbDishId: {}, menuId: {}, kbDishCookResponseListResult: {}", merchantId, entityId, kbDishId, menuId, JSON.toJSONString(kbDishCookResponseListResult));
			return false;
		}
		List<KbDishCookResponse> kbDishCookResponseList = kbDishCookResponseListResult.getModel();

		for (KbDishCookResponse kbDishCookResponse : kbDishCookResponseList) {
			delDishCookRelDetail(kbDishCookResponse, entityId, kbDishId, menuId);
		}
		return true;
	}


	/**
	 * 预处理菜谱明细对象
	 *
	 * @param cookDetailBO
	 * @param merchantId
	 * @param kbCookId     口碑的菜谱id
	 * @return
	 * @throws OpenApiException
	 */
	private KbDishCookRequest prepareMultipleMenuElement(CookDetailBO cookDetailBO, String merchantId, String kbCookId, String shopId, boolean isForDel) throws OpenApiException {
		KbDishCookRequest request = new KbDishCookRequest();
		KbDishCook kbDishCook = new KbDishCook();
		request.setKbDishCook(kbDishCook);

		String entityId = cookDetailBO.getEntityId();
		Long cookId = cookDetailBO.getCookId();
		prepareKoubeiCook(kbDishCook, merchantId, entityId, cookId, kbCookId, shopId);

//        kbDishCook.setRemarks();

		kbDishCook.setMerchantId(merchantId);

//        kbDishCook.getShopList();
		kbDishCook.setSourceFrom(CommonConstant.SOURCE_FROM);
		String localItemId = cookDetailBO.getMenuId();
		Menu menu = getMenu(entityId, localItemId);

		ItemMapping itemMapping = getItemMapping(entityId, CommonConstant.ITEM, localItemId, true, shopId);
		String tpItemId = "";
		if (itemMapping == null) {
			if (isForDel) {
				throw new OpenApiException(TpTakeoutOpResultEn.error212.getCode(), TpTakeoutOpResultEn.error212.getMessage(), "there is no item relation vo.");
			}
            tpItemId = kouBeiCheckUtil.checkDishId(merchantId, shopId, entityId, localItemId, false, null, null);
			if (StringUtils.isEmpty(tpItemId)) {
				throw new OpenApiException(error225.getCode(), error225.getMessage(), "fail to get tp item id.");
			}
		} else {
			tpItemId = itemMapping.getTpId();
		}

		List<KbDishCookDetail> kbCookDetailList = kbDishCook.getKbCookDetailList();
		KbDishCookDetail kbDishCookDetail = new KbDishCookDetail();
		kbCookDetailList.add(kbDishCookDetail);

		kbDishCookDetail.setCookId(kbCookId);
		kbDishCookDetail.setDishId(tpItemId);
		kbDishCookDetail.setCategorySmallId(null);
		kbDishCookDetail.setCategoryBigId(kouBeiCheckUtil.checkKindMenuId(CommonConstant.ITEM_COOK_CATEGORY, merchantId, shopId, entityId, menu.getKindMenuId(), false, null));

//        String cookCategoryId = kouBeiCheckUtil.checkCookCategoryId(merchantId, entityId);
//        kbDishCookDetail.setCategoryBigId(cookCategoryId);

		if (menu.getIsSelf() == 1) {
			kbDishCookDetail.setStatus(KouBeiDishStatusEnum.open); //
		} else {
			kbDishCookDetail.setStatus(KouBeiDishStatusEnum.stop); //
		}

		kbDishCookDetail.setSort(String.valueOf(menu.getSortCode()));
//        kbDishCookDetail.setFlag();
		List<KbDishCookPrice> kbCookSkuPriceList = new ArrayList<>();
		prepareKoubeiSku(kbCookSkuPriceList, cookDetailBO, entityId, localItemId, kbCookId, tpItemId, shopId);
		kbDishCookDetail.setKbCookSkuPriceList(kbCookSkuPriceList);

		return request;
	}


	private void prepareKoubeiSku(List<KbDishCookPrice> kbCookSkuPriceList, CookDetailBO cookDetailBO, String entityId, String localItemId, String tpCookId, String tpItemId, String shopId) throws OpenApiException {

        SpecExtBO specExtBO = cookDetailBO.getSpecExtBO();
		if (specExtBO == null || CollectionUtils.isEmpty(specExtBO.getSpecBOList())) {
			// specDetailList  是空的，则使用默认规格
			String defaultLocalSpecId = entityId + CommonConstant.KOUBEI_DEFAULT_SPEC_ID;
			ItemMapping itemMapping = itemMappingService.getTpId(String.valueOf(CommonConstant.KOUBEI_PLATFORM), CommonConstant.ITEM_SKU, entityId, localItemId, defaultLocalSpecId, shopId);
			if(itemMapping == null) {
			    throw new OpenApiException(error216.getCode(), error216.getMessage(), "there is no default spec. localItemId: {}, defaultLocalSpecId: {}", localItemId, defaultLocalSpecId);
            }
            if(itemMapping.getSyncStatus() == CommonConstants.SyncStatus.FAIL) {
                throw new OpenApiException(error216.getCode(), error216.getMessage(), "the sync result is fail. localItemId: {}, localSpecId: {}", localItemId, defaultLocalSpecId);
            }
			String koubeiSkuId = itemMapping.getTpId();
			KbDishCookPrice kbDishCookPrice_ = new KbDishCookPrice();
			kbDishCookPrice_.setSkuId(koubeiSkuId);
			kbDishCookPrice_.setCookId(tpCookId);
			kbDishCookPrice_.setDishId(tpItemId);
			kbDishCookPrice_.setSellPrice(String.valueOf(cookDetailBO.getPrice()));
			kbDishCookPrice_.setMemberPrice(String.valueOf(cookDetailBO.getMemberPrice()));
			kbCookSkuPriceList.add(kbDishCookPrice_);
			return;
		}
        List<SpecBO> specBOList = specExtBO.getSpecBOList();   //  specExtBO 中存的是 specDetailId
		List<ItemMapping> skuMappingList = new ArrayList<>();
		for(SpecBO specBO : specBOList) {
            ItemMapping itemMapping = new ItemMapping();
            itemMapping.setCommonId(specBO.getSpecId());  //  specBO.getSpecId() 中存的是 specDetailId
            itemMapping.setLocalId(localItemId);
            skuMappingList.add(itemMapping);
        }

        Result<List<SpecDetail>> specDetailListResult = getSpecDetailService.querySuitSpecDetailList(localItemId, entityId);  //
        List<SpecDetail> specDetailList = specDetailListResult.getModel();
        if (!specDetailListResult.isSuccess() || CollectionUtils.isEmpty(specDetailList)) {
            throw new OpenApiException(TpTakeoutOpResultEn.error217.getCode(), TpTakeoutOpResultEn.error217.getMessage(), "fail to get spec detail. localItemId: {}, result: {}", localItemId, JSON.toJSONString(specDetailListResult));
        }

        for(ItemMapping itemMapping : skuMappingList) {
            for(SpecDetail specDetail : specDetailList) {
                if(itemMapping.getCommonId().equals(specDetail.getId())) {
                    itemMapping.setCommonId(specDetail.getSpecId());
                    break;
                }
            }
        }

		Map<String, String> map = itemMappingService.batchQueryRelateTpIds(String.valueOf(CommonConstant.KOUBEI_PLATFORM), CommonConstant.ITEM_SKU, entityId, shopId, skuMappingList);
		if (map.size() == 0) {
			throw new OpenApiException(TpTakeoutOpResultEn.error224.getCode(), TpTakeoutOpResultEn.error224.getMessage(), "there is no spec mapping record. entityId: {}, shopId: {}, localItemId: {}, skuList: {}", entityId, shopId, localItemId, JSON.toJSONString(skuMappingList));
		}
		Map<String, String> skuMap_ = new HashMap<>();
		// 去重
		Iterator<Map.Entry<String, String>> entries = map.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry<String, String> entry = entries.next();
			if (!skuMap_.containsValue(entry.getValue())) {
				skuMap_.put(entry.getKey(), entry.getValue());
			} else {
				bizLogger.error("[kb_databack] there is duplicated sku id. key: {}, value: {}", entry.getKey(), entry.getValue());
			}
		}

		for (SpecBO specBO : specBOList) {
		    for(SpecDetail specDetail : specDetailList) {
		        if(specBO.getSpecId().equals(specDetail.getId())) {
                    String localSkuId = localItemId + ":" + specDetail.getSpecId();
                    String tpSkuId = skuMap_.get(localSkuId);
                    if (StringUtils.isNotEmpty(tpSkuId)) {
                        KbDishCookPrice kbDishCookPrice = new KbDishCookPrice();
                        kbDishCookPrice.setCookId(tpCookId);
                        kbDishCookPrice.setDishId(tpItemId);
//                double price  = multipleMenuElement.getPrice() == null ? 0 : multipleMenuElement.getPrice();
                        kbDishCookPrice.setSellPrice(String.valueOf(specBO.getSpecPrice()));  //  去规格的价格
                        double memberPrice = cookDetailBO.getMemberPrice();  //  会员价时不考虑规格，规格只对原价生效
                        kbDishCookPrice.setMemberPrice(String.valueOf(memberPrice));

                        kbDishCookPrice.setSkuId(tpSkuId);
                        kbCookSkuPriceList.add(kbDishCookPrice);
                        break;

//                if(SpecDetail.PRICE_MODE_ADD == specDetail.getPriceMode()) {
//                    price += specDetail.getPriceScale();
//                } else if (SpecDetail.PRICE_MODE_SCALE == specDetail.getPriceMode()) {
//                    price *= specDetail.getPriceScale();
//                }
//                price = NumberUtil.round(price);

                    } else {
                        bizLogger.error("[kb_databack] spec mapping record is not exist. localSkuId: {}", localSkuId);
                        break;
                    }
                }
            }
		}
		if(CollectionUtils.isEmpty(kbCookSkuPriceList)) {
			throw new OpenApiException(error247.getCode(), error247.getMessage(), "there is no sku. localItemId: {}", localItemId);
		}
	}

	private void prepareKoubeiCook(KbDishCook kbDishCook, String merchantId, String entityId, Long cookId, String kbCookId, String shopId) throws OpenApiException {
		CookBO cookBO = getCookBO(entityId, cookId, shopId);
		prepareCookBO(cookBO, kbDishCook, merchantId, kbCookId, shopId);
	}


	/**
	 * 查询本地商品对象
	 *
	 * @param entityId
	 * @param menuId
	 * @return
	 * @throws OpenApiException
	 */
	private Menu getMenu(String entityId, String menuId) throws OpenApiException {
		Result<Menu> result = getMenuService.queryMenuWithoutValid(entityId, menuId);
		Menu menu = result.getModel();
		if (!result.isSuccess() || menu == null) {
			throw new OpenApiException(TpTakeoutOpResultEn.error210.getCode(), TpTakeoutOpResultEn.error210.getMessage(), "fail to get local item. entityId: {}, menuId: {}, result: {}", entityId, menuId, JSON.toJSONString(result));
		}
		return menu;
	}

	/**
	 * 查询本地菜类对象
	 *
	 * @param entityId
	 * @param categoryId
	 * @return
	 * @throws OpenApiException
	 */
	private KindMenu getKindMenu(String entityId, String categoryId) throws OpenApiException {
		Result<KindMenu> result = getMenuService.findKindMenu(entityId, categoryId);
		KindMenu kindMenu = result.getModel();
		if (!result.isSuccess() || kindMenu == null) {
			throw new OpenApiException(TpTakeoutOpResultEn.error211.getCode(), TpTakeoutOpResultEn.error211.getMessage(), "fail to get local item. entityId: {}, menuId: {}, result: {}", entityId, categoryId, JSON.toJSONString(result));
		}
		return kindMenu;
	}

}
















