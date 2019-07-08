package com.dfire.soa.item.partner.dao.impl;


import com.dfire.soa.flame.UniqueIdGenerator;
import com.dfire.soa.item.partner.bo.BrandSyncResultBo;
import com.dfire.soa.item.partner.bo.ItemMapping;
import com.dfire.soa.item.partner.bo.query.ItemMappingQuery;
import com.dfire.soa.item.partner.dao.IItemMappingDao;
import com.dfire.soa.item.partner.mapper.ItemMappingMapper;
import com.twodfire.share.result.Result;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ItemMappingDao implements IItemMappingDao {
	
	@Resource
	private ItemMappingMapper itemMappingMapper;
	@Resource
	private UniqueIdGenerator uniqueIdGenerator;
	
	/**
	 * 保存
	 */
	 @Override
	public int saveItemMapping(ItemMapping itemMapping){
		 if (itemMapping.getId() == null) {
			 Result<Long> idResult = uniqueIdGenerator.nextId();
			 itemMapping.setId(idResult.getModel());
		 }
		return itemMappingMapper.insert(itemMapping);
	}
	
	/**
	 * 更新
	 */
	 @Override
	public int updateItemMapping(ItemMapping itemMapping){
		return itemMappingMapper.update(itemMapping);
	}
	
	/**
	 * 逻辑删除
	 */
	 @Override
	public int deleteItemMappingById(String entityId, Long id){
		return itemMappingMapper.delete(entityId, id);
	}

	public int batchDeleteByEntityId(String entityId, String tpShopId, String platCode) {
		return itemMappingMapper.batchDeleteByEntityId(entityId, tpShopId, platCode);
	}

	/**
	 * 根据id查询
	 */
	 @Override
	public ItemMapping getItemMappingById(String entityId, Long id){
		return itemMappingMapper.getById(entityId, id);
	}
	
	/**
	 * 根据query查询
	 */
	 @Override
	public List<ItemMapping> getItemMappingListByQuery(ItemMappingQuery query){
		return itemMappingMapper.getListByQuery(query);
	}

	public List<ItemMapping> getListByQueryWithoutEntityId(ItemMappingQuery query) {
        return itemMappingMapper.getListByQueryWithoutEntityId(query);
    }
	
	/**
	 * 根据query计数
	 */
	 @Override
	public int countByQuery(ItemMappingQuery query){
		return itemMappingMapper.countByQuery(query);
	}

	public List<ItemMapping> batchQueryRelateTpIds(String platCode, byte idType, String entityId, String tpShopId, List<ItemMapping> itemMappings) {
		return itemMappingMapper.batchQueryRelateTpIds(platCode, idType, entityId, tpShopId, itemMappings);
	}

	@Override
	public List<ItemMapping> batchQueryRelateLocalIds(String platCode, byte idType, String entityId, String tpShopId, List<String> tpIdList) {
		return itemMappingMapper.batchQueryRelateLocalIds(platCode, idType, entityId, tpShopId, tpIdList);
	}

	@Override
	public List<BrandSyncResultBo> batchQueryFailCount(String platCode, List<String> entityIdList) {
		return itemMappingMapper.batchQueryFailCount(platCode, entityIdList);
	}

}
