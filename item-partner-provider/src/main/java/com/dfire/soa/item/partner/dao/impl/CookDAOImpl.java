package com.dfire.soa.item.partner.dao.impl;

import com.dfire.soa.flame.UniqueIdGenerator;
import com.dfire.soa.item.partner.bo.query.CookQuery;
import com.dfire.soa.item.partner.constants.CommonConstants;
import com.dfire.soa.item.partner.dao.ICookDAO;
import com.dfire.soa.item.partner.domain.CookDO;
import com.dfire.soa.item.partner.mapper.CookMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: xiaoji
 * @Date: create on 2018/8/27
 * @Describle:
 */
@Repository("cookDAO")
public class CookDAOImpl implements ICookDAO {

	@Resource
	private CookMapper cookMapper;

	@Resource
	private UniqueIdGenerator uniqueIdGenerator;

	@Override
	public Boolean insert(CookDO cookDO) {
		if (null == cookDO) {
			return Boolean.FALSE;
		}
		if (null == cookDO.getId()) {
			cookDO.setId(uniqueIdGenerator.nextId().getModel());
		}
		cookDO.setIsValid(CommonConstants.IsValid.VALID);
		return cookMapper.insert(cookDO) > 0;
	}

	@Override
	public Boolean updateById(CookDO cookDO) {
		if (null == cookDO || StringUtils.isEmpty(cookDO.getEntityId()) || null == cookDO.getId()) {
			return Boolean.FALSE;
		}
		return cookMapper.updateById(cookDO) > 0;
	}

	@Override
	public Boolean deleteById(String entityId, Long id) {
		if (StringUtils.isEmpty(entityId) || null == id) {
			return Boolean.FALSE;
		}
		return cookMapper.deleteById(entityId, id) > 0;
	}

	@Override
	public CookDO queryById(String entityId, Long id) {
		if (StringUtils.isEmpty(entityId) || null == id) {
			return null;
		}
		return cookMapper.queryById(entityId, id);
	}

	@Override
	public Integer batchInsert(List<CookDO> cookDOList) {
		if (CollectionUtils.isEmpty(cookDOList)) {
			return 0;
		}
		return cookMapper.batchInsert(cookDOList);
	}

	@Override
	public List<CookDO> selectByEntityId(String entityId) {
		return cookMapper.selectByEntityId(entityId);
	}

	@Override
	public List<CookDO> selectByQuery(CookQuery cookQuery) {
		if (null == cookQuery) {
			return null;
		}
		return cookMapper.selectByQuery(cookQuery);
	}

	@Override
	public CookDO selectByType(String entityId, int type) {
		if (StringUtils.isEmpty(entityId) || type == 0) {
			return null;
		}
		return cookMapper.selectByType(entityId, type);
	}

	@Override
	public Integer batchDelete(Long time) {
		return cookMapper.batchDelete(time);
	}

	@Override
	public List<String> getEntityIdList(long startTime, long endTime) {
		return cookMapper.getEntityIdList(startTime, endTime);
	}

	@Override
	public void updateStatus() {
		cookMapper.updateStatus();

	}
}
