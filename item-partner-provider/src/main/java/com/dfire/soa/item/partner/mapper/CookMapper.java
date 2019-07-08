package com.dfire.soa.item.partner.mapper;

import com.dfire.soa.item.partner.bo.query.CookQuery;
import com.dfire.soa.item.partner.domain.CookDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: xiaoji
 * @Date: create on 2018/8/27
 * @Describle:
 */
@Mapper
public interface CookMapper {
	/**
	 * 新增
	 *
	 * @param cookDO
	 * @return
	 */
	int insert(CookDO cookDO);

	/**
	 * 修改
	 *
	 * @param cookDO
	 * @return
	 */
	int updateById(CookDO cookDO);

	/**
	 * 删除
	 *
	 * @param entityId
	 * @param id
	 * @return
	 */
	int deleteById(@Param("entityId") String entityId, @Param("id") Long id);

	/**
	 * 根据id查询菜谱
	 *
	 * @param entityId
	 * @param id
	 * @return
	 */
	CookDO queryById(@Param("entityId") String entityId, @Param("id") Long id);

	/**
	 * 添加菜谱
	 *
	 * @param cookDOList
	 * @return
	 */
	Integer batchInsert(@Param("cookDOList") List<CookDO> cookDOList);

	/**
	 * 根据entityId查询是否有菜谱
	 *
	 * @param entityId
	 * @return
	 */
	List<CookDO> selectByEntityId(String entityId);

	/**
	 * 通用查询
	 *
	 * @param cookQuery
	 * @return
	 */
	List<CookDO> selectByQuery(CookQuery cookQuery);

	/**
	 * 根据type查询
	 *
	 * @param entityId
	 * @param type
	 * @return
	 */
	CookDO selectByType(@Param("entityId") String entityId, @Param("type") int type);

	/**
	 * 订正数据使用
	 *
	 * @param time
	 * @return
	 */
	Integer batchDelete(@Param("createTime") Long time);

	/**
	 * 订正数据使用
	 *
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	List<String> getEntityIdList(@Param("startTime") long startTime, @Param("endTime") long endTime);

	/**
	 * 订正数据使用
	 */
	void updateStatus();

}
