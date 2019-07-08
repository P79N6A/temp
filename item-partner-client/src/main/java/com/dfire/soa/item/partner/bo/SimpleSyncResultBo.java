package com.dfire.soa.item.partner.bo;

import java.io.Serializable;

/**
 * 同步结果
 * Created by zhishi on 2018/8/31 0029.
 */
public class SimpleSyncResultBo implements Serializable {
    private Long id;            //id
    private String entityId;    //entityId
    private String plateCode;   //平台编号，107：口碑
    private String tpShopId;    //第三方的门店id
    private Integer idType;     //id类型:1-规格;2-单位; 3-菜类;4-套餐组;5-菜谱;7-菜品
    private String localId;     //二维火id（对应idType）
    private Integer syncStatus; //同步状态 1:同步中 2:同步完成 3:同步失败
    private String syncResult;  //同步结果
    private Long opTime;        //最近同步时间

    public SimpleSyncResultBo() {
    }

    public SimpleSyncResultBo(Long id, String entityId, String plateCode, String tpShopId, Integer idType, String localId, Integer syncStatus, String syncResult, Long opTime) {
        this.id = id;
        this.entityId = entityId;
        this.plateCode = plateCode;
        this.tpShopId = tpShopId;
        this.idType = idType;
        this.localId = localId;
        this.syncStatus = syncStatus;
        this.syncResult = syncResult;
        this.opTime = opTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getPlateCode() {
        return plateCode;
    }

    public void setPlateCode(String plateCode) {
        this.plateCode = plateCode;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public Integer getIdType() {
        return idType;
    }

    public void setIdType(Integer idType) {
        this.idType = idType;
    }

    public Integer getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(Integer syncStatus) {
        this.syncStatus = syncStatus;
    }

    public String getSyncResult() {
        return syncResult;
    }

    public void setSyncResult(String syncResult) {
        this.syncResult = syncResult;
    }

    public String getTpShopId() {
        return tpShopId;
    }

    public void setTpShopId(String tpShopId) {
        this.tpShopId = tpShopId;
    }

    public Long getOpTime() {
        return opTime;
    }

    public void setOpTime(Long opTime) {
        this.opTime = opTime;
    }
}
