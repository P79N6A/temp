package com.dfire.soa.item.partner.manager.impl;

import com.dfire.soa.item.partner.bo.CookBO;
import com.dfire.soa.item.partner.bo.query.CookQuery;
import com.dfire.soa.item.partner.dao.ICookDAO;
import com.dfire.soa.item.partner.domain.CookDO;
import com.dfire.soa.item.partner.manager.ICookManager;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: xiaoji
 * @Date: create on 2018/8/27
 * @Describle:
 */
@Component("cookManager")
public class CookManagerImpl implements ICookManager {

	@Resource
	private ICookDAO cookDAO;

	@Override
	public Boolean insert(CookDO cookDO) {
		if (cookDAO.insert(cookDO)) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	@Override
	public Boolean updateById(CookDO cookDO) {
		if (cookDAO.updateById(cookDO)) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	@Override
	public Boolean deleteById(String entityId, Long id) {
		if (cookDAO.deleteById(entityId, id)) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	@Override
	public CookDO queryById(String entityId, Long id) {
		return cookDAO.queryById(entityId, id);
	}

	@Override
	public Integer batchInsert(List<CookDO> cookDOList) {
		return cookDAO.batchInsert(cookDOList);
	}

	@Override
	public List<CookDO> selectByEntityId(String entityId) {
		return cookDAO.selectByEntityId(entityId);
	}

	@Override
	public CookDO selectByType(String entityId, int type) {
		return cookDAO.selectByType(entityId, type);
	}

	@Override
	public Integer batchDelete(Long time) {
		return cookDAO.batchDelete(time);
	}

	@Override
	public List<String> getEntityIdList(long startTime, long endTime) {
		return cookDAO.getEntityIdList(startTime, endTime);
	}

	@Override
	public void updateStatus() {
		cookDAO.updateStatus();

	}
}
