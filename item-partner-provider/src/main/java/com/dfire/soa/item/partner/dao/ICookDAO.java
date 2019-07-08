package com.dfire.soa.item.partner.dao;

import com.dfire.soa.item.partner.bo.query.CookQuery;
import com.dfire.soa.item.partner.domain.CookDO;

import java.util.List;

/**
 * @Author: xiaoji
 * @Date: create on 2018/8/27
 * @Describle:
 */
public interface ICookDAO {

	/**
	 * 添加菜谱
	 *
	 * @param cookDO
	 * @return
	 */
	Boolean insert(CookDO cookDO);

	/**
	 * 修改菜谱
	 *
	 * @param cookDO
	 * @return
	 */
	Boolean updateById(CookDO cookDO);

	/**
	 * 删除菜谱
	 *
	 * @param entityId
	 * @param id
	 * @return
	 */
	Boolean deleteById(String entityId, Long id);

	/**
	 * 根据id查询菜谱
	 *
	 * @param entityId
	 * @param id
	 * @return
	 */
	CookDO queryById(String entityId, Long id);

	/**
	 * 添加菜谱
	 *
	 * @param cookDOList
	 * @return
	 */
	Integer batchInsert(List<CookDO> cookDOList);

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
	CookDO selectByType(String entityId, int type);

	/**
	 * 订正数据使用
	 *
	 * @param time
	 * @return
	 */
	Integer batchDelete(Long time);

	/**
	 * 订正数据使用
	 *
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	List<String> getEntityIdList(long startTime, long endTime);

	/**
	 * 订正数据使用
	 *
	 */
	void updateStatus();

}
