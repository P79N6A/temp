package com.dfire.soa.item.partner.bo.base;

import java.io.Serializable;

/**
 * ItemMapping-基本字段
 * Created by zhishi.
 */
public class BasePartnerMapping implements Serializable{

	private static final long serialVersionUID = 7724712758671432278L;
	/**
	 * <code>主键</code>
	 */
	private Long id;
	/**
	 * <code>二维火的店铺id</code>
	 */
	private String entityId;
	/**
	 * <code>第三方的门店id</code>
	 */
	private String shopId;
	/**
	 * <code>本地的字典id</code>
	 */
	private String localId;
	/**
	 * <code>外部关联ID</code>
	 */
	private String outId;

	/**
	 * <code>关联类型:</code>
	 * @com.dfire.soa.item.partner.enums.EnumMappingType
	 */
	private String mpType;

	/**
	 * <code>扩展字段</code>
	 */
	private String ext;

	/**
	 * <code>0-无效; 1-有效</code>
	 */
	private Integer isValid;
	/**
	 * <code>操作时间</code>
	 */
	private Long opTime;
	/**
	 * <code>创建时间</code>
	 */
	private Long createTime;
	/**
	 * <code>最后更新的版本号</code>
	 */
	private Integer lastVer;

	public BasePartnerMapping() {

	}

	public BasePartnerMapping(String entityId, String shopId, String localId, String outId, String mpType) {
		this();
		this.entityId = entityId;
		this.shopId = shopId;
		this.localId = localId;
		this.outId = outId;
		this.mpType = mpType;
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

	public String getShopId() {
		return shopId;
	}

	public void setShopId(String shopId) {
		this.shopId = shopId;
	}

	public String getLocalId() {
		return localId;
	}

	public void setLocalId(String localId) {
		this.localId = localId;
	}

	public String getOutId() {
		return outId;
	}

	public void setOutId(String outId) {
		this.outId = outId;
	}

	public String getMpType() {
		return mpType;
	}

	public void setMpType(String mpType) {
		this.mpType = mpType;
	}

	public String getExt() {
		return ext;
	}

	public void setExt(String ext) {
		this.ext = ext;
	}

	public Integer getIsValid() {
		return isValid;
	}

	public void setIsValid(Integer isValid) {
		this.isValid = isValid;
	}

	public Long getOpTime() {
		return opTime;
	}

	public void setOpTime(Long opTime) {
		this.opTime = opTime;
	}

	public Long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

	public Integer getLastVer() {
		return lastVer;
	}

	public void setLastVer(Integer lastVer) {
		this.lastVer = lastVer;
	}
}

