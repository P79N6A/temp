package com.dfire.soa.item.partner.service;

import com.dfire.soa.item.partner.bo.ItemMapping;
import com.dfire.soa.item.partner.bo.query.ItemMappingQuery;
import com.dfire.soa.item.partner.bo.query.CommonIdModel;
import com.twodfire.share.result.Result;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IItemMappingClientService{
	
	/**
	 * 新增/更新（根据id判断）
	 */
	Result<ItemMapping> saveOrUpdateItemMapping(ItemMapping itemMapping);

	/**
	 * 逻辑删除
	 */
	Result<Integer> deleteItemMappingById(String entityId, Long id);
	
	/**
	 * 根据id查询
	 */
	Result<ItemMapping> getItemMappingById(String entityId, Long id);
	
	/**
	 * 根据query查询
	 */
	Result<List<ItemMapping>> getItemMappingListByQuery(ItemMappingQuery query);

	/**
	 * 根据localIds查询
	 * （现idType不为8时使用）
	 */
	Result<List<ItemMapping>> getItemMappingListByLocalIds(String platCode, byte idType, String entityId, String tpShopId, List<String> localIds);

	/**
	 * 根据localId、commonId查询
	 * （现idType为8时使用）
	 */
	Result<List<ItemMapping>> getItemMappingListByCommonIdModels(String platCode, byte idType, String entityId, String tpShopId, List<CommonIdModel> commonIdModels);

	/**
	 * 根据tpIds查询
	 */
	Result<List<ItemMapping>> getItemMappingListByTpIds(String platCode, byte idType, String entityId, String tpShopId, List<String> tpIds);




	/**
	 * 可以在没有entityId的时候，使用查询，不建议使用
	 * @param query
	 * @return
	 */
	@Deprecated
	Result<List<ItemMapping>> getItemMappingListByQueryWithoutEntityId(ItemMappingQuery query);

	/**
	 * 批量查skuId
	 * @param platCode
	 * @param idType
	 * @param entityId
	 * @param tpShopId
	 * @param itemMappings
	 * @return
	 */
	Result<Map<String, String>> batchQueryRelateTpIds(String platCode, byte idType, String entityId, String tpShopId, List<ItemMapping> itemMappings);

    /**
     *
     * @param platCode
     * @param idType
     * @param entityId
     * @param tpShopId
     * @param tpIdList
     * @return
     */
    Result<List<ItemMapping>> batchQueryRelateLocalIds(String platCode, byte idType, String entityId, String tpShopId, List<String> tpIdList);

	/**
	 * 查询商品映射
	 * @param platCode
	 * @param idType
	 * @param entityId
	 * @param localId
	 * @param commonId
	 * @param tpShopId
	 * @return
	 */
	Result<ItemMapping> getTpId(String platCode, Byte idType, String entityId, String localId, String commonId, String tpShopId);

	/**
	 * 查询商品映射
	 * @param platCode
	 * @param idType
	 * @param entityId
	 * @param localId
	 * @param tpShopId
	 * @return
	 */
	Result<ItemMapping> getTpId(String platCode, Byte idType, String entityId, String localId, String tpShopId);


	Result<ItemMapping> getLocalId(String platCode, Byte idType, String entityId, String tpId, String tpShopId);

	/**
	 * 根据IdList，批量查询
	 * @param platCode
	 * @param idType
	 * @param entityId
	 * @param shopId
	 * @param localIdSet
	 * @return
	 */
	Result<Map<String, String>> batchQueryRelateTpIds(String platCode, Byte idType, String entityId, String shopId, Set<String> localIdSet);

}
