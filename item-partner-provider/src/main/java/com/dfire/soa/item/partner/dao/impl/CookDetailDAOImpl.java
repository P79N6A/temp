package com.dfire.soa.item.partner.dao.impl;

import com.dfire.soa.flame.UniqueIdGenerator;
import com.dfire.soa.item.partner.bo.query.CookDetailQuery;
import com.dfire.soa.item.partner.constants.CommonConstants;
import com.dfire.soa.item.partner.dao.ICookDetailDAO;
import com.dfire.soa.item.partner.domain.CookDetailDO;
import com.dfire.soa.item.partner.mapper.CookDetailMapper;
import com.twodfire.share.result.Result;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * @Author: xiaoji
 * @Date: create on 2018/8/27
 * @Describle:
 */
@Repository("cookDetailDAO")
public class CookDetailDAOImpl implements ICookDetailDAO {

	@Resource
	private CookDetailMapper cookDetailMapper;

	@Resource
	private UniqueIdGenerator uniqueIdGenerator;

	@Override
	public Boolean insert(CookDetailDO cookDetailDO) {
		if (null == cookDetailDO) {
			return Boolean.FALSE;
		}
		if (null == cookDetailDO.getId()) {
			cookDetailDO.setId(uniqueIdGenerator.nextId().getModel());
		}
		return cookDetailMapper.insert(cookDetailDO) > 0;
	}

	@Override
	public Boolean updateById(CookDetailDO cookDetailDO) {
		if (null == cookDetailDO || StringUtils.isEmpty(cookDetailDO.getEntityId()) || null == cookDetailDO.getId()) {
			return Boolean.TRUE;
		}
		return cookDetailMapper.updateById(cookDetailDO) > 0;
	}

	@Override
	public Boolean deleteById(String entityId, Long id) {
		if (StringUtils.isEmpty(entityId) || null == id) {
			return Boolean.TRUE;
		}
		return cookDetailMapper.deleteById(entityId, id) > 0;
	}

	@Override
	public Integer batchInsert(List<CookDetailDO> cookDetailDOList) {
		if (CollectionUtils.isEmpty(cookDetailDOList)) {
			return 0;
		}
		for (CookDetailDO cookDetailDO : cookDetailDOList) {
			if (null == cookDetailDO.getId()) {
				cookDetailDO.setId(uniqueIdGenerator.nextId().getModel());
			}
			cookDetailDO.setIsValid(CommonConstants.IsValid.VALID);
		}
		return cookDetailMapper.batchInsert(cookDetailDOList);
	}

	@Override
	public Integer batchDelete(Long time) {
		return cookDetailMapper.batchDelete(time);
	}

	@Override
	public List<String> queryMenuIdsByCookId(String entityId, Long cookId) {
		if (StringUtils.isEmpty(entityId) || null == cookId) {
			return Collections.EMPTY_LIST;
		}
		return cookDetailMapper.queryMenuIdsByCookId(entityId, cookId);
	}

	@Override
	public Integer batchDeleteByIdList(String entityId, List<Long> idList) {
		if (StringUtils.isEmpty(entityId)) {
			return 0;
		}
		return cookDetailMapper.batchDeleteByIdList(entityId, idList);
	}

	@Override
	public CookDetailDO queryById(String entityId, Long id) {
		if (StringUtils.isEmpty(entityId) || null == id) {
			return null;
		}
		return cookDetailMapper.queryById(entityId, id);
	}

	@Override
	public CookDetailDO queryByIdWithoutValid(String entityId, Long id) {
		if (StringUtils.isEmpty(entityId) || null == id) {
			return null;
		}
		return cookDetailMapper.queryByIdWithoutValid(entityId, id);
	}

	@Override
	public List<CookDetailDO> selectByQuery(CookDetailQuery cookDetailQuery) {
		if (cookDetailQuery == null) {
			return Collections.EMPTY_LIST;
		}
		return cookDetailMapper.selectByQuery(cookDetailQuery);
	}

	@Override
	public Boolean deleteByMenuId(String entityId, String menuId) {
		if (StringUtils.isEmpty(entityId) || StringUtils.isEmpty(menuId)) {
			return null;
		}
		return cookDetailMapper.deleteByMenuId(entityId, menuId);
	}

	@Override
	public Integer deleteByMenuIdList(String entityId, List<String> menuIdList) {
		if(StringUtils.isEmpty(entityId)||CollectionUtils.isEmpty(menuIdList)){
			return 0;
		}
		return cookDetailMapper.deleteByMenuIdList(entityId, menuIdList);
	}

	@Override
	public List<String> queryEntityIdWihtFewCookDetail() {
        return cookDetailMapper.queryEntityIdWihtFewCookDetail();
	}

	@Override
	public Integer queryCountByMenuIdAndCreateTime(String entityId, Long cookId, String menuId) {
		return cookDetailMapper.queryCountByMenuIdAndCreateTime(entityId, cookId, menuId);
	}

    @Override
    public Integer batchDeleteByMenuIdListAndCreateTime(String entityId, List<String> menuIdList) {
        return cookDetailMapper.batchDeleteByMenuIdListAndCreateTime(entityId,menuIdList);
    }
}
