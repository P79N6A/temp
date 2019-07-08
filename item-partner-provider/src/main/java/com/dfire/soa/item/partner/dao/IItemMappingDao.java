package com.dfire.soa.item.partner.dao;

import com.dfire.soa.item.partner.bo.BrandSyncResultBo;
import com.dfire.soa.item.partner.bo.ItemMapping;
import com.dfire.soa.item.partner.bo.query.ItemMappingQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IItemMappingDao{
	
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


	int batchDeleteByEntityId(String entityId, String tpShopId, String platCode);

	/**
	 * 根据id查询
	 */
	ItemMapping getItemMappingById(String entityId, Long id);
	
	/**
	 * 根据query查询
	 */
	List<ItemMapping> getItemMappingListByQuery(ItemMappingQuery query);


    List<ItemMapping> getListByQueryWithoutEntityId(ItemMappingQuery query);
	
	/**
	 * 根据query计数
	 */
	int countByQuery(ItemMappingQuery query);

	List<ItemMapping> batchQueryRelateTpIds(String platCode, byte idType, String entityId, String tpShopId, List<ItemMapping> itemMappings);

	List<ItemMapping> batchQueryRelateLocalIds(String platCode, byte idType, String entityId, String tpShopId, List<String> tpIdList);

	List<BrandSyncResultBo> batchQueryFailCount(String platCode, List<String> entityIdList);

}
