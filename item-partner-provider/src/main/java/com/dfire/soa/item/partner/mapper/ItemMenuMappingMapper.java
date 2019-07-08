package com.dfire.soa.item.partner.mapper;

import com.dfire.soa.item.partner.bo.ItemMenuMapping;
import com.dfire.soa.item.partner.bo.query.ItemMenuMappingQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ItemMenuMappingMapper{
	
	int insert(ItemMenuMapping itemMenuMapping);
	
	int update(ItemMenuMapping itemMenuMapping);
	
	int delete(@Param(value = "entityId") String entityId, @Param(value = "id") Long id);

	int batchDeleteByEntityId(@Param(value = "entityId") String entityId, @Param(value = "tpShopId") String tpShopId, @Param(value = "platCode") String platCode);
	
	ItemMenuMapping getById(@Param(value = "entityId")  String entityId, @Param(value = "id") Long id);
	
	List<ItemMenuMapping> getListByQuery(ItemMenuMappingQuery query);
	
	int countByQuery(ItemMenuMappingQuery query);

}
