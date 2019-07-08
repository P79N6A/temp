package com.dfire.soa.item.partner.dao.impl;


import com.dfire.soa.flame.UniqueIdGenerator;
import com.dfire.soa.item.partner.bo.PartnerMapping;
import com.dfire.soa.item.partner.bo.query.PartnerMappingQuery;
import com.dfire.soa.item.partner.dao.IPartnerMappingDao;
import com.dfire.soa.item.partner.mapper.PartnerMappingMapper;
import com.twodfire.share.result.Result;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class PartnerMappingDao implements IPartnerMappingDao {
	
	@Resource
	private PartnerMappingMapper partnerMappingMapper;
	@Resource
	private UniqueIdGenerator uniqueIdGenerator;
	
	/**
	 * 保存
	 */
	 @Override
	public int savePartnerMapping(PartnerMapping partnerMapping){
		 if (partnerMapping.getId() == null) {
			 Result<Long> idResult = uniqueIdGenerator.nextId();
			 partnerMapping.setId(idResult.getModel());
		 }
		return partnerMappingMapper.insert(partnerMapping);
	}
	
	/**
	 * 更新
	 */
	 @Override
	public int updatePartnerMapping(PartnerMapping partnerMapping){
		return partnerMappingMapper.update(partnerMapping);
	}
	
	/**
	 * 逻辑删除
	 */
	 @Override
	public int deletePartnerMappingById(String entityId, Long id){
		return partnerMappingMapper.delete(entityId, id);
	}

	/**
	 * 根据id查询
	 */
	 @Override
	public PartnerMapping getPartnerMappingById(String entityId, Long id){
		return partnerMappingMapper.getById(entityId, id);
	}
	
	/**
	 * 根据query查询
	 */
	 @Override
	public List<PartnerMapping> getPartnerMappingListByQuery(PartnerMappingQuery query){
		return partnerMappingMapper.getListByQuery(query);
	}
}
