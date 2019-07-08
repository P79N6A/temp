package com.dfire.soa.item.partner.mapper;

import com.dfire.soa.item.partner.bo.BrandSyncResultBo;
import com.dfire.soa.item.partner.bo.ItemMapping;
import com.dfire.soa.item.partner.bo.query.ItemMappingQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ItemMappingMapper{
	
	int insert(ItemMapping itemMapping);
	
	int update(ItemMapping itemMapping);
	
	int delete( @Param(value = "entityId")String entityId,  @Param(value = "id")Long id);
	
	ItemMapping getById( @Param(value = "entityId")String entityId,  @Param(value = "id")Long id);
	
	List<ItemMapping> getListByQuery(ItemMappingQuery query);

	List<ItemMapping> getListByQueryWithoutEntityId(ItemMappingQuery query);

	int countByQuery(ItemMappingQuery query);

	List<ItemMapping> batchQueryRelateTpIds(@Param(value = "platCode")  String platCode, @Param(value = "idType")  byte idType, @Param(value = "entityId") String entityId, @Param(value = "tpShopId") String tpShopId,  @Param(value = "itemMappings") List<ItemMapping> itemMappings);

	List<ItemMapping> batchQueryRelateLocalIds(@Param(value = "platCode")  String platCode, @Param(value = "idType")  byte idType, @Param(value = "entityId") String entityId, @Param(value = "tpShopId") String tpShopId,  @Param(value = "tpIdList") List<String> tpIdList);

	int batchDeleteByEntityId(@Param(value = "entityId") String entityId, @Param(value = "tpShopId") String tpShopId, @Param(value = "platCode") String platCode);

	List<BrandSyncResultBo> batchQueryFailCount(@Param(value = "platCode") String platCode, @Param(value = "entityIdList") List<String> entityIdList);




}
