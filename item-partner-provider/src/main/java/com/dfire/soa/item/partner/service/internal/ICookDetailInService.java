package com.dfire.soa.item.partner.service.internal;

import com.dfire.soa.item.bo.Menu;
import com.dfire.soa.item.partner.bo.CookDetailBO;
import com.dfire.soa.item.partner.bo.SpecBO;
import com.dfire.soa.item.partner.bo.query.CookDetailQuery;
import com.twodfire.share.result.Result;

import java.util.List;
import java.util.Map;

/**
 * @Author: xiaoji
 * @Date: create on 2018/8/27
 * @Describle:
 */
public interface ICookDetailInService {

	/**
	 * 新增
	 *
	 * @param cookDetailBO
	 * @return
	 */
	Boolean insert(CookDetailBO cookDetailBO);

	/**
	 * 修改
	 *
	 * @param cookDetailBO
	 * @return
	 */
	Boolean updateById(CookDetailBO cookDetailBO);

	/**
	 * 根据id删除
	 *
	 * @param entityId
	 * @param id
	 * @return
	 */
	Boolean deleteById(String entityId, Long id);

	/**
	 * 批量添加
	 *
	 * @param cookDetailBOList
	 * @return
	 */
	Integer batchInsert(List<CookDetailBO> cookDetailBOList);

	/**
	 * 菜谱明细批量添加
	 *
	 * @param entityId
	 * @param cookId
	 * @param menuIdList
	 * @return
	 */
	Integer addCookDetailMenus(String entityId, Long cookId, List<String> menuIdList);

	/**
	 * 批量删除菜谱明细
	 *
	 * @param entityId
	 * @param idList
	 * @return
	 */
	Integer batchDeleteByIdList(String entityId, List<Long> idList);

	/**
	 * 通用查询
	 *
	 * @param cookDetailQuery
	 * @return
	 */
	List<CookDetailBO> selectByQuery(CookDetailQuery cookDetailQuery);

	/**
	 * 查询菜谱菜品
	 *
	 * @param entityId
	 * @param cookId
	 * @return
	 */
	List<String> queryMenuIdsByCookId(String entityId, Long cookId);

	/**
	 * 根据id查询
	 *
	 * @param entityId
	 * @param id
	 * @return
	 */
	CookDetailBO queryById(String entityId, Long id);

	/**
	 * 获取规格信息map
	 *
	 * @param entityId
	 * @param menuList
	 * @return
	 */
	Map<String, List<SpecBO>> getSpecDetailMap(String entityId, List<Menu> menuList);

	/**
	 * 根据menuId删除
	 *
	 * @param entityId
	 * @param menuId
	 * @return
	 */
	Boolean deleteByMenuId(String entityId, String menuId);



	List<String> queryEntityIdWihtFewCookDetail();

}
