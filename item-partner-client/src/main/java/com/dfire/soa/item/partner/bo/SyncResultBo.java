package com.dfire.soa.item.partner.bo;

import java.io.Serializable;

/**
 * Created by GanShu on 2018/8/29 0029.
 */
public class SyncResultBo implements Serializable {
    /**
     * 同步类型（1全量同步 2批量同步）
     */
    private int syncType;
    /**
     * 实体id
     */
    private String entityId;
    /**
     * 同步状态:
     * 1-同步中
     * 2-同步完成：只返回一次
     * 3-同步失败：批量同步只返回一次、全量同步始终返回
     * 4-未同步（当前无同步任务）
     * 5-已同步（当前无同步任务）
     */
    private int syncStatus;
    /**
     * 同步信息
     */
    private String msg;


    /**
     * 1-同步中时的数据
     */
    private int type;           //同步中类型， 1-规格; 2-单位; 3-菜品菜类; 4-套餐组;  7-菜品;


    /**
     * 2-同步完成时的数据
     */
    private int successCount;   //菜品同步成功数
    private int failCount;      //菜品同步失败数


    /**
     * 3-同步失败时的数据
     */
    private String businessId;  //同步失败的id， 类型依据type判断（1-规格; 2-单位; 3-菜品菜类; ）
    private String errorMsg;    //同步失败的信息

    public SyncResultBo() {
    }
    public SyncResultBo(int syncType) {
        this();
        this.syncType = syncType;
    }

    public int getSyncType() {
        return syncType;
    }

    public void setSyncType(int syncType) {
        this.syncType = syncType;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public int getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMsg() {
        switch (this.syncStatus){
            case 1://1-同步中
                return "同步中";
            case 2://2-同步完成：只返回一次
                return "同步完成";
            case 3://3-同步失败：批量同步只返回一次、全量同步始终返回
                return "同步失败";
            case 4://4-未同步（当前无同步任务）
                return "未同步";
            case 5://5-已同步（当前无同步任务）
                return "已同步";
        }
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }
}
