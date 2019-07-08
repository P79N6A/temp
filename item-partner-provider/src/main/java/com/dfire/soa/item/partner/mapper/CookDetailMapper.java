package com.dfire.soa.item.partner.mapper;

import com.dfire.soa.item.partner.bo.query.CookDetailQuery;
import com.dfire.soa.item.partner.domain.CookDetailDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: xiaoji
 * @Date: create on 2018/8/27
 * @Describle:
 */
@Mapper
public interface CookDetailMapper {

	/**
	 * 添加菜谱明细
	 *
	 * @param cookDetailDO
	 * @return
	 */
	int insert(CookDetailDO cookDetailDO);

	/**
	 * 修改菜谱明细
	 *
	 * @param cookDetailDO
	 * @return
	 */
	int updateById(CookDetailDO cookDetailDO);

	/**
	 * 删除菜谱明细
	 *
	 * @param entityId
	 * @param id
	 * @return
	 */
	int deleteById(@Param("entityId") String entityId, @Param("id") Long id);

	/**
	 * 批量添加
	 *
	 * @param cookDetailDOList
	 * @return
	 */
	Integer batchInsert(@Param("cookDetailDOList") List<CookDetailDO> cookDetailDOList);

	/**
	 * 查询菜谱菜品
	 *
	 * @param entityId
	 * @param cookId
	 * @return
	 */
	List<String> queryMenuIdsByCookId(@Param("entityId") String entityId, @Param("cookId") Long cookId);

	/**
	 * 批量删除菜谱明细
	 *
	 * @param entityId
	 * @param idList
	 * @return
	 */
	Integer batchDeleteByIdList(@Param("entityId") String entityId, @Param("idList") List<Long> idList);

	/**
	 * 根据id查询
	 *
	 * @param entityId
	 * @param id
	 * @return
	 */
	CookDetailDO queryById(@Param("entityId") String entityId, @Param("id") Long id);

	/**
	 * 根据id查询
	 *
	 * @param entityId
	 * @param id
	 * @return
	 */
	CookDetailDO queryByIdWithoutValid(@Param("entityId") String entityId, @Param("id") Long id);

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
	Boolean deleteByMenuId(@Param("entityId") String entityId, @Param("menuId") String menuId);

	/**
	 * 批量根据menuId删除
	 *
	 * @param entityId
	 * @param menuIdList
	 * @return
	 */
	Integer deleteByMenuIdList(@Param("entityId") String entityId, @Param("menuIdList") List<String> menuIdList);


	/**
	 *
	 * @param time
	 * @return
	 */
	Integer batchDelete(@Param("createTime") Long time);


	List<String> queryEntityIdWihtFewCookDetail();


	/**
	 * 查询有误订正数据
	 *
	 * @param entityId
	 * @param cookId
	 * @param menuId
	 */
	Integer queryCountByMenuIdAndCreateTime(@Param("entityId") String entityId,@Param("cookId") Long cookId,@Param("menuId") String menuId);

    /**
     * 删除有误订正数据
     *
     * @param entityId
     * @param menuIdList
     * @return
     */
    Integer batchDeleteByMenuIdListAndCreateTime(@Param("entityId") String entityId,@Param("menuIdList") List<String> menuIdList);
}
