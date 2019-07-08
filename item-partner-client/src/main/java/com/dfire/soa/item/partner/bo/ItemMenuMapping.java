package com.dfire.soa.item.partner.bo;


import com.dfire.soa.item.partner.bo.base.BaseItemMenuMapping;

/**
 * ItemMenuMapping-扩展字段
 * Created by zhishi.
 */
public class ItemMenuMapping extends BaseItemMenuMapping {



    public ItemMenuMapping() {
        super();
    }

    public ItemMenuMapping(String entityId, String tpShopId, String platCode, String localItemId, String tpItemId, String localMenuId, String tpMenuId) {
        super(entityId, tpShopId, platCode, localItemId, tpItemId, localMenuId, tpMenuId);
    }

}



