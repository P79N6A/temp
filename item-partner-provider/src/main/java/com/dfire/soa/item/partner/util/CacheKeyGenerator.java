package com.dfire.soa.item.partner.util;

import com.dfire.soa.item.partner.bo.ItemMenuMapping;
import com.dfire.soa.item.partner.constant.CacheConstants;

/**
 * Created by GanShu on 2018/10/29 0029.
 */
public class CacheKeyGenerator {


    /**
     * key的组成:  CacheConstants.Namespace.ITEM_MENU_MAPPING + entityId + tpShopId + platCode + localMenuId + localItemId
     * @param itemMenuMapping
     * @return
     */
    public static String generateItemMenuMappingKey(ItemMenuMapping itemMenuMapping) {
        String entityId = itemMenuMapping.getEntityId();
        String tpShopId = itemMenuMapping.getTpShopId();
        String platCode = itemMenuMapping.getPlatCode();
        String localMenuId = itemMenuMapping.getLocalMenuId();
        String localItemId = itemMenuMapping.getLocalItemId();
        return CacheConstants.Namespace.ITEM_MENU_MAPPING + entityId + tpShopId + platCode + localMenuId + localItemId;
    }


}
