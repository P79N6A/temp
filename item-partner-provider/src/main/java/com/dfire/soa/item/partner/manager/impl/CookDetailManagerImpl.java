package com.dfire.soa.item.partner.manager.impl;

import com.alibaba.fastjson.JSONObject;
import com.dfire.soa.item.partner.bo.query.CookDetailQuery;
import com.dfire.soa.item.partner.constant.CacheConstants;
import com.dfire.soa.item.partner.constants.CommonConstants;
import com.dfire.soa.item.partner.dao.ICookDetailDAO;
import com.dfire.soa.item.partner.domain.CookDetailDO;
import com.dfire.soa.item.partner.manager.ICookDetailManager;
import com.dfire.soa.item.partner.service.IItemCacheService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: xiaoji
 * @Date: create on 2018/8/27
 * @Describle:
 */
@Component("cookDetailManager")
public class CookDetailManagerImpl implements ICookDetailManager {
	private final String SELECT_BY_ENTITY_ID_AND_COOK_ID = "selectByEntityIdAndCookId";

	private final String SELECT_BY_ENTITY_ID_AND_COOK_ID_AND_MENU_ID = "selectByEntityIdAndCookIdAndMenuId";

	@Resource
	private IItemCacheService itemCacheService;

	@Resource
	private ICookDetailDAO cookDetailDAO;

	@Override
	public Boolean insert(CookDetailDO cookDetailDO) {
		Boolean flag = cookDetailDAO.insert(cookDetailDO);
		clearCache(cookDetailDO);
		if (flag) {
			return flag;
		}
		return Boolean.FALSE;
	}

	@Override
	public Boolean updateById(CookDetailDO cookDetailDO) {
		Boolean flag = cookDetailDAO.updateById(cookDetailDO);
		clearCache(cookDetailDO);
		if (flag) {
			return flag;
		}
		return Boolean.FALSE;
	}

	@Override
	public Boolean deleteById(String entityId, Long id) {
		CookDetailDO cookDetailDO = cookDetailDAO.queryById(entityId, id);
		if (null == cookDetailDO) {
			return Boolean.FALSE;
		}
		Boolean flag = cookDetailDAO.deleteById(entityId, id);
		clearCache(cookDetailDO);
		if (flag) {
			return flag;
		}
		return Boolean.FALSE;
	}

	@Override
	public Integer batchInsert(List<CookDetailDO> cookDetailDOList) {
		Integer count = cookDetailDAO.batchInsert(cookDetailDOList);
		for (CookDetailDO cookDetailDO : cookDetailDOList) {
			clearCache(cookDetailDO);
		}
		return count;
	}

	@Override
	public Integer batchDelete(Long time) {
		return cookDetailDAO.batchDelete(time);
	}

	@Override
	public List<String> queryMenuIdsByCookId(String entityId, Long cookId) {
		String key = buildNamespace(entityId, SELECT_BY_ENTITY_ID_AND_COOK_ID, String.valueOf(cookId));
		Object obj = itemCacheService.getCache(key);
		if (obj != null) {
			return (List<String>) obj;
		}
		List<String> menuIdList = cookDetailDAO.queryMenuIdsByCookId(entityId, cookId);
		if (!CollectionUtils.isEmpty(menuIdList)) {
			itemCacheService.putCache(key, menuIdList, CacheConstants.ITEM_CACHE_BY_THREE_DAYS);
		}
		return menuIdList;
	}

	@Override
	public Integer batchDeleteByIdList(String entityId, List<Long> idList) {
		List<CookDetailDO> cookDetailDOList = new ArrayList<>();
		for (Long id : idList) {
			CookDetailDO cookDetailDO = queryById(entityId, id);
			if (null != cookDetailDO) {
				cookDetailDOList.add(cookDetailDO);
				clearCache(cookDetailDO);
			}
		}
		Integer count = cookDetailDAO.batchDeleteByIdList(entityId, idList);
		List<String> menuIdList = cookDetailDOList.stream().map(cookDetailDO -> cookDetailDO.getMenuId()).collect(Collectors.toList());
		clearCache(entityId, String.valueOf(cookDetailDOList.get(0).getCookId()), menuIdList);
		return count;
	}

	@Override
	public List<CookDetailDO> selectByQuery(CookDetailQuery cookDetailQuery) {
		return cookDetailDAO.selectByQuery(cookDetailQuery);
	}

	@Override
	public CookDetailDO queryById(String entityId, Long id) {
		return cookDetailDAO.queryById(entityId, id);
	}

	@Override
	public Boolean deleteByMenuId(String entityId, String menuId) {
		CookDetailQuery cookDetailQuery = new CookDetailQuery(entityId);
		cookDetailQuery.setMenuId(menuId);
		cookDetailQuery.setIsValid(CommonConstants.IsValid.VALID);
		cookDetailQuery.setUsePage(true);
		List<CookDetailDO> cookDetailDOList = selectByQuery(cookDetailQuery);
		if (CollectionUtils.isEmpty(cookDetailDOList)) {
			return Boolean.FALSE;
		}
		Boolean flag = cookDetailDAO.deleteByMenuId(entityId, menuId);

		for (CookDetailDO cookDetailDO : cookDetailDOList) {
			clearCache(entityId, String.valueOf(cookDetailDO.getCookId()), Lists.newArrayList(menuId));
		}
		return flag;
	}

	@Override
	public Integer deleteByMenuIdList(String entityId, List<String> menuIdList) {
		CookDetailQuery cookDetailQuery = new CookDetailQuery(entityId);
		cookDetailQuery.setMenuIdList(menuIdList);
		cookDetailQuery.setIsValid(CommonConstants.IsValid.VALID);
		cookDetailQuery.setUsePage(true);
		List<CookDetailDO> cookDetailDOList = selectByQuery(cookDetailQuery);
		if (CollectionUtils.isEmpty(cookDetailDOList)) {
			return 0;
		}
		int count = cookDetailDAO.deleteByMenuIdList(entityId, menuIdList);
		for (CookDetailDO cookDetailDO : cookDetailDOList) {
		    clearCache(cookDetailDO);
			clearCache(entityId, String.valueOf(cookDetailDO.getCookId()), menuIdList);
		}
		return count;
	}

	@Override
	public List<String> queryEntityIdWithFewCookDetail() {
		return cookDetailDAO.queryEntityIdWihtFewCookDetail();
	}

	@Override
	public Integer queryCountByMenuIdAndCreateTime(String entityId, Long cookId, String menuId) {
		return cookDetailDAO.queryCountByMenuIdAndCreateTime(entityId, cookId, menuId);
	}

	@Override
	public Integer batchDeleteByMenuIdListAndCreateTime(String entityId, List<String> menuIdList) {
		return cookDetailDAO.batchDeleteByMenuIdListAndCreateTime(entityId, menuIdList);
	}

	private String buildNamespace(String entityId, String methodName, String dataId) {
		StringBuilder namespace = new StringBuilder(CacheConstants.Namespace.COOK_DETAIL_PREFIX);
		namespace.append(entityId).append(CacheConstants.SEPARATOR).append(methodName).append(CacheConstants.SEPARATOR).append(dataId);
		return namespace.toString();
	}

	private void clearCache(CookDetailDO cookDetailDO) {
		if (cookDetailDO == null || StringUtils.isEmpty(cookDetailDO.getEntityId())) {
			return;
		}
		String[] namespces = new String[]{
				buildNamespace(cookDetailDO.getEntityId(), SELECT_BY_ENTITY_ID_AND_COOK_ID, String.valueOf(cookDetailDO.getCookId())),
				buildNamespace(cookDetailDO.getEntityId(), SELECT_BY_ENTITY_ID_AND_COOK_ID_AND_MENU_ID, cookDetailDO.getCookId() + cookDetailDO.getMenuId())
		};
		itemCacheService.clearCache(namespces);
	}

	private void clearCache(String entityId, String cookId, List<String> menuIds) {
		if (StringUtils.isEmpty(entityId) || StringUtils.isEmpty(cookId)) {
			return;
		}
		Set<String> keys = Sets.newHashSet(buildNamespace(entityId, SELECT_BY_ENTITY_ID_AND_COOK_ID_AND_MENU_ID, cookId));
		if (!CollectionUtils.isEmpty(menuIds)) {
			for (String menuId : menuIds) {
				StringBuffer sbKey = new StringBuffer(cookId);
				sbKey.append(menuId);
				keys.add(buildNamespace(entityId, SELECT_BY_ENTITY_ID_AND_COOK_ID_AND_MENU_ID, sbKey.toString()));
			}
		}
		itemCacheService.clearCache(keys.toArray(new String[keys.size()]));
	}
}
