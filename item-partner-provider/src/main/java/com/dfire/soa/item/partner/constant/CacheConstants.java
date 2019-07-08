package com.dfire.soa.item.partner.constant;

/**
 * 缓存相关常量定义
 * Created by xiaoji on 18/10/26.
 */
public interface CacheConstants {

    // 统一的缓存前缀
    String ITEM_PARTNER_PREFIX = "item_partner_";

    // 分隔符
    String SEPARATOR = "_";

    // 5分钟
    int ITEM_CACHE_BY_FIVE_MIN = 60 * 5;

    // 10分钟
    int ITEM_CACHE_BY_TEN_MIN = 60 * 10;

    // 缓存半小时
    int ITEM_CACHE_BY_HALF_AN_HOUR = 60 * 30;

    // 缓存12小时
    int ITEM_CACHE_BY_TWELVE_HOUR = 60 * 60 * 12;

    // 3天缓存
    int ITEM_CACHE_BY_THREE_DAYS = 60 * 60 * 24 * 2;




    /**
     * 命名空间前缀定义
     */
    interface Namespace {

        String COOK_DETAIL_PREFIX="cook_detail_";

        String ITEM_MENU_MAPPING = "ITEM_MENU_MAPPING_";

    }

}
