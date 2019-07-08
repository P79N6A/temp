package com.dfire.soa.item.partner.dao.impl;


import com.dfire.soa.flame.UniqueIdGenerator;
import com.dfire.soa.item.partner.bo.ItemMenuMapping;
import com.dfire.soa.item.partner.bo.query.ItemMenuMappingQuery;
import com.dfire.soa.item.partner.dao.IItemMenuMappingDao;
import com.dfire.soa.item.partner.mapper.ItemMenuMappingMapper;
import com.twodfire.share.result.Result;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

@Service
public class ItemMenuMappingDao implements IItemMenuMappingDao {
	
	@Resource
	private ItemMenuMappingMapper itemMenuMappingMapper;
	@Resource
	private UniqueIdGenerator uniqueIdGenerator;
	
	/**
	 * 保存
	 */
	 @Override
	public int saveItemMenuMapping(ItemMenuMapping itemMenuMapping){
		 if (itemMenuMapping.getId() == null) {
			 Result<Long> idResult = uniqueIdGenerator.nextId();
			 itemMenuMapping.setId(idResult.getModel());
		 }
		return itemMenuMappingMapper.insert(itemMenuMapping);
	}
	
	/**
	 * 更新
	 */
	 @Override
	public int updateItemMenuMapping(ItemMenuMapping itemMenuMapping){
		return itemMenuMappingMapper.update(itemMenuMapping);
	}
	
	/**
	 * 逻辑删除
	 */
	 @Override
	public int deleteItemMenuMappingById(String entityId, Long id){
		return itemMenuMappingMapper.delete(entityId, id);
	}

	@Override
	public int batchDeleteByEntityId(String entityId, String tpShopId, String platCode) {
	 	return  itemMenuMappingMapper.batchDeleteByEntityId(entityId, tpShopId, platCode);
	}
	
	/**
	 * 根据id查询
	 */
	 @Override
	public ItemMenuMapping getItemMenuMappingById(String entityId, Long id){
		return itemMenuMappingMapper.getById(entityId, id);
	}
	
	/**
	 * 根据query查询
	 */
	 @Override
	public List<ItemMenuMapping> getItemMenuMappingListByQuery(ItemMenuMappingQuery query){
		return itemMenuMappingMapper.getListByQuery(query);
	}
	
	/**
	 * 根据query计数
	 */
	 @Override
	public int countByQuery(ItemMenuMappingQuery query){
		return itemMenuMappingMapper.countByQuery(query);
	}

}
