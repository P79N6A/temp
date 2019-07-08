package com.dfire.soa.item.partner.bo;

import java.io.Serializable;
import java.util.List;

/**
 * 扩展信息
 * Created by zhishi on 2018/5/21 0021.
 */
public class ItemMappingExt implements Serializable {
    /**
     * idType=3菜类时使用
     */
    private Integer level;//菜类层级

    /**
     * idType=7时菜品使用
     */
    private String goodsId;         //口碑goodsId;
    private String imgUrl;          //二维火图片地址
    private String imgUrlCode;      //口碑图片码

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getImgUrlCode() {
        return imgUrlCode;
    }

    public void setImgUrlCode(String imgUrlCode) {
        this.imgUrlCode = imgUrlCode;
    }
}
