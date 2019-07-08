package com.dfire.soa.item.partner.dao;

import com.dfire.soa.item.partner.bo.ItemMenuMapping;
import com.dfire.soa.item.partner.bo.query.ItemMenuMappingQuery;

import java.util.List;

public interface IItemMenuMappingDao{
	
	/**
	 * 保存
	 */
	int saveItemMenuMapping(ItemMenuMapping itemMenuMapping);
	
	/**
	 * 更新
	 */
	int updateItemMenuMapping(ItemMenuMapping itemMenuMapping);
	
	/**
	 * 逻辑删除
	 */
	int deleteItemMenuMappingById(String entityId, Long id);

	int batchDeleteByEntityId(String entityId, String tpShopId, String platCode);
	
	/**
	 * 根据id查询
	 */
	ItemMenuMapping getItemMenuMappingById(String entityId, Long id);
	
	/**
	 * 根据query查询
	 */
	List<ItemMenuMapping> getItemMenuMappingListByQuery(ItemMenuMappingQuery query);
	
	/**
	 * 根据query计数
	 */
	int countByQuery(ItemMenuMappingQuery query);

}
