package com.dfire.soa.item.partner.enums;

/**
 * @Author: xiaoji
 * @Date: create on 2018/8/27
 * @Describle:
 */
public enum EnumCookType {

	KOUBEI(107,"口碑菜谱");

	private int code;
	private String msg;

	EnumCookType(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
