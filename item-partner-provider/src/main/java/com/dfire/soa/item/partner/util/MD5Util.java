/*
 * Copyright (C) 2009-2016 Hangzhou 2Dfire Technology Co., Ltd. All rights reserved
 */

package com.dfire.soa.item.partner.util;

import java.nio.charset.Charset;
import java.security.MessageDigest;

/**
 * The type Md 5 util.
 */
public class MD5Util {
    /**
     * charset
     */
    private static final String CHARSET = "UTF-8";

    /**
     * Used to build output as Hex
     */
    private static final char[] DIGITS_LOWER =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * Used to build output as Hex
     */
    private static final char[] DIGITS_UPPER =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * 字节转16进制
     *
     * @param data
     * @param toDigits
     * @return
     */
    private static String byteArrayToHexString(final byte[] data, final char[] toDigits) {
        StringBuffer resultSb = new StringBuffer();
        final int len = data.length;
        int low, high;
        for (int i = 0; i < len; i++) {
            high = (0xF0 & data[i]) >>> 4;
            low = 0x0F & data[i];
            resultSb.append(toDigits[high]);
            resultSb.append(toDigits[low]);
        }
        return resultSb.toString();
    }

    /**
     * md5
     *
     * @param origin      原始字符串
     * @param charsetName 编码
     * @param toLowerCase 返回数据是否小写
     * @return
     */
    public static String MD5Encode(String origin, String charsetName, final boolean toLowerCase) {
        byte[] data;
        if (charsetName == null || "".equals(charsetName)) {
            data = origin.getBytes();
        } else {
            data = origin.getBytes(Charset.forName(charsetName));
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return byteArrayToHexString(md.digest(data), toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * Md 5 encode string.默认UTF-8 ,默认小写
     *
     * @param origin the origin
     * @return the string
     */
    public static String MD5Encode(String origin) {
        return MD5Encode(origin, CHARSET, true);
    }

    /**
     * Md 5 encode string.默认小写
     *
     * @param origin      the origin
     * @param charsetName the charsetname
     * @return the string
     */
    public static String MD5Encode(String origin, String charsetName) {
        return MD5Encode(origin, charsetName, true);
    }

}
