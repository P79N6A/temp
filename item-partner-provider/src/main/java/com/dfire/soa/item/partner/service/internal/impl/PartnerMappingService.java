package com.dfire.soa.item.partner.service.internal.impl;

import com.dfire.soa.item.partner.bo.PartnerMapping;
import com.dfire.soa.item.partner.bo.query.PartnerMappingQuery;
import com.dfire.soa.item.partner.dao.IPartnerMappingDao;
import com.dfire.soa.item.partner.service.IItemCacheService;
import com.dfire.soa.item.partner.service.internal.IPartnerMappingService;
import com.twodfire.exception.BizException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class PartnerMappingService implements IPartnerMappingService {

	@Resource
	private IPartnerMappingDao partnerMappingDao;
	@Resource
	private IItemCacheService itemCacheService;

	/**
	 * 保存(已处理缓存)
	 */
	@Override
	public int savePartnerMapping(PartnerMapping partnerMapping){
		return partnerMappingDao.savePartnerMapping(partnerMapping);
	}

	/**
	 * 更新(已处理缓存)
	 */
	@Override
	public int updatePartnerMapping(PartnerMapping partnerMapping){
		return partnerMappingDao.updatePartnerMapping(partnerMapping);
	}

	/**
	 * 逻辑删除(已处理缓存)
	 */
	@Override
	public int deletePartnerMappingById(String entityId, Long id){
		return partnerMappingDao.deletePartnerMappingById(entityId, id);
	}

	/**
	 * 根据id查询
	 */
	@Override
	public PartnerMapping getPartnerMappingById(String entityId, Long id){
		return partnerMappingDao.getPartnerMappingById(entityId, id);
	}

	/**
	 * 根据query查询(已处理缓存)
	 */
	@Override
	public List<PartnerMapping> getPartnerMappingListByQuery(PartnerMappingQuery query){
		String entityId = query.getEntityId();
		String shopId = query.getShopId();
		String localId = query.getLocalId();
		String outId = query.getOutId();
		String mpType = query.getMpType();

		if(StringUtils.isBlank(entityId)){
			throw new BizException("entityId不能为空！");
		}
		return partnerMappingDao.getPartnerMappingListByQuery(query);
	}
}
