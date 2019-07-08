package com.dfire.soa.item.partner.bo;

import com.dfire.soa.item.partner.constants.CommonConstants;
import lombok.Data;

import java.util.List;

/**
 * @Author: xiaoji
 * @Date: create on 2018/8/27
 * @Describle:
 */
@Data
public class CookDetailBO extends BaseBO {
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
	 * 存规格扩展字段
	 */
	private SpecExtBO specExtBO;

	/**
	 * 扩展字段
	 */
	private String ext;

	/**
	 * 是否和商品库价格保持一致
	 * {@link com.dfire.soa.item.partner.constants.CommonConstants.UsePriceSwitch}
	 */
	private int usePriceSwitch = CommonConstants.UsePriceSwitch.SAME;

}
