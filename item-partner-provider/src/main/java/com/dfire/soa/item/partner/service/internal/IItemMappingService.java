package com.dfire.soa.item.partner.service.internal;


import com.dfire.soa.item.partner.bo.BrandSyncResultBo;
import com.dfire.soa.item.partner.bo.ItemMapping;
import com.dfire.soa.item.partner.bo.query.CommonIdModel;
import com.dfire.soa.item.partner.bo.query.ItemMappingQuery;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IItemMappingService{
	
	/**
	 * 保存
	 */
	int saveItemMapping(ItemMapping itemMapping);
	
	/**
	 * 更新
	 */
	int updateItemMapping(ItemMapping itemMapping);
	
	/**
	 * 逻辑删除
	 */
	int deleteItemMappingById(String entityId, Long id);

	/**
	 * 根据id查询
	 */
	ItemMapping getItemMappingById(String entityId, Long id);

	/**
	 * 根据query查询
	 */
	List<ItemMapping> getItemMappingListByQuery(ItemMappingQuery query);

	/**
	 * 根据localIds查询
	 * （idType不为8时使用）
	 */
	List<ItemMapping> getItemMappingListByLocalIds(String platCode, byte idType, String entityId, String tpShopId, List<String> localIds);

	/**
	 * 根据<localId commonId>查询
	 * （idType为8时使用）
	 */
	List<ItemMapping> getItemMappingListByCommonIdModels(String platCode, byte idType, String entityId, String tpShopId, List<CommonIdModel> commonIdModels);

	/**
	 * 根据localIds查询
	 */
	List<ItemMapping> getItemMappingListByTpIds(String platCode, byte idType, String entityId, String tpShopId, List<String> tpIds);

	/**
	 * 根据localId查tpId
	 */
	ItemMapping getTpId(String platCode, Byte idType, String entityId, String localId, String tpShopId);

	/**
	 * 根据localId查tpId（使用commonId 作为条件查询）
	 */
	ItemMapping getTpId(String platCode, Byte idType, String entityId, String localId, String commonId, String tpShopId);

	/**
	 * 根据tpId查localId
	 */
	ItemMapping getLocalId(String platCode, Byte idType, String entityId, String tpId, String tpShopId);

	/**
	 * 批量删除映射数据
	 */
	int batchDeleteByEntityId(String entityId, String tpShopId, String platCode);

	/**
	 * 根据query计数
	 */
	int countByQuery(ItemMappingQuery query);



	/**
	 * 批量查询
	 */
	Map<String, String> batchQueryRelateTpIds(String platCode, byte idType, String entityId, String tpShopId, List<ItemMapping> itemMappings);

	/**
	 * 批量查询
	 */
	List<ItemMapping> batchQueryRelateTpIds(String platCode, Byte idType, String entityId, String shopId, Set<String> localIdSet);

	/**
	 * 批量查询
	 */
	List<ItemMapping> batchQueryRelateLocalIds(String platCode, byte idType, String entityId, String tpShopId, List<String> tpIdList);



	/**
	 * 可以在没有entityId的时候，使用查询，不建议使用
	 * @param query
	 * @return
	 */
	@Deprecated
	List<ItemMapping> getItemMappingListByQueryWithoutEntityId(ItemMappingQuery query);

	List<BrandSyncResultBo> batchQueryFailCount(String platCode, List<String> entityIdList);



}
