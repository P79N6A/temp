package com.dfire.soa.item.partner.service;

import com.dfire.soa.item.partner.bo.CookBO;
import com.dfire.validator.annotation.Check;
import com.dfire.validator.annotation.Validator;
import com.dfire.validator.validator.NotBlank;
import com.dfire.validator.validator.NotNull;
import com.twodfire.share.result.Result;

import java.util.List;
import java.util.Map;

/**
 * @Author: xiaoji
 * @Date: create on 2018/8/27
 * @Describle:
 */
public interface ICookService {

	/**
	 * 新增
	 *
	 * @param cookBO
	 * @return
	 */
	@Validator({@Check(name = "cookBO", adapter = NotNull.class, message = "对象不能为空")})
	Result<Boolean> insert(CookBO cookBO);

	/**
	 * 修改(订正数据使用)
	 *
	 * @param entityId
	 * @return
	 */
	@Validator({@Check(name = "cookBO", adapter = NotNull.class, message = "对象不能为空")})
	Result<Boolean> updateById(String[] entityId);

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
	 * 根据id查询
	 *
	 * @param entityId
	 * @param id
	 * @return
	 */
	@Validator({@Check(name = "entityId", adapter = NotBlank.class, message = "实体id不能为空"),
			@Check(name = "id", adapter = NotBlank.class, message = "id不能为空")})
	Result<CookBO> selectById(String entityId, Long id);

	/**
	 * 根据type查询
	 *
	 * @param entityId
	 * @param type
	 * @return
	 */
	@Validator({@Check(name = "entityId", adapter = NotBlank.class, message = "实体id不能为空"),
			@Check(name = "id", adapter = NotBlank.class, message = "id不能为空")})
	Result<CookBO> selectByType(String entityId, int type);


	/**
	 * 根据idlist批量添加老店菜谱数据（订正数据使用）
	 *
	 * @param idList
	 * @return
	 */
	@Validator({@Check(name = "idList", adapter = NotBlank.class, message = "map不能为空")})
	Result<Integer> batchInsertByIdList(Map<String, Long> idList);

	/**
	 * 订正数据使用
	 * @param time
	 * @return
	 */
	@Validator({@Check(name = "time", adapter = NotBlank.class, message = "time不能为空")})
	Result batchDelete(Long time);

	/**
	 * 订正数据使用
	 * @return
	 */
	Result updateStatus();

	/**
	 * 订正数据使用
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	Result<List<String>> getEntityIdList(long startTime, long endTime);
}
