package com.dfire.soa.item.partner.util;

import java.math.BigDecimal;

/**
 * @Author: xiaoji
 * @Date: create on 2018/8/30
 * @Describle:
 */
public class ArithUtil {

	public static double add(double v1, double v2)
	{
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.add(b2).doubleValue();
	}

	/**
	 * 右补0
	 * @param str
	 * @param strLength
	 * @return
	 */
	public static String addZeroForNum(String str, int strLength) {
		int strLen = str.length();
		if (strLen < strLength) {
			StringBuilder sb = new StringBuilder();
			sb.append(str);
			while (strLen < strLength) {
				sb.append("0");//右补0
				str = sb.toString();
				strLen = str.length();
			}
		}
		return str;
	}


}
