package com.dfire.soa.item.partner.service.internal.impl;

import com.dfire.soa.item.partner.bo.BrandSyncResultBo;
import com.dfire.soa.item.partner.bo.ItemMapping;
import com.dfire.soa.item.partner.bo.base.BaseItemMapping;
import com.dfire.soa.item.partner.bo.query.CommonIdModel;
import com.dfire.soa.item.partner.bo.query.ItemMappingQuery;
import com.dfire.soa.item.partner.constant.CacheConstants;
import com.dfire.soa.item.partner.constant.CommonConstant;
import com.dfire.soa.item.partner.dao.IItemMappingDao;
import com.dfire.soa.item.partner.service.IItemCacheService;
import com.dfire.soa.item.partner.service.internal.IItemMappingService;
import com.twodfire.exception.BizException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ItemMappingService implements IItemMappingService {

	@Resource
	private IItemMappingDao itemMappingDao;
	@Resource
	private IItemCacheService itemCacheService;

	/**
	 * 保存(已处理缓存)
	 */
	@Override
	public int saveItemMapping(ItemMapping itemMapping){
		int count = itemMappingDao.saveItemMapping(itemMapping);
		if (count>0) {
			//清除缓存
			this.clearCache(itemMapping);
		}
		return count;
	}

	/**
	 * 更新(已处理缓存)
	 */
	@Override
	public int updateItemMapping(ItemMapping itemMapping){
		int count = itemMappingDao.updateItemMapping(itemMapping);
		if(count>0){
			this.clearCache(itemMapping);
		}
		return count;
	}

	/**
	 * 逻辑删除(已处理缓存)
	 */
	@Override
	public int deleteItemMappingById(String entityId, Long id){
		int count = itemMappingDao.deleteItemMappingById(entityId, id);
		if (count>0) {
			//清除缓存
			ItemMapping itemMapping = itemMappingDao.getItemMappingById(entityId, id);
			if(itemMapping!=null){
				this.clearCache(itemMapping);
			}
		}
		return count;
	}

	/**
	 * 根据id查询
	 */
	@Override
	public ItemMapping getItemMappingById(String entityId, Long id){
		return itemMappingDao.getItemMappingById(entityId, id);
	}

	/**
	 * 根据query查询(已处理缓存)
	 */
	@Override
	public List<ItemMapping> getItemMappingListByQuery(ItemMappingQuery query){
		String entityId = query.getEntityId();
		String tpShopId = query.getTpShopId();
		String platCode = query.getPlatCode();
		Integer idType = query.getIdType();
		String localId = query.getLocalId();
		String commonId = query.getCommonId();
		String tpId = query.getTpId();

		if(StringUtils.isBlank(entityId)){
			throw new BizException("entityId不能为空！");
		}
		//校验list字段
		if((query.getIdList()!=null && query.getIdList().size()==0)
				|| (query.getIdTypeList()!=null && query.getIdTypeList().size()==0)
				|| (query.getLocalIdList()!=null && query.getLocalIdList().size()==0)
				|| (query.getCommonIdList()!=null && query.getCommonIdList().size()==0)
				|| (query.getTpIdList()!=null && query.getTpIdList().size()==0)){
			return new ArrayList<>();
		}

		List<ItemMapping> itemMappings = new ArrayList<>();
		if(this.getQueryType(query) == 1){
			//获取缓存
			ItemMapping cacheItemMapping = this.getCache(entityId, tpShopId, platCode, idType, localId, commonId, tpId);
			if(cacheItemMapping!=null){
				itemMappings.add(cacheItemMapping.getId()==null ? null : cacheItemMapping);
				return itemMappings;
			}

			//查询并加入缓存
			itemMappings = itemMappingDao.getItemMappingListByQuery(query);
			if(CollectionUtils.isEmpty(itemMappings)){//null处理
				List<ItemMapping> cacheList = new ArrayList<>();
				ItemMapping nullItemMapping = new ItemMapping(entityId, tpShopId, platCode, idType, localId, tpId);
				nullItemMapping.setCommonId(commonId);
				cacheList.add(nullItemMapping);
				this.putCache(cacheList);
			}else {
				this.putCache(itemMappings);
			}
			return itemMappings;
		}
		return itemMappingDao.getItemMappingListByQuery(query);
	}

	@Override
	public List<ItemMapping> getItemMappingListByLocalIds(String platCode, byte idType, String entityId, String tpShopId, List<String> localIdsParam) {
		List<String> localIds = new ArrayList<>(localIdsParam);
		if (idType==8){
			throw new BizException("idType不应为8");
		}
		List<ItemMapping> itemMappings = new ArrayList<>();

		//从缓存获取
		List<ItemMapping> cacheItemMappings = this.getCache(entityId, tpShopId, platCode, (int)idType, localIds, null, null);
		cacheItemMappings.forEach(itemMapping -> {
			if(itemMapping.getId()!=null) {
				itemMappings.add(itemMapping);
			}
			localIds.removeIf(s -> s.equals(itemMapping.getLocalId()));
		});

		//查询没缓存的数据
		if(CollectionUtils.isNotEmpty(localIds)) {
			ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, tpShopId, platCode, (int) idType, null, null);
			itemMappingQuery.setLocalIdList(localIds);
			itemMappingQuery.setPageSize(ItemMappingQuery.MAX_PAGE_SIZE);
			List<ItemMapping> dbItemMappings = itemMappingDao.getItemMappingListByQuery(itemMappingQuery);
			itemMappings.addAll(dbItemMappings);
			dbItemMappings.forEach(itemMapping -> {
				localIds.removeIf(s -> s.equals(itemMapping.getLocalId()));
			});

			//缓存数据
			localIds.forEach(localId -> dbItemMappings.add(new ItemMapping(entityId, tpShopId, platCode, (int)idType, localId, null)));
			this.putCache(dbItemMappings);
		}

		return itemMappings;
	}

	@Override
	public List<ItemMapping> getItemMappingListByCommonIdModels(String platCode, byte idType, String entityId, String tpShopId, List<CommonIdModel> commonIdModelsParam) {
		List<CommonIdModel> commonIdModels = new ArrayList<>(commonIdModelsParam);
		if (idType!=8){
			throw new BizException("idType应为8");
		}
		List<ItemMapping> itemMappings = new ArrayList<>();
		List<String> localIds = new ArrayList<>();
		List<String> commonIds = new ArrayList<>();
		for (CommonIdModel commonIdModel : commonIdModels){
			if(StringUtils.isNotBlank(commonIdModel.getLocalId()) && StringUtils.isNotBlank(commonIdModel.getCommonId())){
				localIds.add(commonIdModel.getLocalId());
				commonIds.add(commonIdModel.getCommonId());
			}
		}

		//从缓存获取
		List<ItemMapping> cacheItemMappings = this.getCache(entityId, tpShopId, platCode, (int)idType, localIds, commonIds, null);
		cacheItemMappings.forEach(itemMapping -> {
			if(itemMapping.getId()!=null) {
				itemMappings.add(itemMapping);
			}
			commonIdModels.removeIf(commonIdModel -> commonIdModel.getLocalId().equals(itemMapping.getLocalId()) && commonIdModel.getCommonId().equals(itemMapping.getCommonId()));
		});

		//查询没有缓存的数据
		if(CollectionUtils.isNotEmpty(commonIdModels)) {
			List<ItemMapping> putCacheItemMappings = new ArrayList<>();
			for (CommonIdModel commonIdModel : commonIdModels){
				//查询
				ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, tpShopId, platCode, (int) idType, commonIdModel.getLocalId(), null);
				itemMappingQuery.setCommonId(commonIdModel.getCommonId());
				List<ItemMapping> dbItemMappings = itemMappingDao.getItemMappingListByQuery(itemMappingQuery);
				itemMappings.addAll(dbItemMappings);

				//准备缓存数据
				if(CollectionUtils.isEmpty(dbItemMappings)){
					ItemMapping nullItemMapping = new ItemMapping(entityId, tpShopId, platCode, (int)idType, commonIdModel.getLocalId(), null);
					nullItemMapping.setCommonId(commonIdModel.getCommonId());
					putCacheItemMappings.add(nullItemMapping);
				}else {
					putCacheItemMappings.addAll(dbItemMappings);
				}
			}
			//缓存数据
			this.putCache(putCacheItemMappings);
		}
		return itemMappings;
	}

	@Override
	public List<ItemMapping> getItemMappingListByTpIds(String platCode, byte idType, String entityId, String tpShopId, List<String> tpIdsParam) {
		List<String> tpIds = new ArrayList<>(tpIdsParam);
		List<ItemMapping> itemMappings = new ArrayList<>();

		//从缓存获取
		List<ItemMapping> cacheItemMappings = this.getCache(entityId, tpShopId, platCode, (int)idType, null, null, tpIds);
		cacheItemMappings.forEach(itemMapping -> {
			if(itemMapping.getId()!=null) {
				itemMappings.add(itemMapping);
			}
			tpIds.removeIf(s -> s.equals(itemMapping.getTpId()));
		});

		//查询没缓存的数据
		if(CollectionUtils.isNotEmpty(tpIds)) {
			ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, tpShopId, platCode, (int) idType, null, null);
			itemMappingQuery.setTpIdList(tpIds);
			itemMappingQuery.setPageSize(ItemMappingQuery.MAX_PAGE_SIZE);
			List<ItemMapping> dbItemMappings = itemMappingDao.getItemMappingListByQuery(itemMappingQuery);
			itemMappings.addAll(dbItemMappings);
			dbItemMappings.forEach(itemMapping -> {
				tpIds.removeIf(s -> s.equals(itemMapping.getTpId()));
			});

			//缓存数据
			tpIds.forEach(tpId -> dbItemMappings.add(new ItemMapping(entityId, tpShopId, platCode, (int)idType, null, tpId)));
			this.putCache(dbItemMappings);
		}

		return itemMappings;
	}

	/**
	 * 根据localId查tpId
	 */
	@Override
	public ItemMapping getTpId(String platCode, Byte idType, String entityId, String localId, String tpShopId) {
		return this.getTpId(platCode, idType, entityId, localId, null, tpShopId);
	}

	/**
	 * 根据localId查tpId（使用commonId 作为条件查询）
	 */
	@Override
	public ItemMapping getTpId(String platCode, Byte idType, String entityId, String localId, String commonId, String tpShopId) {
		ItemMappingQuery query = new ItemMappingQuery();
		query.setEntityId(entityId);
		query.setTpShopId(tpShopId);
		query.setPlatCode(platCode);
		query.setIdType((int)idType);
		query.setLocalId(localId);
		query.setCommonId(commonId);
		List<ItemMapping> itemMappings = this.getItemMappingListByQuery(query);
		return CollectionUtils.isNotEmpty(itemMappings) ? itemMappings.get(0) : null;
	}

	/**
	 * 根据tpId查localId
	 */
	@Override
	public ItemMapping getLocalId(String platCode, Byte idType, String entityId, String tpId, String tpShopId) {
		ItemMappingQuery query = new ItemMappingQuery();
		query.setEntityId(entityId);
		query.setTpShopId(tpShopId);
		query.setPlatCode(platCode);
		query.setIdType((int)idType);
		query.setTpId(tpId);
		List<ItemMapping> itemMappings = this.getItemMappingListByQuery(query);
		return CollectionUtils.isNotEmpty(itemMappings) ? itemMappings.get(0) : null;
	}

	/**
	 * 批量删除映射数据(已处理缓存)
	 */
	@Override
	public int batchDeleteByEntityId(String entityId, String tpShopId, String platCode) {
		//查询数据
        List<ItemMapping> itemMappings = new ArrayList<>();
        int pageIndex = 1;
        while (true) {
            ItemMappingQuery itemMappingQuery = new ItemMappingQuery();
            itemMappingQuery.setEntityId(entityId);
            itemMappingQuery.setTpShopId(tpShopId);
            itemMappingQuery.setPlatCode(platCode);
            itemMappingQuery.setPageIndex(pageIndex);
            itemMappingQuery.setPageSize(ItemMappingQuery.MAX_PAGE_SIZE);
            List<ItemMapping> itemMappingsMax = itemMappingDao.getItemMappingListByQuery(itemMappingQuery);
            if(CollectionUtils.isNotEmpty(itemMappingsMax)){
				itemMappings.addAll(itemMappingsMax);
            }else {
                break;
            }
			pageIndex++;
        }

		int count = itemMappingDao.batchDeleteByEntityId(entityId, tpShopId, platCode);
		if(count>0 && CollectionUtils.isNotEmpty(itemMappings)){
			this.clearCache(itemMappings);
		}

		return count;
	}

	/**
	 * 根据query计数
	 */
	@Override
	public int countByQuery(ItemMappingQuery query){
		if(StringUtils.isBlank(query.getEntityId())){
			throw new BizException("entityId不能为空！");
		}
		//校验list字段
		if((query.getIdList()!=null && query.getIdList().size()==0)
				|| (query.getIdTypeList()!=null && query.getIdTypeList().size()==0)
				|| (query.getLocalIdList()!=null && query.getLocalIdList().size()==0)
				|| (query.getCommonIdList()!=null && query.getCommonIdList().size()==0)
				|| (query.getTpIdList()!=null && query.getTpIdList().size()==0)){
			return 0;
		}

		return itemMappingDao.countByQuery(query);
	}



	/**
	 * 批量查询
	 */
    public Map<String, String> batchQueryRelateTpIds(String platCode, byte idType, String entityId, String tpShopId, List<ItemMapping> itemMappings) {
        List<ItemMapping> list = itemMappingDao.batchQueryRelateTpIds(platCode, idType, entityId, tpShopId, itemMappings);
        if(list == null) {
            return new HashMap<>();
        } else {
			Map<String, String> map = new HashMap<>();
			if (CommonConstant.ITEM_SKU == idType) {
				for (ItemMapping itemMapping : list) {
					String localAssemblyId = itemMapping.getLocalId() + ":" + itemMapping.getCommonId();
					map.put(localAssemblyId, itemMapping.getTpId());
				}
				return map;
			}
		}
        return list.stream().collect(Collectors.toMap(ItemMapping::getLocalId, ItemMapping::getTpId));

    }

	/**
	 * 批量查询
	 */
	public List<ItemMapping> batchQueryRelateLocalIds(String platCode, byte idType, String entityId, String tpShopId, List<String> tpIdList) {
		return itemMappingDao.batchQueryRelateLocalIds(platCode, idType, entityId, tpShopId, tpIdList);
	}

	/**
	 * 批量查询
	 */
    public List<ItemMapping> batchQueryRelateTpIds(String platCode, Byte idType, String entityId, String shopId, Set<String> localIdSet) {
		if(CollectionUtils.isEmpty(localIdSet)) {
			return new ArrayList<>();
		}
		List<String> LocalIds = new ArrayList<>(localIdSet);

		ItemMappingQuery query = new ItemMappingQuery();
		query.setEntityId(entityId);
		query.setTpShopId(shopId);
		query.setPlatCode(platCode);
		query.setIdType((int)idType);
		query.setLocalIdList(LocalIds);
		query.setPageSize(ItemMappingQuery.MAX_PAGE_SIZE);
		return this.getItemMappingListByQuery(query);
	}



	/**
	 * 特殊处理
	 */
	public List<ItemMapping> getItemMappingListByQueryWithoutEntityId(ItemMappingQuery query) {
		//校验list字段
		if((query.getIdList()!=null && query.getIdList().size()==0)
				|| (query.getIdTypeList()!=null && query.getIdTypeList().size()==0)
				|| (query.getLocalIdList()!=null && query.getLocalIdList().size()==0)
				|| (query.getCommonIdList()!=null && query.getCommonIdList().size()==0)
				|| (query.getTpIdList()!=null && query.getTpIdList().size()==0)){
			return new ArrayList<>();
		}
		return itemMappingDao.getListByQueryWithoutEntityId(query);
	}

	public List<BrandSyncResultBo> batchQueryFailCount(String platCode, List<String> entityIdList) {
		return itemMappingDao.batchQueryFailCount(platCode, entityIdList);
	}





	/**
	 * 清除缓存
	 */
	private void clearCache(ItemMapping itemMapping){
		String entityId = itemMapping.getEntityId();
		String tpShopId = itemMapping.getTpShopId();
		String platCode = itemMapping.getPlatCode();
		Integer idType = itemMapping.getIdType();
		String localId = itemMapping.getLocalId();
		String commonId = itemMapping.getCommonId();
		String tpId = itemMapping.getTpId();

		//清除缓存key1
		String key1 = CacheConstants.Namespace.ITEM_MENU_MAPPING + entityId + tpShopId + platCode + idType + localId + (idType==8 ? commonId : "");
		itemCacheService.clearCache(key1);

		//清除缓存key2
		if(StringUtils.isNotBlank(itemMapping.getTpId())){
			String key2 = CacheConstants.Namespace.ITEM_MENU_MAPPING + entityId + tpShopId + platCode + idType + tpId;
			itemCacheService.clearCache(key2);
		}
	}

	/**
	 * 清除缓存批量
	 */
	private void clearCache(List<ItemMapping> itemMappings){
		List<String> key1s = new ArrayList<>();
		List<String> key2s = new ArrayList<>();

		itemMappings.forEach(itemMapping -> {
			String entityId = itemMapping.getEntityId();
			String tpShopId = itemMapping.getTpShopId();
			String platCode = itemMapping.getPlatCode();
			Integer idType = itemMapping.getIdType();
			String localId = itemMapping.getLocalId();
			String commonId = itemMapping.getCommonId();
			String tpId = itemMapping.getTpId();
			//清除缓存key1
			String key1 = CacheConstants.Namespace.ITEM_MENU_MAPPING + entityId + tpShopId + platCode + idType + localId + (idType==8 ? commonId : "");
			key1s.add(key1);
			//清除缓存key2
			if(StringUtils.isNotBlank(itemMapping.getTpId())){
				String key2 = CacheConstants.Namespace.ITEM_MENU_MAPPING + entityId + tpShopId + platCode + idType + tpId;
				key2s.add(key2);
			}
		});

		if(CollectionUtils.isNotEmpty(key1s)) {
			String[] key1Arr = new String[key1s.size()];
			key1s.toArray(key1Arr);
			itemCacheService.clearCache(key1Arr);
		}
		if(CollectionUtils.isNotEmpty(key2s)) {
			String[] key2Arr = new String[key2s.size()];
			key2s.toArray(key2Arr);
			itemCacheService.clearCache(key2Arr);
		}
	}

	/**
	 * 获取缓存
	 * （主要根据localId|tpId字段）
	 */
	private ItemMapping getCache(String entityId, String tpShopId, String platCode, Integer idType, String localId, String commonId, String tpId){
		//获取缓存key1
		if(StringUtils.isNotBlank(entityId) && StringUtils.isNotBlank(tpShopId) && StringUtils.isNotBlank(platCode) && idType!=null && StringUtils.isNotBlank(localId) && (idType != 8 || StringUtils.isNotBlank(commonId))){
			String key1 = CacheConstants.Namespace.ITEM_MENU_MAPPING + entityId + tpShopId + platCode + idType + localId + (idType==8 ? commonId : "");
			List<String> keyList = new ArrayList<>();
			keyList.add(key1);
			List<ItemMapping> itemMappingList = itemCacheService.getCache(keyList);
			ItemMapping itemMapping = null;
			if(CollectionUtils.isNotEmpty(itemMappingList)) {
				itemMapping = itemMappingList.get(0);
			}
			return itemMapping;
		}

		//获取缓存key2
		if(StringUtils.isNotBlank(entityId) && StringUtils.isNotBlank(tpShopId) && StringUtils.isNotBlank(platCode) && idType!=null && StringUtils.isNotBlank(tpId)){
			String key2 = CacheConstants.Namespace.ITEM_MENU_MAPPING + entityId + tpShopId + platCode + idType + tpId;
			List<String> keyList = new ArrayList<>();
			keyList.add(key2);
			List<ItemMapping> itemMappingList = itemCacheService.getCache(keyList);
			ItemMapping itemMapping = null;
			if(CollectionUtils.isNotEmpty(itemMappingList)) {
				itemMapping = itemMappingList.get(0);
			}
			return itemMapping;
		}

		return null;
	}

	/**
	 * 获取缓存
	 * （主要根据localId|tpId字段）
	 * （若有commonIds，需与localId顺序一至）
	 */
	private List<ItemMapping> getCache(String entityId, String tpShopId, String platCode, Integer idType, List<String> localIds, List<String> commonIds, List<String> tpIds){
		if (idType==8 && (localIds!=null && commonIds!=null && localIds.size()!=commonIds.size())){
			return new ArrayList<>();
		}
		List<String> keys = new ArrayList<>();

		//获取缓存key1
		if(StringUtils.isNotBlank(entityId) && StringUtils.isNotBlank(tpShopId) && StringUtils.isNotBlank(platCode) && idType!=null && CollectionUtils.isNotEmpty(localIds) && (idType != 8 || CollectionUtils.isNotEmpty(commonIds))){
			for(int i=0 ;i<localIds.size(); i++){
				String key1 = CacheConstants.Namespace.ITEM_MENU_MAPPING + entityId + tpShopId + platCode + idType + localIds.get(i) + (idType==8 ? commonIds.get(i) : "");
				keys.add(key1);
			}
			return itemCacheService.getCache(keys);
		}

		//获取缓存key2
		if(StringUtils.isNotBlank(entityId) && StringUtils.isNotBlank(tpShopId) && StringUtils.isNotBlank(platCode) && idType!=null && CollectionUtils.isNotEmpty(tpIds)){
			for (String tpId : tpIds) {
				String key2 = CacheConstants.Namespace.ITEM_MENU_MAPPING + entityId + tpShopId + platCode + idType + tpId;
				keys.add(key2);
			}
			return itemCacheService.getCache(keys);
		}

		return new ArrayList<>();
	}

	/**
	 * 添加缓存
	 */
	private void putCache(List<ItemMapping> itemMappings){
		if(CollectionUtils.isNotEmpty(itemMappings)){
			Map<String, Object> keyValueMap = new HashMap<>();
			for (ItemMapping itemMapping : itemMappings){
				String entityId = itemMapping.getEntityId();
				String tpShopId = itemMapping.getTpShopId();
				String platCode = itemMapping.getPlatCode();
				Integer idType = itemMapping.getIdType();
				String localId = itemMapping.getLocalId();
				String commonId = itemMapping.getCommonId();
				String tpId = itemMapping.getTpId();
				//添加缓存key1
				if(StringUtils.isNotBlank(entityId)
						&& StringUtils.isNotBlank(tpShopId)
						&& StringUtils.isNotBlank(platCode)
						&& idType!=null
						&& StringUtils.isNotBlank(localId)
						&& (idType != 8 || StringUtils.isNotBlank(commonId))){
					String key1 = CacheConstants.Namespace.ITEM_MENU_MAPPING + entityId + tpShopId + platCode + idType + localId + (idType==8 ? commonId : "");
					keyValueMap.put(key1, itemMapping);
				}

				//添加缓存key2
				if(StringUtils.isNotBlank(entityId)
						&& StringUtils.isNotBlank(tpShopId)
						&& StringUtils.isNotBlank(platCode)
						&& idType!=null
						&& StringUtils.isNotBlank(tpId)){
					String key2 = CacheConstants.Namespace.ITEM_MENU_MAPPING + entityId + tpShopId + platCode + idType + tpId;
					keyValueMap.put(key2, itemMapping);
				}
			}
			itemCacheService.putCache(keyValueMap, CacheConstants.ITEM_CACHE_BY_TEN_MIN);
		}
	}

	/**
	 * 获取查询类型
	 * @param query query
	 * @return 0:不走缓存 1：走缓存
	 */
	private int getQueryType(ItemMappingQuery query){
		if(query.getId() != null
				|| query.getSyncStatus()!=null
				|| query.getSyncResult()!=null
				|| query.getExt()!=null
				|| query.getIsValid()==null
				|| query.getIsValid()!=1
				|| query.getOpTime()!=null
				|| query.getCreateTime()!=null
				|| query.getLastVer()!=null
				|| query.getIdList()!=null
				|| query.getIdTypeList()!=null){
			return 0;//特殊查询
		}
		if(query.getLocalIdList()!=null
				|| query.getCommonIdList()!=null
				|| query.getTpIdList()!=null){
			return 0;//批量查询
		}
		if(StringUtils.isBlank(query.getEntityId())
				|| StringUtils.isBlank(query.getTpShopId())
				|| StringUtils.isBlank(query.getPlatCode())
				|| query.getIdType()==null){
			return 0;//特殊查询
		}
		if (query.getIdType()==8){
			if(query.getLocalId()!=null && query.getCommonId()!=null && query.getTpId()==null){
				return 1;//精确查询
			} else if(query.getLocalId()==null && query.getCommonId()==null && query.getTpId()!=null){
				return 1;//精确查询
			}
		}else if(query.getCommonId()==null){
			if (query.getLocalId()!=null && query.getTpId()==null){
				return 1;//精确查询
			}else if(query.getLocalId()==null && query.getTpId()!=null){
				return 1;//精确查询
			}
		}
		return 0;//其他非精确查询
	}
}
