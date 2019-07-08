package com.dfire.soa.item.partner.bo.query;

import com.dfire.soa.item.partner.constants.CommonConstants;
import lombok.Data;

/**
 * @Author: xiaoji
 * @Date: create on 2018/8/31
 * @Describle:
 */
@Data
public class CookQuery extends BaseQuery{
	/**
	 * 菜谱ID
	 */
	private Long id;

	/**
	 * 菜谱名称
	 */
	private String name;

	/**
	 * 门店编码
	 */
	private String entityId;

	/**
	 * 菜谱状态
	 * {@link com.dfire.soa.item.partner.constants.CommonConstants.Status}
	 */
	private int status;

	/**
	 * 菜谱渠道
	 * {@link com.dfire.soa.item.partner.enums.EnumCookType}
	 */
	private int type;

	/**
	 * 菜谱类型
	 * {@link com.dfire.soa.item.partner.enums.EnumCookSubType}
	 */
	private int subType;

	/**
	 * 数据是否有效
	 * {@link CommonConstants.IsValid}
	 */
	private Integer isValid = CommonConstants.IsValid.VALID;

	public CookQuery(String entityId) {
		this.entityId = entityId;
	}
}
