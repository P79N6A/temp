package com.dfire.soa.item.partner.service.impl;

import com.alibaba.fastjson.JSON;
import com.dfire.soa.item.partner.bo.PartnerMapping;
import com.dfire.soa.item.partner.bo.query.PartnerMappingQuery;
import com.dfire.soa.item.partner.service.IPartnerMappingClientService;
import com.dfire.soa.item.partner.service.internal.IPartnerMappingService;
import com.twodfire.exception.BizException;
import com.twodfire.share.result.Result;
import com.twodfire.share.result.ResultSupport;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static com.dfire.soa.item.partner.enums.EnumErrorType.SYSTEM_BUSINESS_ERROR;
import static com.dfire.soa.item.partner.enums.EnumErrorType.SYSTEM_DEFAULT_ERROR;

@Service
public class PartnerMappingClientService implements IPartnerMappingClientService {
	
	private static final Logger logger = LoggerFactory.getLogger(PartnerMappingClientService.class);
	
	@Resource
	private IPartnerMappingService partnerMappingService;
	
	/**
	 * 保存
	 */
	@Override
	public Result<PartnerMapping> saveOrUpdatePartnerMapping(PartnerMapping partnerMapping){
		Result<PartnerMapping> result = new ResultSupport<>();
		int count;
		try{
			if(partnerMapping.getId()==null){
				count = partnerMappingService.savePartnerMapping(partnerMapping);
			}else {
				count = partnerMappingService.updatePartnerMapping(partnerMapping);
			}
			if(count < 1) {
				result.setSuccess(false);
			}
			result.setModel(partnerMapping);
			return result;
		}catch(BizException e){
			logger.error("[PartnerMapping]savePartnerMapping() failed. partnerMapping:{}.",JSON.toJSONString(partnerMapping), e);
			return new ResultSupport<>(false, SYSTEM_BUSINESS_ERROR.getErrorCode(), SYSTEM_BUSINESS_ERROR.getErrorMsg() +":"+ e.getMessage());
		}catch(Exception e) {
			logger.error("[PartnerMapping]savePartnerMapping() failed. partnerMapping:{}.",JSON.toJSONString(partnerMapping), e);
			return new ResultSupport<>(false, SYSTEM_DEFAULT_ERROR.getErrorCode(), SYSTEM_DEFAULT_ERROR.getErrorMsg());
		}
	}

	/**
	 * 逻辑删除
	 */
	@Override
	public Result<Integer> deletePartnerMappingById(String entityId, Long id){
		Result<Integer> result = new ResultSupport<>();
		try{
			result.setModel(partnerMappingService.deletePartnerMappingById(entityId, id));
			return result;
		}catch(BizException e){
			logger.error("[PartnerMapping]deletePartnerMapping() failed. entityId:{}, id:{}.", JSON.toJSONString(entityId), JSON.toJSONString(id), e);
			return new ResultSupport<>(false, SYSTEM_BUSINESS_ERROR.getErrorCode(), SYSTEM_BUSINESS_ERROR.getErrorMsg() +":"+ e.getMessage());
		}catch(Exception e){
			logger.error("[PartnerMapping]deletePartnerMapping() failed. entityId:{}, id:{}.", JSON.toJSONString(entityId), JSON.toJSONString(id), e);
			return new ResultSupport<>(false, SYSTEM_DEFAULT_ERROR.getErrorCode(), SYSTEM_DEFAULT_ERROR.getErrorMsg());
		}
	}
	
	/**
	 * 根据id查询
	 */
	@Override
	public Result<PartnerMapping> getPartnerMappingById(String entityId, Long id){
		Result<PartnerMapping> result = new ResultSupport<>();
		try{
			result.setModel(partnerMappingService.getPartnerMappingById(entityId, id));
			return result;
		}catch(BizException e){
			logger.error("[PartnerMapping]getPartnerMappingById() failed. entityId:{}, id:{}.", JSON.toJSONString(entityId), JSON.toJSONString(id), e);
			return new ResultSupport<>(false, SYSTEM_BUSINESS_ERROR.getErrorCode(), SYSTEM_BUSINESS_ERROR.getErrorMsg() +":"+ e.getMessage());
		}catch(Exception e){
			logger.error("[PartnerMapping]getPartnerMappingById() failed. entityId:{}, id:{}.", JSON.toJSONString(entityId), JSON.toJSONString(id), e);
			return new ResultSupport<>(false, SYSTEM_DEFAULT_ERROR.getErrorCode(), SYSTEM_DEFAULT_ERROR.getErrorMsg());
		}
	}
	
	/**
	 * 根据query查询
	 */
	@Override
	public Result<List<PartnerMapping>> getPartnerMappingListByQuery(PartnerMappingQuery query){
		Result<List<PartnerMapping>> result = new ResultSupport<>();
		try{

			List<PartnerMapping> partnerMappingList = partnerMappingService.getPartnerMappingListByQuery(query);
			if(CollectionUtils.isNotEmpty(partnerMappingList)){
				result.setModel(partnerMappingList);
			}
			return result;
		}catch(BizException e){
			logger.error("[PartnerMapping]getPartnerMappingListByQuery() failed. query:{}.", JSON.toJSONString(query), e);
			return new ResultSupport<>(false, SYSTEM_BUSINESS_ERROR.getErrorCode(), SYSTEM_BUSINESS_ERROR.getErrorMsg() +":"+ e.getMessage());
		}catch(Exception e){
			logger.error("[PartnerMapping]getPartnerMappingListByQuery() failed. query:{}.", JSON.toJSONString(query), e);
			return new ResultSupport<>(false, SYSTEM_DEFAULT_ERROR.getErrorCode(), SYSTEM_DEFAULT_ERROR.getErrorMsg());
		}
	}
}
