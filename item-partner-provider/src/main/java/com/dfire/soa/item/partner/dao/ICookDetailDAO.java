package com.dfire.soa.item.partner.dao;

import com.dfire.soa.item.partner.bo.query.CookDetailQuery;
import com.dfire.soa.item.partner.domain.CookDetailDO;
import com.twodfire.share.result.Result;

import java.util.List;

/**
 * @Author: xiaoji
 * @Date: create on 2018/8/27
 * @Describle:
 */
public interface ICookDetailDAO {

	/**
	 * 添加菜谱明细
	 *
	 * @param cookDetailDO
	 * @return
	 */
	Boolean insert(CookDetailDO cookDetailDO);

	/**
	 * 修改菜谱明细
	 *
	 * @param cookDetailDO
	 * @return
	 */
	Boolean updateById(CookDetailDO cookDetailDO);

	/**
	 * 删除菜谱明细
	 *
	 * @param entityId
	 * @param id
	 * @return
	 */
	Boolean deleteById(String entityId, Long id);

	/**
	 * 批量添加
	 *
	 * @param cookDetailDOList
	 * @return
	 */
	Integer batchInsert(List<CookDetailDO> cookDetailDOList);

	/**
	 * 订正数据使用
	 *
	 * @param time
	 * @return
	 */
	Integer batchDelete(Long time);

	/**
	 * 查询菜谱菜品
	 *
	 * @param entityId
	 * @param cookId
	 * @return
	 */
	List<String> queryMenuIdsByCookId(String entityId, Long cookId);

	/**
	 * 批量删除菜谱明细
	 *
	 * @param entityId
	 * @param idList
	 * @return
	 */
	Integer batchDeleteByIdList(String entityId, List<Long> idList);

	/**
	 * 根据id查询
	 *
	 * @param entityId
	 * @param id
	 * @return
	 */
	CookDetailDO queryById(String entityId, Long id);

	/**
	 * 根据id查询
	 *
	 * @param entityId
	 * @param id
	 * @return
	 */
	CookDetailDO queryByIdWithoutValid(String entityId, Long id);

	/**
	 * 通用查询
	 *
	 * @param cookDetailQuery
	 * @return
	 */
	List<CookDetailDO> selectByQuery(CookDetailQuery cookDetailQuery);

	/**
	 * 根据menuId删除
	 *
	 * @param entityId
	 * @param menuId
	 * @return
	 */
	Boolean deleteByMenuId(String entityId, String menuId);

	/**
	 * 批量根据menuId删除
	 *
	 * @param entityId
	 * @param menuIdList
	 * @return
	 */
	Integer deleteByMenuIdList(String entityId, List<String> menuIdList);

	List<String> queryEntityIdWihtFewCookDetail();


	/**
	 * 清理有误订正数据
	 *
	 * @param entityId
	 * @param cookId
	 * @param menuId
	 */
	Integer queryCountByMenuIdAndCreateTime(String entityId,Long cookId,String menuId);

    /**
     * 删除有误订正数据
     *
     * @param entityId
     * @param menuIdList
     * @return
     */
    Integer batchDeleteByMenuIdListAndCreateTime(String entityId,List<String> menuIdList);
}
