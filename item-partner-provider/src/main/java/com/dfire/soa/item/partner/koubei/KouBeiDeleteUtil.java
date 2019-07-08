package com.dfire.soa.item.partner.koubei;

import com.alibaba.fastjson.JSON;
import com.dfire.open.takeout.bo.kb.*;
import com.dfire.open.takeout.enumeration.KouBeiDictionaryBizTypeEnum;
import com.dfire.open.takeout.enumeration.KouBeiDishBizTypeEnum;
import com.dfire.open.takeout.enumeration.KouBeiDishStatusEnum;
import com.dfire.open.takeout.enumeration.KouBeiSyncTypeEnum;
import com.dfire.open.takeout.service.IKouBeiDishCookService;
import com.dfire.soa.item.bo.KindMenu;
import com.dfire.soa.item.bo.Menu;
import com.dfire.soa.item.bo.Spec;
import com.dfire.soa.item.bo.SuitMenuDetail;
import com.dfire.soa.item.dto.UnitExtDto;
import com.dfire.soa.item.partner.bo.ItemMapping;
import com.dfire.soa.item.partner.bo.query.ItemMappingQuery;
import com.dfire.soa.item.partner.constant.CommonConstant;
import com.dfire.soa.item.partner.koubei.handler.KoubeiCookDishHandler;
import com.dfire.soa.item.partner.service.internal.IItemMappingService;
import com.twodfire.exception.BizException;
import com.twodfire.redis.CodisService;
import com.twodfire.share.result.Result;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 口碑数据回流工具类
 * Created by zhishi on 2018/8/16 0016.
 */
@Component
public class KouBeiDeleteUtil {
    @Resource
    private IKouBeiDishCookService kouBeiDishCookService;
    @Resource
    private KoubeiCookDishHandler koubeiCookDishHandler;
    @Resource
    private IItemMappingService itemMappingService;
    @Resource
    private CodisService codisService;
//    @Resource
//    private CodisService codisService;

    private static final Logger bizLog = LoggerFactory.getLogger(CommonConstant.BIZ_LOG);

    /**
     * 删除菜类
     * @param idType 类型 （CommonConstant.ITEM_CATEGORY：普通菜类  CommonConstant.ITEM_COOK_CATEGORY：菜谱菜类）
     * @param merchantId merchantId
     * @param kindMenu 菜类
     * @return boolean
     */
    public boolean deleteKindMenuId(byte idType, String merchantId, String shopId, KindMenu kindMenu){
        String entityId = kindMenu.getEntityId();
        String localId = kindMenu.getId();
        String platCode = String.valueOf(CommonConstant.KOUBEI_PLATFORM);
        boolean success = true;

        //查询菜类映射关系(localId->tpId)
        ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopId, platCode, (int)idType, localId, null);
        List<ItemMapping> itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
        if(CollectionUtils.isEmpty(itemMappings)){
            return true;
        }
        ItemMapping itemMapping = itemMappings.get(0);
        if(StringUtils.isBlank(itemMapping.getTpId())) {
            itemMapping.setIsValid(0);
            itemMappingService.updateItemMapping(itemMapping);
            return true;
        }

        //删除菜类(口碑)
        KbDishDictionaryRequest request = new KbDishDictionaryRequest();
        request.setName(kindMenu.getName());
        request.setMerchantId(merchantId);
        request.setStatus(KouBeiDishStatusEnum.open);
        request.setDictionaryId(itemMapping.getTpId());
        request.setExtInfo(JSON.toJSONString(new HashMap<>()));
        request.setCreateUser("ADMIN");
        request.setUpdateUser("ADMIN");
        Result<String> deleteResult = kouBeiDishCookService.dishDictionarySync(request, KouBeiSyncTypeEnum.del, KouBeiDictionaryBizTypeEnum.catetory);
        if (!deleteResult.isSuccess()) {
            bizLog.error("[kb_databack]kouBeiDishCookService.dishDictionarySync(request, syncType, bizType) failed. request: " + JSON.toJSONString(request) + ", syncType: " + JSON.toJSONString(KouBeiSyncTypeEnum.del) + ", bizType: " + JSON.toJSONString(KouBeiDictionaryBizTypeEnum.catetory) + ", deleteResult: " + JSON.toJSONString(deleteResult));
            if(!(deleteResult.getMessage().contains("操作的字典数据不存在或已删除") || deleteResult.getMessage().contains("操作的数据不存在"))){
                success = false;
            }
        }

        //更新菜类映射关系
        itemMapping.setSyncStatus(deleteResult.isSuccess() ? 1 : 0);
        itemMapping.setSyncResult(deleteResult.getMessage());
        itemMapping.setIsValid(success ? 0 : 1);
        itemMappingService.updateItemMapping(itemMapping);
        return success;
    }

    /**
     * 删除单位
     * @param merchantId merchantId
     * @param unit 单位
     * @return boolean
     */
    public boolean deleteUnitId(String merchantId, String shopId, UnitExtDto unit){
        String entityId = unit.getEntityId();
        String localId = unit.getUnitId();
        String platCode = String.valueOf(CommonConstant.KOUBEI_PLATFORM);
        byte idType = CommonConstant.ITEM_UNIT;
        boolean success = true;

        //查询单位映射关系(localId->tpId)
        ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopId, platCode, (int)idType, localId, null);
        List<ItemMapping> itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
        if(CollectionUtils.isEmpty(itemMappings)){
            return true;
        }
        ItemMapping itemMapping = itemMappings.get(0);
        if(StringUtils.isBlank(itemMapping.getTpId())) {
            itemMapping.setIsValid(0);
            itemMappingService.updateItemMapping(itemMapping);
            return true;
        }

        //删除单位(口碑)
        KbDishDictionaryRequest request = new KbDishDictionaryRequest();
        request.setName(unit.getUnitDesc());
        request.setMerchantId(merchantId);
        request.setStatus(KouBeiDishStatusEnum.open);
        request.setDictionaryId(itemMapping.getTpId());
        request.setExtInfo(JSON.toJSONString(new HashMap<>()));
        request.setCreateUser("ADMIN");
        request.setUpdateUser("ADMIN");
        Result<String> deleteResult = kouBeiDishCookService.dishDictionarySync(request, KouBeiSyncTypeEnum.del, KouBeiDictionaryBizTypeEnum.unit);
        if (!deleteResult.isSuccess()) {
            bizLog.error("[kb_databack]kouBeiDishCookService.dishDictionarySync(request, syncType, bizType) failed. request: " + JSON.toJSONString(request) + ", syncType: " + JSON.toJSONString(KouBeiSyncTypeEnum.del) + ", bizType: " + JSON.toJSONString(KouBeiDictionaryBizTypeEnum.unit) + ", deleteResult: " + JSON.toJSONString(deleteResult));
            if(!(deleteResult.getMessage().contains("操作的字典数据不存在或已删除") || deleteResult.getMessage().contains("操作的数据不存在"))){
                success = false;
            }
        }

        //更新单位映射关系
        itemMapping.setSyncStatus(deleteResult.isSuccess() ? 1 : 0);
        itemMapping.setSyncResult(deleteResult.getMessage());
        itemMapping.setIsValid(success ? 0 : 1);
        itemMappingService.updateItemMapping(itemMapping);
        return success;
    }

    /**
     * 删除菜品组
     * @param merchantId merchantId
     * @param suitMenuDetail 菜品组
     * @return boolean
     */
    public boolean deleteGroupId(String merchantId, String shopId, SuitMenuDetail suitMenuDetail){
        String entityId = suitMenuDetail.getEntityId();
        String localId = suitMenuDetail.getId();
        String platCode = String.valueOf(CommonConstant.KOUBEI_PLATFORM);
        byte idType = CommonConstant.ITEM_GROUP;
        boolean success = true;

        //查询菜品组映射关系(localId->tpId)
        ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopId, platCode, (int)idType, localId, null);
        List<ItemMapping> itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
        if(CollectionUtils.isEmpty(itemMappings)){
            return true;
        }
        ItemMapping itemMapping = itemMappings.get(0);
        if(StringUtils.isBlank(itemMapping.getTpId())) {
            itemMapping.setIsValid(0);
            itemMappingService.updateItemMapping(itemMapping);
            return true;
        }

        //删除菜品组(口碑)
        KbdishGroupRequest request = new KbdishGroupRequest();
        KbdishGroup kbdishGroup = new KbdishGroup();
        kbdishGroup.setGroupName(suitMenuDetail.getName());
        kbdishGroup.setMerchantId(merchantId);
        kbdishGroup.setStatus(KouBeiDishStatusEnum.open);
        kbdishGroup.setGroupId(itemMapping.getTpId());
        kbdishGroup.setGroupVersion(String.valueOf(System.currentTimeMillis()));
        kbdishGroup.setCreateUser("ADMIN");
        kbdishGroup.setUpdateUser("ADMIN");
        request.setKbdishGroup(kbdishGroup);
        Result<KbdishGroupResponse> deleteResult = kouBeiDishCookService.dishGroupSync(request, KouBeiSyncTypeEnum.del);
        if(!deleteResult.isSuccess()) {
            bizLog.error("kouBeiDishCookService.dishGroupSync(request, syncType) failed. request: " + JSON.toJSONString(request) + ", syncType: " + JSON.toJSONString(StringUtils.isNotBlank(kbdishGroup.getGroupId()) ? KouBeiSyncTypeEnum.update : KouBeiSyncTypeEnum.add) + ", deleteResult: " + JSON.toJSONString(deleteResult));
            if(!deleteResult.getMessage().contains("操作的数据不存在")){
                success = false;
            }
        }

        //更新菜品组映射关系
        itemMapping.setSyncStatus(deleteResult.isSuccess() ? 1 : 0);
        itemMapping.setSyncResult(deleteResult.getMessage());
        itemMapping.setIsValid(success ? 0 : 1);
        itemMappingService.updateItemMapping(itemMapping);
        return success;
    }

    /**
     * 删除规格
     * @param merchantId merchantId
     * @param spec 规格
     * @return boolean
     */
    public boolean deleteSpecId(String merchantId, String shopId, Spec spec) {
        String entityId = spec.getEntityId();
        String localId = spec.getId();
        String platCode = String.valueOf(CommonConstant.KOUBEI_PLATFORM);
        byte idType = CommonConstant.ITEM_SPEC;
        boolean success = true;

        //查询规格spec映射关系(localId->tpId)
        ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopId, platCode, (int)idType, localId, null);
        List<ItemMapping> itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
        if(CollectionUtils.isEmpty(itemMappings)){
            return true;
        }
        ItemMapping itemMapping = itemMappings.get(0);
        if(StringUtils.isBlank(itemMapping.getTpId())) {
            itemMapping.setIsValid(0);
            itemMappingService.updateItemMapping(itemMapping);
            return true;
        }

        //删除规格(口碑)
        KbDishDictionaryRequest request = new KbDishDictionaryRequest();
        request.setName(spec.getName());
        request.setMerchantId(merchantId);
        request.setDictionaryId(itemMapping.getTpId());
        request.setStatus(KouBeiDishStatusEnum.open);
        request.setCreateUser("ADMIN");
        request.setUpdateUser("ADMIN");
        request.setExtInfo(JSON.toJSONString(new HashMap<>()));
        Result<String> deleteResult = kouBeiDishCookService.dishDictionarySync(request, KouBeiSyncTypeEnum.del, KouBeiDictionaryBizTypeEnum.spec);
        if (!deleteResult.isSuccess()) {
            bizLog.error("[kb_databack]kouBeiDishCookService.dishDictionarySync(request, syncType, bizType) failed. request: " + JSON.toJSONString(request) + ", syncType: " + JSON.toJSONString(KouBeiSyncTypeEnum.del) + ", bizType: " + JSON.toJSONString(KouBeiDictionaryBizTypeEnum.unit) + ", deleteResult: " + JSON.toJSONString(deleteResult));
            if(!(deleteResult.getMessage().contains("操作的字典数据不存在或已删除") || deleteResult.getMessage().contains("操作的数据不存在"))){
                success = false;
            }
        }

        //更新规格映射关系
        itemMapping.setSyncStatus(deleteResult.isSuccess() ? 1 : 0);
        itemMapping.setSyncResult(deleteResult.getMessage());
        itemMapping.setIsValid(success ? 0 : 1);
        itemMappingService.updateItemMapping(itemMapping);
        return success;
    }

    /**
     * 删除菜品
     * @param merchantId merchantId
     * @param menu 菜品
     * @return boolean
     */
    public boolean deleteDishId(String merchantId, String shopId, Menu menu){
        //删除菜品处理
        String entityId = menu.getEntityId();
        String localId = menu.getId();
        String platCode = String.valueOf(CommonConstant.KOUBEI_PLATFORM);
        byte idType = CommonConstant.ITEM;

        //抢锁
        while (true) {
            Long cacheFlag =  codisService.setnx("dish"+ entityId + localId, 2, "true");//更新加锁2秒
            if (cacheFlag != null && cacheFlag > 0) {
                break;
            }
            try {
                Thread.sleep(CommonConstant.SLEEP_TIME);
            } catch (InterruptedException e) {
                throw new BizException("[kb_databack]线程中断异常");
            }
        }

        try {
            //查询菜品映射关系
            ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopId, platCode, (int)idType, localId, null);
            List<ItemMapping> itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
            if(CollectionUtils.isEmpty(itemMappings)){
                return true;
            }
            ItemMapping itemMapping = itemMappings.get(0);
            if(StringUtils.isBlank(itemMapping.getTpId())) {
                itemMapping.setIsValid(0);
                itemMappingService.updateItemMapping(itemMapping);
                return true;
            }

            //删除菜品（口碑）
            KbDishRequest request = new KbDishRequest();
            KbDish kbDish = new KbDish();
            kbDish.setDishName(menu.getName());
            kbDish.setMerchantId(merchantId);
            kbDish.setDishId(itemMapping.getTpId());
            kbDish.setStatus(KouBeiDishStatusEnum.open);
            kbDish.setCreateUser("ADMIN");
            kbDish.setUpdateUser("ADMIN");
            request.setKbDish(kbDish);
            Result<KbDishResponse> deleteResult = kouBeiDishCookService.dishSync(request, KouBeiSyncTypeEnum.del, KouBeiDishBizTypeEnum.dish);
            if (!deleteResult.isSuccess()) {
                bizLog.error("[kb_databack] kouBeiDishCookService.dishSync(request, syncType, bizType) failed. request:{}, syncType:{}, bizType{}, deleteResult:{}", JSON.toJSONString(request), KouBeiSyncTypeEnum.del, KouBeiDishBizTypeEnum.dish, JSON.toJSON(deleteResult));
                if (!deleteResult.getMessage().contains("操作的数据不存在")) {
                    throw new BizException("[kb_databack]删除菜品（口碑）失败:" + deleteResult.getMessage());
                }
            }

            //删除菜品关联
            itemMapping.setSyncStatus(deleteResult.isSuccess() ? 1 : 0);
            itemMapping.setSyncResult(deleteResult.getMessage());
            itemMapping.setIsValid(0);
            itemMappingService.updateItemMapping(itemMapping);
            return true;
        } finally {
            codisService.del("dish"+ entityId + localId);
        }
    }

    /**
     * 删除sku
     * @param merchantId merchantId
     * @param entityId entityId
     * @param menuId 菜品id
     * @param specId 规格id
     * @return boolean
     */
    public boolean deleteSkuId(String merchantId, String shopId, String entityId, String menuId, String specId){
        String dishId = null;
        String tdSpecId = null;
        String platCode = String.valueOf(CommonConstant.KOUBEI_PLATFORM);
        byte idType = CommonConstant.ITEM_SKU;

        //抢锁
        while (true) {
            Long cacheFlag = codisService.setnx("sku" + entityId + menuId + specId, 2, "true");//更新加锁2秒
            if (cacheFlag != null && cacheFlag > 0) {
                break;
            }
            try {
                Thread.sleep(CommonConstant.SLEEP_TIME);
            } catch (InterruptedException e) {
                throw new BizException("[kb_databack]线程中断异常");
            }
        }

        try {
            //查询sku映射关系
            ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopId, platCode, (int)idType, menuId, null);
            itemMappingQuery.setCommonId(specId);
            List<ItemMapping> itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
            if(CollectionUtils.isEmpty(itemMappings)){
                return true;
            }
            ItemMapping itemMapping = itemMappings.get(0);
            if(StringUtils.isBlank(itemMapping.getTpId())) {
                itemMapping.setIsValid(0);
                itemMappingService.updateItemMapping(itemMapping);
                return true;
            }

            //查询规格映射关系
            ItemMappingQuery itemMappingQuery_spec = new ItemMappingQuery(entityId, shopId, platCode, (int) CommonConstant.ITEM_SPEC, specId, null);
            List<ItemMapping> itemMappings_spec = itemMappingService.getItemMappingListByQuery(itemMappingQuery_spec);
            if (CollectionUtils.isNotEmpty(itemMappings_spec)) {
                tdSpecId = itemMappings_spec.get(0).getTpId();
            }

            //查询菜品映射关系
            ItemMappingQuery itemMappingQuery_menu = new ItemMappingQuery(entityId, shopId, platCode, (int) CommonConstant.ITEM, menuId, null);
            List<ItemMapping> itemMappings_menu = itemMappingService.getItemMappingListByQuery(itemMappingQuery_menu);
            if (CollectionUtils.isNotEmpty(itemMappings_menu)) {
                dishId = itemMappings_menu.get(0).getTpId();
            }

            //删除sku(口碑)
            if (StringUtils.isNotBlank(dishId) && StringUtils.isNotBlank(tdSpecId)) {
                KbDishRequest request = new KbDishRequest();
                KbDish kbDish = new KbDish();
                KbDishSku kbDishSku = new KbDishSku();
                List<KbDishSku> kbDishSkuList = new ArrayList<>();
                kbDish.setMerchantId(merchantId);
                kbDish.setDishId(dishId);
                kbDish.setDishSkuList(kbDishSkuList);
                kbDish.setUpdateUser("ADMIN");
                kbDish.setCreateUser("ADMIN");
                kbDishSku.setDishId(dishId);
                kbDishSku.setSkuId(itemMapping.getTpId());
                kbDishSku.setSpecCode01(tdSpecId);
                kbDishSkuList.add(kbDishSku);
                request.setKbDish(kbDish);
                Result<KbDishResponse> deleteResult = kouBeiDishCookService.dishSync(request, KouBeiSyncTypeEnum.del, KouBeiDishBizTypeEnum.sku);
                if (!deleteResult.isSuccess()) {
                    bizLog.error("[kb_databack] kouBeiDishCookService.dishSync(request, KouBeiSyncTypeEnum.del, KouBeiDishBizTypeEnum.sku) failed. request:{}, deleteResult:{} ", JSON.toJSONString(request), JSON.toJSONString(deleteResult));
                    if (!deleteResult.getMessage().contains("操作的数据不存在")) {
                        throw new BizException("[kb_databack]删除Sku（口碑）失败:" + deleteResult.getMessage());
                    }
                }

                //删除sku映射关系
                itemMapping.setSyncStatus(deleteResult.isSuccess() ? 1 : 0);
                itemMapping.setSyncResult(deleteResult.getMessage());
                itemMapping.setIsValid(0);
                itemMappingService.updateItemMapping(itemMapping);
                return true;
            }

            itemMapping.setIsValid(0);
            itemMappingService.updateItemMapping(itemMapping);
            return true;
        } finally {
            codisService.del("sku" + entityId + menuId + specId);
        }
    }

    /**
     * 删除菜谱-菜品关系
     * @param shopId shopId
     * @param merchantId merchantId
     * @param entityId entityId
     * @param menuId 菜品id
     * @return boolean
     */
    public boolean deleteCookDish(String shopId, String merchantId, String entityId, String menuId){
        try {
            return koubeiCookDishHandler.delDishCookRel(shopId, merchantId, entityId, menuId);
        } catch (Exception e) {
            bizLog.error("fail to del dish cook. merchantId: {}. shopId: {}", merchantId, shopId, e);
        }
        return false;
    }

    /**
     * 删除菜谱-sku关系
     * @param entityId entityId
     * @param merchantId merchantId
     * @param shopId shopId
     * @param menuId 菜品id
     * @param specId 规格id
     * @return boolean
     */
    public boolean deleteCookSku(String entityId, String merchantId, String shopId, String menuId, String specId){
        try{
            return koubeiCookDishHandler.deleteMultipleMenuElementSku(entityId,merchantId,shopId,menuId,specId);
        }catch (Exception e){
            bizLog.error("[kb_databack]删除菜谱SKU绑定关系失败！异常:", e);
        }
        return false;
    }

    /**
     * 删除菜谱
     * @param entityId entityId
     * @param merchantId merchantId
     * @param cookId 菜谱id
     * @return boolean
     */
    public boolean deleteCook(String entityId, String merchantId, Long cookId, String shopId){
        try{
            return koubeiCookDishHandler.deleteMultipleMenuDetail(merchantId, entityId, cookId, shopId);
        }catch (Exception e){
            bizLog.error("[kb_databack]删除菜谱失败！异常:", e);
        }
        return false;
    }

	/**
	 * 删除加料
	 *
	 * @param merchantId merchantId
	 * @param menu       加料
	 * @return boolean
	 */
	public boolean deleteAffiliateId(String merchantId, String shopId, Menu menu) {
		String entityId = menu.getEntityId();
		String localId = menu.getId();
		String platCode = String.valueOf(CommonConstant.KOUBEI_PLATFORM);
		byte idType = CommonConstant.ADDITION;
		boolean success = true;

		//查询加料映射关系(localId->tpId)
		ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopId, platCode, (int) idType, localId, null);
		List<ItemMapping> itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
		if (CollectionUtils.isEmpty(itemMappings)) {
			return true;
		}
		ItemMapping itemMapping = itemMappings.get(0);
		if (StringUtils.isBlank(itemMapping.getTpId())) {
			itemMapping.setIsValid(0);
			itemMappingService.updateItemMapping(itemMapping);
			return true;
		}

		//删除加料(口碑)
		KbDishAddition request = new KbDishAddition();
		request.setAddPrice(String.valueOf(menu.getPrice()));
		request.setMaterialName(menu.getName());
		request.setMaterialType("SHOP");
		request.setPublicId(shopId);
		request.setMerchantId(merchantId);
		request.setMaterialId(itemMapping.getTpId());
		request.setCreateUser("ADMIN");
		request.setUpdateUser("ADMIN");
		request.setExtInfo(JSON.toJSONString(new HashMap<>()));
		Result<String> deleteResult = kouBeiDishCookService.dishAdditionSync(request, KouBeiSyncTypeEnum.del);
		if (!deleteResult.isSuccess()) {
			bizLog.error("[kb_databack]kouBeiDishCookService.dishAdditionSync(request, syncType) failed. request: " + JSON.toJSONString(request) + ", syncType: " + JSON.toJSONString(KouBeiSyncTypeEnum.del) + ", bizType: " + "addition" + ", deleteResult: " + JSON.toJSONString(deleteResult));
			if (!(deleteResult.getMessage().contains("操作的字典数据不存在或已删除") || deleteResult.getMessage().contains("操作的数据不存在"))) {
				success = false;
			}
		}

		//更新加料映射关系
        itemMapping.setSyncStatus(deleteResult.isSuccess() ? 1 : 0);
        itemMapping.setSyncResult(deleteResult.getMessage());
        itemMapping.setIsValid(0);
		itemMappingService.updateItemMapping(itemMapping);
		return success;
	}
}
