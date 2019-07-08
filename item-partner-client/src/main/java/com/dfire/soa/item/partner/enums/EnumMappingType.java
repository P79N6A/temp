package com.dfire.soa.item.partner.enums;

/**
 * @Author: xiaoji
 * @Date: create on 2018/8/27
 * @Describle:
 */
public enum EnumMappingType {

	MAPP_PAY_USER_ID("MAPP_PAY_USER_ID", "映射支付用户ID");

	private String code;
	private String msg;

	EnumMappingType(String code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
