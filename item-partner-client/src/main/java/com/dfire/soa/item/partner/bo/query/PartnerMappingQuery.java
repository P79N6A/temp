package com.dfire.soa.item.partner.bo.query;

/**
 * 
 * Created by zhishi.
 */
public class PartnerMappingQuery extends BaseQuery{

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

	public PartnerMappingQuery() {

	}

	public PartnerMappingQuery(String entityId, String shopId, String localId, String outId, String mpType) {
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
}

