package com.dfire.soa.item.partner.bo;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName BrandSyncResultBo
 * @Description
 * @Author GANSHU
 * @Date 2019/5/29 0029
 * @Version 1.0
 */
public class BrandSyncResultBo implements Serializable {

    /**
     * 店铺id  或者是 连锁的id
     */
    private String entityId;


    /**
     * 连锁的门店数量
     */
    private int shopCount;

    /**
     * 1-表示门店id
     * 2-表示连锁id
     */
    private int entityType;


    /**
     * 连锁门店
     */
    private List<BrandSyncResultBo> children;

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public int getShopCount() {
        return shopCount;
    }

    public void setShopCount(int shopCount) {
        this.shopCount = shopCount;
    }

    public int getEntityType() {
        return entityType;
    }

    public void setEntityType(int entityType) {
        this.entityType = entityType;
    }


    public List<BrandSyncResultBo> getChildren() {
        return children;
    }

    public void setChildren(List<BrandSyncResultBo> children) {
        this.children = children;
    }
}
