package com.dfire.soa.item.partner.service;

import com.dfire.soa.item.partner.bo.CookDetailBO;
import com.dfire.soa.item.vo.MenuVO;
import com.dfire.validator.annotation.Check;
import com.dfire.validator.annotation.Validator;
import com.dfire.validator.validator.NotBlank;
import com.dfire.validator.validator.NotNull;
import com.twodfire.share.result.Result;

import java.util.List;

/**
 * @Author: xiaoji
 * @Date: create on 2018/8/27
 * @Describle:
 */
public interface ICookDetailService {
	/**
	 * 新增
	 *
	 * @param cookDetailBO
	 * @return
	 */
	@Validator({@Check(name = "cookBO", adapter = NotNull.class, message = "对象不能为空")})
	Result<Boolean> insert(CookDetailBO cookDetailBO);

	/**
	 * 修改
	 *
	 * @param cookDetailBO
	 * @return
	 */
	@Validator({@Check(name = "cookBO", adapter = NotNull.class, message = "对象不能为空")})
	Result<Boolean> updateById(CookDetailBO cookDetailBO);

	/**
	 * 根据id删除
	 *
	 * @param entityId
	 * @param id
	 * @return
	 */
	@Validator({@Check(name = "entityId", adapter = NotBlank.class, message = "实体id不能为空"),
			@Check(name = "id", adapter = NotBlank.class, message = "id不能为空")})
	Result<Boolean> deleteById(String entityId, Long id);

	/**
	 * 菜谱明细批量添加
	 *
	 * @param entityId
	 * @param cookId
	 * @param menuIdList
	 * @return
	 */
	@Validator({@Check(name = "entityId", adapter = NotBlank.class, message = "实体id不能为空"),
			@Check(name = "cookId", adapter = NotBlank.class, message = "菜谱id不能为空")})
	Result addCookDetailMenus(String entityId, Long cookId, List<String> menuIdList);

	/**
	 * 批量删除菜谱明细
	 *
	 * @param entityId
	 * @param idList
	 * @return
	 */
	@Validator({@Check(name = "entityId", adapter = NotBlank.class, message = "实体id不能为空"),
			@Check(name = "idList", adapter = NotBlank.class, message = "菜谱明细id不能为空")})
	Result<Integer> batchDeleteByIdList(String entityId, List<Long> idList);

	/**
	 * 获取所有菜谱明细
	 *
	 * @param entityId
	 * @return
	 */
	@Validator({@Check(name = "entityId", adapter = NotBlank.class, message = "实体id不能为空")})
	Result<List<CookDetailBO>> selectByQuery(String entityId);

	/**
	 * 根据cookId获取口碑菜谱明细
	 *
	 * @param entityId
	 * @return
	 */
	@Validator({@Check(name = "entityId", adapter = NotBlank.class, message = "实体id不能为空"),
			@Check(name = "cookId", adapter = NotBlank.class, message = "菜谱id不能为空")})
	Result<List<CookDetailBO>> selectByCookId(String entityId, Long cookId);

	/**
	 * 获取单个菜谱详情
	 *
	 * @param entityId
	 * @param id
	 * @return
	 */
	@Validator({@Check(name = "entityId", adapter = NotBlank.class, message = "实体id不能为空"),
			@Check(name = "id", adapter = NotBlank.class, message = "菜谱明细id不能为空")})
	Result<CookDetailBO> queryById(String entityId, Long id);

	/**
	 * 订正数据使用
	 *
	 * @return
	 */
	Result<List<String>> queryEntityIdWihtFewCookDetail();


	/**
	 * 根据entityId清除有误数据
	 *
	 * @param entityIdList
	 * @return
	 */
	Result<Integer> batchDeleteByMenuIdList(String[] entityIdList);

	/**
	 * 查询有误订正数据
	 *
	 * @param entityId
	 * @param cookId
	 * @param menuId
	 */
	Result<Integer> queryCountByMenuIdAndCreateTime(String entityId, Long cookId, String menuId);

}
