package com.dfire.soa.item.partner.enums;

/**
 * @Author: xiaoji
 * @Date: create on 2018/8/27
 * @Describle:
 */
public enum EnumCookSubType {
	EAT_IN((byte) 1, "堂食"), TAKE_OUT((byte) 2, "外卖");

	private byte code;
	private String msg;

	EnumCookSubType(byte code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public byte getCode() {
		return code;
	}

	public void setCode(byte code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
