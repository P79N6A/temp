package com.dfire.soa.item.partner.mapper;

import com.dfire.soa.item.partner.bo.ExportAddition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: xiaoji
 * @Date: create on 2018/11/14
 * @Describle:
 */
@Mapper
public interface ExportAdditionMapper {

	List<ExportAddition> selectAll();

	void delete(String entityId);

	Integer queryCount();
}
