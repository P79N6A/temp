package com.dfire.soa.item.partner.service.internal.impl;


import com.dfire.soa.item.partner.bo.ItemMenuMapping;
import com.dfire.soa.item.partner.bo.query.ItemMenuMappingQuery;
import com.dfire.soa.item.partner.constant.CacheConstants;
import com.dfire.soa.item.partner.constant.CommonConstant;
import com.dfire.soa.item.partner.dao.IItemMenuMappingDao;
import com.dfire.soa.item.partner.service.IItemCacheService;
import com.dfire.soa.item.partner.service.internal.IItemMenuMappingService;
import com.dfire.soa.item.partner.util.CacheKeyGenerator;
import com.twodfire.exception.BizException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class ItemMenuMappingService implements IItemMenuMappingService {
	
	@Resource
	private IItemMenuMappingDao itemMenuMappingDao;
	@Resource
	private IItemCacheService itemCacheService;

	/**
	 * 日志：业务
	 */
	private static Logger bizLogger = LoggerFactory.getLogger(CommonConstant.BIZ_LOG);
	
	/**
	 * 保存 [这里不加缓存]
	 */
	@Override
	public int saveItemMenuMapping(ItemMenuMapping itemMenuMapping){
        String key = CacheKeyGenerator.generateItemMenuMappingKey(itemMenuMapping);
        bizLogger.info("[kb_databack] key: {}", key);
        itemCacheService.clearCache(key);
        return itemMenuMappingDao.saveItemMenuMapping(itemMenuMapping);
	}
	
	/**
	 * 更新
	 */
	@Override
	public int updateItemMenuMapping(ItemMenuMapping itemMenuMapping){
        String key = CacheKeyGenerator.generateItemMenuMappingKey(itemMenuMapping);
        bizLogger.info("[kb_databack] key: {}", key);
        itemCacheService.clearCache(key);
		return itemMenuMappingDao.updateItemMenuMapping(itemMenuMapping);
	}
	
	/**
	 * 逻辑删除
	 */
	@Override
	public int deleteItemMenuMappingById(String entityId, Long id){
        ItemMenuMapping itemMenuMapping = itemMenuMappingDao.getItemMenuMappingById(entityId, id);
        String key = CacheKeyGenerator.generateItemMenuMappingKey(itemMenuMapping);
        itemCacheService.clearCache(key);
		return itemMenuMappingDao.deleteItemMenuMappingById(entityId, id);
	}



	@Override
	public int batchDeleteByEntityId(String entityId, String tpShopId, String platCode) {
		this.clearItemMenuMappingCache(entityId);
		return itemMenuMappingDao.batchDeleteByEntityId(entityId, tpShopId, platCode);
	}

	/**
	 * 根据id查询
	 */
	@Override
	public ItemMenuMapping getItemMenuMappingById(String entityId, Long id){
		return itemMenuMappingDao.getItemMenuMappingById(entityId, id);
	}
	
	/**
	 * 根据query查询
	 */
	@Override
	public List<ItemMenuMapping> getItemMenuMappingListByQuery(ItemMenuMappingQuery query){
		if(StringUtils.isBlank(query.getEntityId())){
			throw new BizException("entityId不能为空！");
		}
		//校验list字段
		if((query.getIdList()!=null && query.getIdList().size()==0)
				|| (query.getLocalItemIdList()!=null && query.getLocalItemIdList().size()==0)
				|| (query.getTpItemIdList()!=null && query.getTpItemIdList().size()==0)
				|| (query.getLocalMenuIdList()!=null && query.getLocalMenuIdList().size()==0)
				|| (query.getTpMenuIdList()!=null && query.getTpMenuIdList().size()==0)){
			return new ArrayList<>();
		}
		return itemMenuMappingDao.getItemMenuMappingListByQuery(query);
	}

	@Override
	public ItemMenuMapping getItemMenuMappingByLocalId(String entityId, String tpShopId, String platCode, String localMenuId, String localItemId) {

        String key = CacheConstants.Namespace.ITEM_MENU_MAPPING + entityId + tpShopId + platCode + localMenuId + localItemId;
        bizLogger.info("[kb_databack] key: {}", key);
        ItemMenuMapping itemMenuMapping = (ItemMenuMapping) itemCacheService.getCache(key);
        if(itemMenuMapping != null) {
            return itemMenuMapping;
        }
		ItemMenuMappingQuery query = new ItemMenuMappingQuery();
		query.setEntityId(entityId);
		query.setTpShopId(tpShopId);
		query.setPlatCode(platCode);
		query.setLocalMenuId(localMenuId);
		query.setLocalItemId(localItemId);
		List<ItemMenuMapping> list = itemMenuMappingDao.getItemMenuMappingListByQuery(query);
		if(list.size() == 0) {
			return null;
		}
        itemMenuMapping = list.get(0);
		itemCacheService.putCache(key, itemMenuMapping, CacheConstants.ITEM_CACHE_BY_HALF_AN_HOUR);
		return itemMenuMapping;
	}

	/**
	 * 根据query计数
	 */
	@Override
	public int countByQuery(ItemMenuMappingQuery query){
		if(StringUtils.isBlank(query.getEntityId())){
			throw new BizException("entityId不能为空！");
		}
		//校验list字段
		if((query.getIdList()!=null && query.getIdList().size()==0)
				|| (query.getLocalItemIdList()!=null && query.getLocalItemIdList().size()==0)
				|| (query.getTpItemIdList()!=null && query.getTpItemIdList().size()==0)
				|| (query.getLocalMenuIdList()!=null && query.getLocalMenuIdList().size()==0)
				|| (query.getTpMenuIdList()!=null && query.getTpMenuIdList().size()==0)){
			return 0;
		}
		return itemMenuMappingDao.countByQuery(query);
	}

	/**
	 * 清空店铺的 itemMenuMapping的缓存
	 * @param entityId
	 */
	private void clearItemMenuMappingCache(String entityId) {
		int pageIndex = 1;
		int pageSize = 500;
		List<ItemMenuMapping> list;
		List<String> keys = new ArrayList<>();
		ItemMenuMappingQuery query = new ItemMenuMappingQuery();
		query.setPlatCode(String.valueOf(CommonConstant.KOUBEI_PLATFORM));
		query.setEntityId(entityId);
		query.setPageSize(pageSize);
		do {
			query.setPageIndex(pageIndex);
			list = itemMenuMappingDao.getItemMenuMappingListByQuery(query);
			if(CollectionUtils.isNotEmpty(list)) {
				for(ItemMenuMapping itemMenuMapping : list) {
					String key = CacheKeyGenerator.generateItemMenuMappingKey(itemMenuMapping);
					keys.add(key);
				}
			}
			pageIndex ++;
		} while (list.size() > 0);

		String[] keyArr = new String[keys.size()];
		keys.toArray(keyArr);
		itemCacheService.clearCache(keyArr);
	}
}
