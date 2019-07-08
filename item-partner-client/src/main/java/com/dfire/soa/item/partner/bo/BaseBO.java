package com.dfire.soa.item.partner.bo;

import com.dfire.soa.item.partner.constants.CommonConstants;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: xiaoji
 * @Date: create on 2018/8/27
 * @Describle:
 */
@Data
public class BaseBO implements Serializable {

	/**
	 * 数据是否有效
	 */
	private Integer isValid = CommonConstants.IsValid.VALID;

	/**
	 * 创建时间
	 */
	private Long createTime;

	/**
	 * 修改时间
	 */
	private Long opTime;

	/**
	 * 版本号
	 */
	private Integer lastVer = 0;
}
