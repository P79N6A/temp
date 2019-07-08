package com.dfire.soa.item.partner.service;

import java.util.List;

import com.dfire.soa.item.partner.bo.ItemMenuMapping;
import com.dfire.soa.item.partner.bo.query.ItemMenuMappingQuery;
import com.twodfire.share.result.Result;

public interface IItemMenuMappingClientService{
	
	/**
	 * 新增/更新（根据id判断）
	 */
	Result<ItemMenuMapping> saveOrUpdateItemMenuMapping(ItemMenuMapping itemMenuMapping);

	/**
	 * 逻辑删除
	 */
	Result<Integer> deleteItemMenuMappingById(String entityId, Long id);
	
	/**
	 * 根据id查询
	 */
	Result<ItemMenuMapping> getItemMenuMappingById(String entityId, Long id);
	
	/**
	 * 根据query查询
	 */
	Result<List<ItemMenuMapping>> getItemMenuMappingListByQuery(ItemMenuMappingQuery query);

}
