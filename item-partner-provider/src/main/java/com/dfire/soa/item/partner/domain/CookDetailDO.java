package com.dfire.soa.item.partner.domain;

import lombok.Data;

/**
 * @Author: xiaoji
 * @Date: create on 2018/8/27
 * @Describle:菜谱明细表
 */
@Data
public class CookDetailDO extends BaseDO {

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
	 * 基础价格
	 */
	private double price;

	/**
	 * 会员价格
	 */
	private double memberPrice;

	/**
	 * 存规格相关信息
	 */
	private String specExtra;

	/**
	 * 扩展字段
	 */
	private String ext;

	/**
	 * 是否和商品库价格保持一致
	 * {@link com.dfire.soa.item.partner.constants.CommonConstants.UsePriceSwitch}
	 */
	private int usePriceSwitch;
}
