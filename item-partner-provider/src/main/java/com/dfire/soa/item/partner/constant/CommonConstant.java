package com.dfire.soa.item.partner.constant;

/**
 * Created by GanShu on 2018/8/29 0029.
 */
public class CommonConstant {

    public static final String ERROR = "ERROR";
    public static final String LONG_TIME = "LONG_TIME";
    public static final String ALERT_MONITOR = "ALERT_MONITOR";
    public static final String ROCKET_MQ = "ROCKET_MQ";
    public static final String BIZ_LOG = "BIZ_LOG";


    public static final byte ITEM_SPEC = 1;         // 规格
    public static final byte ITEM_UNIT = 2;         // 单位
    public static final byte ITEM_CATEGORY = 3;     // 菜品分类
    public static final byte ITEM_GROUP = 4;        // 套餐组
    public static final byte ITEM_COOK = 5;         // 菜谱
    public static final byte ITEM_COOK_CATEGORY = 6;// 菜谱分类
    public static final byte ITEM = 7;              // 菜品
    public static final byte ITEM_SKU = 8;          // 菜品sku
    public static final byte ADDITION = 9;     		// 加料
	public static final byte ITEM_ADDITION = 10;    // 菜品加料
	public static final int[] COOK_TYPES = {1,2};
    public static final int KOUBEI_PLATFORM = 107;  // 平台类型口碑
    public static final String SOURCE_FROM = "seconddfire";   // TODO: 2018/5/21 0021   先传seconddfire  这个字段目前强管控 还没放开 预计这周发布之后
    public static final int SLEEP_TIME = 300;  // 等待时间
    public static final long MQ_MESSAGE_EXPIRE_TIME = 60 * 1000L;  // 30秒
    public static final String KOUBEI_DEFAULT_SPEC_ID = "h3a4c5a901l3ab57b3580g58";
    public static final String KOUBEI_DEFAULT_COOK_CATEGORY_ID = "03a4c5a901l3ab57b3580g58";
    public static final String KOUBEI_DEFAULT_SPEC_NAME = "份";
    public static final String KOUBEI_ITEM_SYNC_RESULT = "koubei_item_sync_result:";                //全量同步结果
    public static final String KOUBEI_BATCH_ITEM_SYNC_RESULT = "koubei_batch_item_sync_result:";    //批量同步结果
    public static final String KOUBEI_BATCH_ITEM_SYNC_MAP = "koubei_batch_item_sync_map:";          //批量同步参数
    public static final String KOUBEI_COOK_DETAIL_SYNC_RESULT = "koubei_cook_detail_sync_result:";

    public static final String KOUBEI_COOK_NAME = "口碑菜谱";



    public static final String CHOOSE = "choose";
    public static final String FIXED = "fixed";
    public static final String NOT_CUR_PRICE_FLAG = "N";
    public static final String DISH = "dish";
    public static final String GROUP = "group";
    public static final String ADD = "add";
    public static final String MULTIPLY = "multiply";


    public static final String ERROR_CODE_1001 = "1001";
    public static final String ERROR_MESSAGE_1001 = "套餐内的商品不可单独被移除!";

    public static final String ERROR_MESSAGE_1002 = "添加菜谱失败，请重试";
    /**
     * item 消息TAG
     */
    public static class Notify {

        //菜品
        public static final String MENU_DELETE_TAG = "MENU_DELETE";
        public static final String MENU_INSERT_TAG = "MENU_INSERT";
        public static final String MENU_UPDATE_TAG = "MENU_UPDATE";
        //菜类
        public static final String KIND_MENU_INSERT_TAG = "KIND_MENU_INSERT";
        public static final String KIND_MENU_UPDATE_TAG = "KIND_MENU_UPDATE";
        public static final String KIND_MENU_DELETE_TAG = "KIND_MENU_DELETE";
        //规格
        public static final String SPEC_INSERT_TAG = "SPEC_INSERT";
        public static final String SPEC_UPDATE_TAG = "SPEC_UPDATE";
        //规格详情
        public static final String SPEC_DETAIL_INSERT_TAG = "SPEC_DETAIL_INSERT";
        public static final String SPEC_DETAIL_UPDATE_TAG = "SPEC_DETAIL_UPDATE";
        //菜品规格关联
        public static final String MENU_SPEC_DETAIL_INSERT_TAG = "MENU_SPEC_DETAIL_INSERT";
        public static final String MENU_SPEC_DETAIL_UPDATE_TAG = "MENU_SPEC_DETAIL_UPDATE";
        //单位
        public static final String UNIT_INSERT_TAG = "UNIT_INSERT";
        public static final String UNIT_UPDATE_TAG = "UNIT_UPDATE";
        public static final String UNIT_DELETE_TAG = "UNIT_DELETE";
        //菜谱_TAG
        public static final String COOK_INSERT_TAG = "COOK_INSERT";
        public static final String COOK_UPDATE_TAG = "COOK_UPDATE";
		public static final String COOK_DELETE_TAG = "COOK_DELETE";
        //多菜单和菜品的关联
        public static final String COOK_DETAIL_INSERT_TAG = "COOK_DETAIL_INSERT";
        public static final String COOK_DETAIL_UPDATE_TAG = "COOK_DETAIL_UPDATE";
        public static final String COOK_DETAIL_DELETE_TAG = "COOK_DETAIL_DELETE";
        //套餐子菜
        public static final String SUIT_MENU_CHANGE_INSERT_TAG = "SUIT_MENU_CHANGE_INSERT";
        public static final String SUIT_MENU_CHANGE_UPDATE_TAG = "SUIT_MENU_CHANGE_UPDATE";
        //套餐子菜分类
        public static final String SUIT_MENU_DETAIL_INSERT_TAG = "SUIT_MENU_DETAIL_INSERT";
        public static final String SUIT_MENU_DETAIL_UPDATE_TAG = "SUIT_MENU_DETAIL_UPDATE";
        //菜类加料关联
        public static final String KIND_MENU_ADDITION_INSERT_TAG = "KIND_MENU_ADDITION_INSERT";
        public static final String KIND_MENU_ADDITION_UPDATE_TAG = "KIND_MENU_ADDITION_UPDATE";
        //菜类加料关联
        public static final String MENU_ADDITION_INSERT_TAG = "MENU_ADDITION_INSERT";
        public static final String MENU_ADDITION_UPDATE_TAG = "MENU_ADDITION_UPDATE";
        //口味分类
        public static final String KIND_TASTE_INSERT_TAG = "KIND_TASTE_INSERT";
        public static final String KIND_TASTE_UPDATE_TAG = "KIND_TASTE_UPDATE";
        //口味
        public static final String TASTE_INSERT_TAG = "TASTE_INSERT";
        public static final String TASTE_UPDATE_TAG = "TASTE_UPDATE";
        //做法
        public static final String MAKE_INSERT_TAG = "MAKE_INSERT";
        public static final String MAKE_UPDATE_TAG = "MAKE_UPDATE";
        //菜品附件
        public static final String MENU_DETAIL_INSERT_TAG = "MENU_DETAIL_INSERT";
        public static final String MENU_DETAIL_UPDATE_TAG = "MENU_DETAIL_UPDATE";
        //菜品口味关联
        public static final String MENU_KIND_TASTE_INSERT_TAG = "MENU_KIND_TASTE_INSERT";
        public static final String MENU_KIND_TASTE_UPDATE_TAG = "MENU_KIND_TASTE_UPDATE";
        //菜品做法关联
        public static final String MENU_MAKE_INSERT_TAG = "MENU_MAKE_INSERT";
        public static final String MENU_MAKE_UPDATE_TAG = "MENU_MAKE_UPDATE";
        //商品扩展信息
        public static final String MENU_PROP_INSERT_TAG = "MENU_PROP_INSERT";
        public static final String MENU_PROP_UPDATE_TAG = "MENU_PROP_UPDATE";
        //分时菜价
        public static final String MENU_TIME_PRICE_INSERT_TAG = "MENU_TIME_PRICE_INSERT";
        public static final String MENU_TIME_PRICE_UPDATE_TAG = "MENU_TIME_PRICE_UPDATE";
        //分时菜
        public static final String MENU_TIME_INSERT_TAG = "MENU_TIME_INSERT";
        public static final String MENU_TIME_UPDATE_TAG = "MENU_TIME_UPDATE";
        //套餐扩展信息
        public static final String SUIT_MENU_PROP_INSERT_TAG = "SUIT_MENU_PROP_INSERT";
        public static final String SUIT_MENU_PROP_UPDATE_TAG = "SUIT_MENU_PROP_UPDATE";

        public static final String MENU_SELLOUT_TAG = "302";  //  本地收银
        public static final String MENU_SELLOUT_CLOUD_TAG = "305";  // 云收银


        public static final String DISH_SYNC_TAG = "DISH_SYNC";  // 口碑菜品同步结果
    }

}
