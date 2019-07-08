package com.dfire.soa.item.partner.dao;

import com.dfire.soa.item.partner.bo.PartnerMapping;
import com.dfire.soa.item.partner.bo.query.PartnerMappingQuery;

import java.util.List;

public interface IPartnerMappingDao {
	
	/**
	 * 保存
	 */
	int savePartnerMapping(PartnerMapping partnerMapping);
	
	/**
	 * 更新
	 */
	int updatePartnerMapping(PartnerMapping partnerMapping);
	
	/**
	 * 逻辑删除
	 */
	int deletePartnerMappingById(String entityId, Long id);

	/**
	 * 根据id查询
	 */
	PartnerMapping getPartnerMappingById(String entityId, Long id);
	
	/**
	 * 根据query查询
	 */
	List<PartnerMapping> getPartnerMappingListByQuery(PartnerMappingQuery query);
}
