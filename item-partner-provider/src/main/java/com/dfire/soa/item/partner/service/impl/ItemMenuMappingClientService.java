package com.dfire.soa.item.partner.service.impl;

import com.alibaba.fastjson.JSON;
import com.dfire.soa.item.partner.bo.ItemMenuMapping;
import com.dfire.soa.item.partner.bo.query.ItemMenuMappingQuery;
import com.dfire.soa.item.partner.service.IItemMenuMappingClientService;
import com.dfire.soa.item.partner.service.internal.IItemMenuMappingService;
import com.twodfire.exception.BizException;
import com.twodfire.share.result.Result;
import com.twodfire.share.result.ResultSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static com.dfire.soa.item.partner.enums.EnumErrorType.SYSTEM_BUSINESS_ERROR;
import static com.dfire.soa.item.partner.enums.EnumErrorType.SYSTEM_DEFAULT_ERROR;

@Service
public class ItemMenuMappingClientService implements IItemMenuMappingClientService {
	
	private static final Logger logger = LoggerFactory.getLogger(ItemMenuMappingClientService.class);
	
	@Resource
	private IItemMenuMappingService itemMenuMappingService;
	
	/**
	 * 保存
	 */
	@Override
	public Result<ItemMenuMapping> saveOrUpdateItemMenuMapping(ItemMenuMapping itemMenuMapping){
		Result<ItemMenuMapping> result = new ResultSupport<>();
		try{
			if(itemMenuMapping.getId()==null){
				itemMenuMappingService.saveItemMenuMapping(itemMenuMapping);
			}else {
				itemMenuMappingService.updateItemMenuMapping(itemMenuMapping);
			}
			result.setModel(itemMenuMapping);
			return result;
		}catch(BizException e){
			logger.error("[ItemMenuMapping]saveItemMenuMapping() failed. itemMenuMapping:{}.", JSON.toJSONString(itemMenuMapping), e);
			return new ResultSupport<>(false, SYSTEM_BUSINESS_ERROR.getErrorCode(), SYSTEM_BUSINESS_ERROR.getErrorMsg() +":"+ e.getMessage());
		}catch(Exception e){
			logger.error("[ItemMenuMapping]saveItemMenuMapping() failed. itemMenuMapping:{}.", JSON.toJSONString(itemMenuMapping), e);
			return new ResultSupport<>(false, SYSTEM_DEFAULT_ERROR.getErrorCode(), SYSTEM_DEFAULT_ERROR.getErrorMsg());
		}
	}

	/**
	 * 逻辑删除  [这个方法没有被用到]
	 */
	@Override
	public Result<Integer> deleteItemMenuMappingById(String entityId, Long id){
		Result<Integer> result = new ResultSupport<>();
		try{
			result.setModel(itemMenuMappingService.deleteItemMenuMappingById(entityId, id));
			return result;
		}catch(BizException e){
			logger.error("[ItemMenuMapping]deleteItemMenuMapping() failed. entityId:{}, id:{}.",JSON.toJSONString(entityId), JSON.toJSONString(id), e);
			return new ResultSupport<>(false, SYSTEM_BUSINESS_ERROR.getErrorCode(), SYSTEM_BUSINESS_ERROR.getErrorMsg() +":"+ e.getMessage());
		}catch(Exception e){
			logger.error("[ItemMenuMapping]deleteItemMenuMapping() failed. entityId:{}, id:{}.",JSON.toJSONString(entityId), JSON.toJSONString(id), e);
			return new ResultSupport<>(false, SYSTEM_DEFAULT_ERROR.getErrorCode(), SYSTEM_DEFAULT_ERROR.getErrorMsg());
		}
	}
	
	/**
	 * 根据id查询 [这个方法没有被使用]
	 */
	@Override
	public Result<ItemMenuMapping> getItemMenuMappingById(String entityId, Long id){
		Result<ItemMenuMapping> result = new ResultSupport<>();
		try{
			result.setModel(itemMenuMappingService.getItemMenuMappingById(entityId, id));
			return result;
		}catch(BizException e){
			logger.error("[ItemMenuMapping]getItemMenuMappingById() failed. entityId:{}, id:{}.",JSON.toJSONString(entityId), JSON.toJSONString(id), e);
			return new ResultSupport<>(false, SYSTEM_BUSINESS_ERROR.getErrorCode(), SYSTEM_BUSINESS_ERROR.getErrorMsg() +":"+ e.getMessage());
		}catch(Exception e){
			logger.error("[ItemMenuMapping]getItemMenuMappingById() failed. entityId:{}, id:{}.",JSON.toJSONString(entityId), JSON.toJSONString(id), e);
			return new ResultSupport<>(false, SYSTEM_DEFAULT_ERROR.getErrorCode(), SYSTEM_DEFAULT_ERROR.getErrorMsg());
		}
	}
	
	/**
	 * 根据query查询
	 */
	@Override
	public Result<List<ItemMenuMapping>> getItemMenuMappingListByQuery(ItemMenuMappingQuery query){
		Result<List<ItemMenuMapping>> result = new ResultSupport<>();
		try{
			result.setModel(itemMenuMappingService.getItemMenuMappingListByQuery(query));
			return result;
		}catch(BizException e){
			logger.error("[ItemMenuMapping]getItemMenuMappingListByQuery() failed. query:{}.", JSON.toJSONString(query), e);
			return new ResultSupport<>(false, SYSTEM_BUSINESS_ERROR.getErrorCode(), SYSTEM_BUSINESS_ERROR.getErrorMsg() +":"+ e.getMessage());
		}catch(Exception e){
			logger.error("[ItemMenuMapping]getItemMenuMappingListByQuery() failed. query:{}.", JSON.toJSONString(query), e);
			return new ResultSupport<>(false, SYSTEM_DEFAULT_ERROR.getErrorCode(), SYSTEM_DEFAULT_ERROR.getErrorMsg());
		}
	}

}
