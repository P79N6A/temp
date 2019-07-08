package com.dfire.soa.item.partner.bo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: xiaoji
 * @Date: create on 2018/9/3
 * @Describle:
 */
@Data
public class SpecBO implements Serializable {

	/**
	 * 规格id
	 */
	private String specId;

	/**
	 * 该规格下对应的商品价格
	 */
	private double specPrice;

}
