package com.dfire.soa.item.partner.mapper;

import com.dfire.soa.item.partner.bo.PartnerMapping;
import com.dfire.soa.item.partner.bo.query.PartnerMappingQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PartnerMappingMapper {
	
	int insert(PartnerMapping partnerMapping);
	
	int update(PartnerMapping partnerMapping);
	
	int delete(@Param(value = "entityId") String entityId, @Param(value = "id") Long id);
	
	PartnerMapping getById(@Param(value = "entityId") String entityId, @Param(value = "id") Long id);
	
	List<PartnerMapping> getListByQuery(PartnerMappingQuery query);
}
