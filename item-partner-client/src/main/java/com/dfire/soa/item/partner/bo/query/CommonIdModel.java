package com.dfire.soa.item.partner.bo.query;

import java.io.Serializable;

/**
 * 带commonId查询类
 * Created by zhishi on 2018/11/15 0015.
 */
public class CommonIdModel implements Serializable{
    /**
     * 本地id
     * (idType为8时：菜品id)
     */
    private String localId;
    /**
     * 本地id2
     * (idType为8时：规格id)
     */
    private String commonId;

    public CommonIdModel() {
    }

    public CommonIdModel(String localId, String commonId) {
        this.localId = localId;
        this.commonId = commonId;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public String getCommonId() {
        return commonId;
    }

    public void setCommonId(String commonId) {
        this.commonId = commonId;
    }
}
