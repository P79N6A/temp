package com.dfire.soa.item.partner.service.internal;

import com.dfire.soa.item.partner.bo.CookBO;

import java.util.List;
import java.util.Map;

/**
 * @Author: xiaoji
 * @Date: create on 2018/8/27
 * @Describle:
 */
public interface ICookInService {

	/**
	 * 新增
	 *
	 * @param cookBO
	 * @return
	 */
	Boolean insert(CookBO cookBO);

	/**
	 * 修改
	 *
	 * @param cookBO
	 * @return
	 */
	Boolean updateById(CookBO cookBO);

	/**
	 * 根据id删除
	 *
	 * @param entityId
	 * @param id
	 * @return
	 */
	Boolean deleteById(String entityId, Long id);

	/**
	 * 根据idlist批量添加老店菜谱数据
	 *
	 * @param idList
	 * @return
	 */
	Integer batchInsertByIdList(Map<String, Long> idList);

	/**
	 * 订正数据使用
	 *
	 * @param time
	 * @return
	 */
	Integer batchDelete(Long time);

	/**
	 * 根据entityId查询当前有效菜谱list
	 *
	 * @param entityId
	 * @return
	 */
	List<CookBO> selectByEntityId(String entityId);

	/**
	 * 根据id查询菜谱
	 *
	 * @param entityId
	 * @param id
	 * @return
	 */
	CookBO queryById(String entityId, Long id);

	/**
	 * 根据type查询
	 *
	 * @param entityId
	 * @param type
	 * @return
	 */
	CookBO selectByType(String entityId, int type);

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
	 */
	void updateStatus();

	/**
	 * 根据entityId清除有误数据
	 *
	 * @param entityIdList
	 * @return
	 */
	Integer batchDeleteByMenuIdList(String[] entityIdList);

    /**
     * 查询有误订正数据
     *
     * @param entityId
     * @param cookId
     * @param menuId
     */
    Integer queryCountByMenuIdAndCreateTime(String entityId,Long cookId,String menuId);

}
