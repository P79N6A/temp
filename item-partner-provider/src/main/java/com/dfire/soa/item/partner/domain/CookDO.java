package com.dfire.soa.item.partner.domain;

import lombok.Data;

/**
 * @Author: xiaoji
 * @Date: create on 2018/8/27
 * @Describle:菜谱表
 */
@Data
public class CookDO extends BaseDO {

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

}
