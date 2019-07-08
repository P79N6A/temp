package com.dfire.soa.item.partner.bo.query;

import com.dfire.soa.item.partner.constants.CommonConstants;
import lombok.Data;

import java.util.List;

/**
 * @Author: xiaoji
 * @Date: create on 2018/8/30
 * @Describle:
 */
@Data
public class CookDetailQuery extends BaseQuery {

	public static final String ORDER_CreateTime_Desc_Id_Desc = "create_time desc,id desc";

	/**
	 * 菜谱明细ID
	 */
	private Long id;

	/**
	 * cookID
	 */
	private Long cookId;

	/**
	 * 商品id
	 */
	private String menuId;

	/**
	 * 门店编码
	 */
	private String entityId;

	/**
	 * 主键list
	 */
	private List<Long> idList;

	/**
	 * menuIdList
	 */
	private List<String> menuIdList;

	/**
	 * 数据是否有效
	 * {@link CommonConstants.IsValid}
	 */
	private Integer isValid = CommonConstants.IsValid.VALID;

	public CookDetailQuery(String entityId) {
		this.entityId = entityId;
	}
}
