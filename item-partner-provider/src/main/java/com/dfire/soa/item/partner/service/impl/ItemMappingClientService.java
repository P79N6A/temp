package com.dfire.soa.item.partner.service.impl;

import com.alibaba.fastjson.JSON;
import com.dfire.soa.item.partner.bo.ItemMapping;
import com.dfire.soa.item.partner.bo.query.ItemMappingQuery;
import com.dfire.soa.item.partner.bo.query.CommonIdModel;
import com.dfire.soa.item.partner.service.IItemMappingClientService;
import com.dfire.soa.item.partner.service.internal.IItemMappingService;
import com.twodfire.exception.BizException;
import com.twodfire.share.result.Result;
import com.twodfire.share.result.ResultSupport;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.dfire.soa.item.partner.enums.EnumErrorType.SYSTEM_BUSINESS_ERROR;
import static com.dfire.soa.item.partner.enums.EnumErrorType.SYSTEM_DEFAULT_ERROR;

@Service
public class ItemMappingClientService implements IItemMappingClientService {
	
	private static final Logger logger = LoggerFactory.getLogger(ItemMappingClientService.class);
	
	@Resource
	private IItemMappingService itemMappingService;
	
	/**
	 * 保存
	 */
	@Override
	public Result<ItemMapping> saveOrUpdateItemMapping(ItemMapping itemMapping){
		Result<ItemMapping> result = new ResultSupport<>();
		int count;
		try{
			if(itemMapping.getId()==null){
				count = itemMappingService.saveItemMapping(itemMapping);
			}else {
				count = itemMappingService.updateItemMapping(itemMapping);
			}
			if(count < 1) {
				result.setSuccess(false);
			}
			result.setModel(itemMapping);
			return result;
		}catch(BizException e){
			logger.error("[ItemMapping]saveItemMapping() failed. itemMapping:{}.",JSON.toJSONString(itemMapping), e);
			return new ResultSupport<>(false, SYSTEM_BUSINESS_ERROR.getErrorCode(), SYSTEM_BUSINESS_ERROR.getErrorMsg() +":"+ e.getMessage());
		}catch(Exception e) {
			logger.error("[ItemMapping]saveItemMapping() failed. itemMapping:{}.",JSON.toJSONString(itemMapping), e);
			return new ResultSupport<>(false, SYSTEM_DEFAULT_ERROR.getErrorCode(), SYSTEM_DEFAULT_ERROR.getErrorMsg());
		}
	}

	/**
	 * 逻辑删除
	 */
	@Override
	public Result<Integer> deleteItemMappingById(String entityId, Long id){
		Result<Integer> result = new ResultSupport<>();
		try{
			result.setModel(itemMappingService.deleteItemMappingById(entityId, id));
			return result;
		}catch(BizException e){
			logger.error("[ItemMapping]deleteItemMapping() failed. entityId:{}, id:{}.", JSON.toJSONString(entityId), JSON.toJSONString(id), e);
			return new ResultSupport<>(false, SYSTEM_BUSINESS_ERROR.getErrorCode(), SYSTEM_BUSINESS_ERROR.getErrorMsg() +":"+ e.getMessage());
		}catch(Exception e){
			logger.error("[ItemMapping]deleteItemMapping() failed. entityId:{}, id:{}.", JSON.toJSONString(entityId), JSON.toJSONString(id), e);
			return new ResultSupport<>(false, SYSTEM_DEFAULT_ERROR.getErrorCode(), SYSTEM_DEFAULT_ERROR.getErrorMsg());
		}
	}
	
	/**
	 * 根据id查询
	 */
	@Override
	public Result<ItemMapping> getItemMappingById(String entityId, Long id){
		Result<ItemMapping> result = new ResultSupport<>();
		try{
			result.setModel(itemMappingService.getItemMappingById(entityId, id));
			return result;
		}catch(BizException e){
			logger.error("[ItemMapping]getItemMappingById() failed. entityId:{}, id:{}.", JSON.toJSONString(entityId), JSON.toJSONString(id), e);
			return new ResultSupport<>(false, SYSTEM_BUSINESS_ERROR.getErrorCode(), SYSTEM_BUSINESS_ERROR.getErrorMsg() +":"+ e.getMessage());
		}catch(Exception e){
			logger.error("[ItemMapping]getItemMappingById() failed. entityId:{}, id:{}.", JSON.toJSONString(entityId), JSON.toJSONString(id), e);
			return new ResultSupport<>(false, SYSTEM_DEFAULT_ERROR.getErrorCode(), SYSTEM_DEFAULT_ERROR.getErrorMsg());
		}
	}
	
	/**
	 * 根据query查询
	 */
	@Override
	public Result<List<ItemMapping>> getItemMappingListByQuery(ItemMappingQuery query){
		Result<List<ItemMapping>> result = new ResultSupport<>();
		try{
			result.setModel(itemMappingService.getItemMappingListByQuery(query));
			return result;
		}catch(BizException e){
			logger.error("[ItemMapping]getItemMappingListByQuery() failed. query:{}.", JSON.toJSONString(query), e);
			return new ResultSupport<>(false, SYSTEM_BUSINESS_ERROR.getErrorCode(), SYSTEM_BUSINESS_ERROR.getErrorMsg() +":"+ e.getMessage());
		}catch(Exception e){
			logger.error("[ItemMapping]getItemMappingListByQuery() failed. query:{}.", JSON.toJSONString(query), e);
			return new ResultSupport<>(false, SYSTEM_DEFAULT_ERROR.getErrorCode(), SYSTEM_DEFAULT_ERROR.getErrorMsg());
		}
	}

	@Override
	public Result<List<ItemMapping>> getItemMappingListByLocalIds(String platCode, byte idType, String entityId, String tpShopId, List<String> localIds) {
		Result<List<ItemMapping>> result = new ResultSupport<>();
		try{
			if (StringUtils.isBlank(platCode)){
				return new ResultSupport<>(false, SYSTEM_BUSINESS_ERROR.getErrorCode(), "platCode不能为空");
			}
			if (StringUtils.isBlank(entityId)){
				return new ResultSupport<>(false, SYSTEM_BUSINESS_ERROR.getErrorCode(), "entityId不能为空");
			}
			if (StringUtils.isBlank(tpShopId)){
				return new ResultSupport<>(false, SYSTEM_BUSINESS_ERROR.getErrorCode(), "tpShopId不能为空");
			}
			if(idType==8){
				return new ResultSupport<>(false, SYSTEM_BUSINESS_ERROR.getErrorCode(), "idType不能为8");
			}
			if (CollectionUtils.isEmpty(localIds)){
				result.setModel(new ArrayList<>());
				return result;
			}

			result.setModel(itemMappingService.getItemMappingListByLocalIds(platCode, idType, entityId, tpShopId, localIds));
			return result;
		}catch(BizException e){
			logger.error("[ItemMapping]getItemMappingListByLocalIds() failed. platCode:{}, idType:{}, entityId:{}, tpShopId:{}, localIds:{}.", JSON.toJSONString(platCode), JSON.toJSONString(idType), JSON.toJSONString(entityId), JSON.toJSONString(tpShopId), JSON.toJSONString(localIds), e);
			return new ResultSupport<>(false, SYSTEM_BUSINESS_ERROR.getErrorCode(), SYSTEM_BUSINESS_ERROR.getErrorMsg() +":"+ e.getMessage());
		}catch(Exception e){
			logger.error("[ItemMapping]getItemMappingListByLocalIds() failed. platCode:{}, idType:{}, entityId:{}, tpShopId:{}, localIds:{}.", JSON.toJSONString(platCode), JSON.toJSONString(idType), JSON.toJSONString(entityId), JSON.toJSONString(tpShopId), JSON.toJSONString(localIds), e);
			return new ResultSupport<>(false, SYSTEM_DEFAULT_ERROR.getErrorCode(), SYSTEM_DEFAULT_ERROR.getErrorMsg());
		}
	}

	@Override
	public Result<List<ItemMapping>> getItemMappingListByCommonIdModels(String platCode, byte idType, String entityId, String tpShopId, List<CommonIdModel> commonIdModels) {
		Result<List<ItemMapping>> result = new ResultSupport<>();
		try{
			if (StringUtils.isBlank(platCode)){
				return new ResultSupport<>(false, SYSTEM_BUSINESS_ERROR.getErrorCode(), "platCode不能为空");
			}
			if (StringUtils.isBlank(entityId)){
				return new ResultSupport<>(false, SYSTEM_BUSINESS_ERROR.getErrorCode(), "entityId不能为空");
			}
			if (StringUtils.isBlank(tpShopId)){
				return new ResultSupport<>(false, SYSTEM_BUSINESS_ERROR.getErrorCode(), "tpShopId不能为空");
			}
			if (CollectionUtils.isEmpty(commonIdModels)){
				result.setModel(new ArrayList<>());
				return result;
			}

			result.setModel(itemMappingService.getItemMappingListByCommonIdModels(platCode, idType, entityId, tpShopId, commonIdModels));
			return result;
		}catch(BizException e){
			logger.error("[ItemMapping]getItemMappingListByLocalIdAndCommonIdMap() failed. platCode:{}, idType:{}, entityId:{}, tpShopId:{}, commonIdModels:{}.", JSON.toJSONString(platCode), JSON.toJSONString(idType), JSON.toJSONString(entityId), JSON.toJSONString(tpShopId), JSON.toJSONString(commonIdModels), e);
			return new ResultSupport<>(false, SYSTEM_BUSINESS_ERROR.getErrorCode(), SYSTEM_BUSINESS_ERROR.getErrorMsg() +":"+ e.getMessage());
		}catch(Exception e){
			logger.error("[ItemMapping]getItemMappingListByLocalIdAndCommonIdMap() failed. platCode:{}, idType:{}, entityId:{}, tpShopId:{}, commonIdModels:{}.", JSON.toJSONString(platCode), JSON.toJSONString(idType), JSON.toJSONString(entityId), JSON.toJSONString(tpShopId), JSON.toJSONString(commonIdModels), e);
			return new ResultSupport<>(false, SYSTEM_DEFAULT_ERROR.getErrorCode(), SYSTEM_DEFAULT_ERROR.getErrorMsg());
		}
	}

	@Override
	public Result<List<ItemMapping>> getItemMappingListByTpIds(String platCode, byte idType, String entityId, String tpShopId, List<String> tpIds) {
		Result<List<ItemMapping>> result = new ResultSupport<>();
		try{
			if (StringUtils.isBlank(platCode)){
				return new ResultSupport<>(false, SYSTEM_BUSINESS_ERROR.getErrorCode(), "platCode不能为空");
			}
			if (StringUtils.isBlank(entityId)){
				return new ResultSupport<>(false, SYSTEM_BUSINESS_ERROR.getErrorCode(), "entityId不能为空");
			}
			if (StringUtils.isBlank(tpShopId)){
				return new ResultSupport<>(false, SYSTEM_BUSINESS_ERROR.getErrorCode(), "tpShopId不能为空");
			}
			if (CollectionUtils.isEmpty(tpIds)){
				result.setModel(new ArrayList<>());
				return result;
			}

			result.setModel(itemMappingService.getItemMappingListByTpIds(platCode, idType, entityId, tpShopId, tpIds));
			return result;
		}catch(BizException e){
			logger.error("[ItemMapping]getItemMappingListByTpIds() failed. platCode:{}, idType:{}, entityId:{}, tpShopId:{}, tpIds:{}.", JSON.toJSONString(platCode), JSON.toJSONString(idType), JSON.toJSONString(entityId), JSON.toJSONString(tpShopId), JSON.toJSONString(tpIds), e);
			return new ResultSupport<>(false, SYSTEM_BUSINESS_ERROR.getErrorCode(), SYSTEM_BUSINESS_ERROR.getErrorMsg() +":"+ e.getMessage());
		}catch(Exception e){
			logger.error("[ItemMapping]getItemMappingListByTpIds() failed. platCode:{}, idType:{}, entityId:{}, tpShopId:{}, tpIds:{}.", JSON.toJSONString(platCode), JSON.toJSONString(idType), JSON.toJSONString(entityId), JSON.toJSONString(tpShopId), JSON.toJSONString(tpIds), e);
			return new ResultSupport<>(false, SYSTEM_DEFAULT_ERROR.getErrorCode(), SYSTEM_DEFAULT_ERROR.getErrorMsg());
		}
	}

	@Override
	public Result<List<ItemMapping>> getItemMappingListByQueryWithoutEntityId(ItemMappingQuery query) {
		Result<List<ItemMapping>> result = new ResultSupport<>();
		try {
			result.setModel(itemMappingService.getItemMappingListByQueryWithoutEntityId(query));
			return result;
		} catch(BizException e){
			logger.error("[ItemMapping] getItemMappingListByQueryWithoutEntityId() failed. query:{}.", JSON.toJSONString(query), e);
			return new ResultSupport<>(false, SYSTEM_BUSINESS_ERROR.getErrorCode(), SYSTEM_BUSINESS_ERROR.getErrorMsg() +":"+ e.getMessage());
		} catch(Exception e){
			logger.error("[ItemMapping] getItemMappingListByQueryWithoutEntityId() failed. query:{}.", JSON.toJSONString(query), e);
			return new ResultSupport<>(false, SYSTEM_DEFAULT_ERROR.getErrorCode(), SYSTEM_DEFAULT_ERROR.getErrorMsg());
		}
	}

	@Override
	public Result<Map<String, String>> batchQueryRelateTpIds(String platCode, byte idType, String entityId, String tpShopId, List<ItemMapping> itemMappings) {
		Result<Map<String, String>> result = new ResultSupport<>();
		try {
			Map<String, String> map = itemMappingService.batchQueryRelateTpIds(platCode, idType, entityId, tpShopId, itemMappings);
			result.setModel(map);
		} catch (Exception e) {
			logger.error("[ItemMapping] fail to batch query relate tp ids. entityId: {}, itemMappings: {}", entityId, JSON.toJSONString(itemMappings), e);
			result.setSuccess(false);
		}
		return result;
	}

	public Result<List<ItemMapping>> batchQueryRelateLocalIds(String platCode, byte idType, String entityId, String tpShopId, List<String> tpIdList) {
        Result<List<ItemMapping>> result = new ResultSupport<>();
        try {
            List<ItemMapping> list = itemMappingService.batchQueryRelateLocalIds(platCode, idType, entityId, tpShopId, tpIdList);
            result.setModel(list);
        } catch (Exception e) {
            logger.error("[ItemMapping] fail to get itemMapping. entityId: {}, idType: {}, localId: {}", entityId, idType, tpIdList, e);
            result.setSuccess(false);
        }
        return result;
    }

    public Result<ItemMapping> getTpId(String platCode, Byte idType, String entityId, String localId, String commonId, String tpShopId) {
	    Result<ItemMapping> result = new ResultSupport<>();
	    try {
            ItemMapping itemMapping = itemMappingService.getTpId(platCode, idType, entityId, localId, commonId, tpShopId);
            result.setModel(itemMapping);
        } catch (Exception e) {
            logger.error("[ItemMapping] fail to get itemMapping. entityId: {}, idType: {}, localId: {}", entityId, idType, localId, e);
            result.setSuccess(false);
        }
        return result;
    }

    public Result<ItemMapping> getTpId(String platCode, Byte idType, String entityId, String localId, String tpShopId) {
        Result<ItemMapping> result = new ResultSupport<>();
        try {
            ItemMapping itemMapping = itemMappingService.getTpId(platCode, idType, entityId, localId, tpShopId);
            result.setModel(itemMapping);
        } catch (Exception e) {
            logger.error("[ItemMapping] fail to get itemMapping. entityId: {}, idType: {}, localId: {}", entityId, idType, localId, e);
            result.setSuccess(false);
        }
        return result;
    }

    public Result<ItemMapping> getLocalId(String platCode, Byte idType, String entityId, String tpId, String tpShopId) {
		Result<ItemMapping> result = new ResultSupport<>();
		try {
			ItemMapping itemMapping = itemMappingService.getLocalId(platCode, idType, entityId, tpId, tpShopId);
			result.setModel(itemMapping);
		} catch (Exception e) {
			logger.error("[ItemMapping] fail to get itemMapping. entityId: {}, idType: {}, localId: {}", entityId, idType, tpId, e);
			result.setSuccess(false);
		}
		return result;
	}

    public Result<Map<String, String>> batchQueryRelateTpIds(String platCode, Byte idType, String entityId, String shopId, Set<String> localIdSet) {
        Result<Map<String, String>> result = new ResultSupport<>();
        try {
            List<ItemMapping> list = itemMappingService.batchQueryRelateTpIds(platCode, idType, entityId,shopId, localIdSet);
            Map<String, String> map = list.stream().collect(Collectors.toMap(ItemMapping::getLocalId, ItemMapping::getTpId));
            if (map == null) {
                result.setModel(new HashMap<>());
            }
            result.setModel(map);
            return result;
        } catch (Exception e) {
            logger.error("[ItemMapping] fail to get itemMapping. entityId: {}, idType: {}, localIdSet: {}", entityId, idType, JSON.toJSONString(localIdSet), e);
            result.setSuccess(false);
        }
        return result;
    }


}
