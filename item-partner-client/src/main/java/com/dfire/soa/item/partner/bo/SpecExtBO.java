package com.dfire.soa.item.partner.bo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: xiaoji
 * @Date: create on 2018/9/7
 * @Describle:
 */
@Data
public class SpecExtBO implements Serializable{

	/**
	 * 存放规格信息
	 */
	private List<SpecBO> specBOList;
}
