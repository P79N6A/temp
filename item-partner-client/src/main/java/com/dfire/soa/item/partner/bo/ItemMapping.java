package com.dfire.soa.item.partner.bo;


import com.dfire.soa.item.partner.bo.base.BaseItemMapping;

/**
 * ItemMapping-扩展字段
 * Created by zhishi.
 */
public class ItemMapping extends BaseItemMapping {

    public ItemMapping() {
        super();
    }

    public ItemMapping(String entityId, String tpShopId, String platCode, Integer idType, String localId, String tpId) {
        super(entityId, tpShopId, platCode, idType, localId, tpId);
    }
}



