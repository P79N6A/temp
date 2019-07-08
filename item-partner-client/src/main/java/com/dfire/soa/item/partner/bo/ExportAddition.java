package com.dfire.soa.item.partner.bo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: xiaoji
 * @Date: create on 2018/11/14
 * @Describle:
 */
@Data
public class ExportAddition implements Serializable{

	/**
	 * entityId
	 */
	private String entityId;

	/**
	 * 有效无效
	 */
	private Integer isValid;
}
