package com.dfire.soa.item.partner.service;

import com.dfire.soa.item.partner.bo.PartnerMapping;
import com.dfire.soa.item.partner.bo.query.PartnerMappingQuery;
import com.twodfire.share.result.Result;

import java.util.List;

public interface IPartnerMappingClientService {
	
	/**
	 * 新增/更新（根据id判断）
	 */
	Result<PartnerMapping> saveOrUpdatePartnerMapping(PartnerMapping partnerMapping);

	/**
	 * 逻辑删除
	 */
	Result<Integer> deletePartnerMappingById(String entityId, Long id);
	
	/**
	 * 根据id查询
	 */
	Result<PartnerMapping> getPartnerMappingById(String entityId, Long id);
	
	/**
	 * 根据query查询
	 */
	Result<List<PartnerMapping>> getPartnerMappingListByQuery(PartnerMappingQuery query);
}
