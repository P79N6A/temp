package com.dfire.soa.item.partner.manager;

import com.dfire.soa.item.partner.domain.CookDO;

import java.util.List;

/**
 * @Author: xiaoji
 * @Date: create on 2018/8/27
 * @Describle:
 */
public interface ICookManager {

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
	 * 批量添加菜谱
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
	 * 根据type查询
	 *
	 * @param entityId
	 * @param type
	 * @return
	 */
	CookDO selectByType(String entityId, int type);

	/**
	 *
	 * @param time
	 * @return
	 */
	Integer batchDelete(Long time);

	List<String> getEntityIdList(long startTime, long endTime);

	void updateStatus();
}
