package com.dfire.soa.item.partner.bo.base;

import com.alibaba.fastjson.JSON;
import com.dfire.soa.item.partner.bo.ItemMappingExt;

import java.io.Serializable;

/**
 * ItemMapping-基本字段
 * Created by zhishi.
 */
public class BaseItemMapping implements Serializable{
	private static final long serialVersionUID = 1L;
	
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
	private String tpShopId;
	/**
	 * <code>本地的字典id</code>
	 */
	private String localId;
	/**
	 * <code>通用检索字段.如id_type=7(sku)时,存的是specId</code>
	 */
	private String commonId;
	/**
	 * <code>第三方的字典id</code>
	 */
	private String tpId;
	/**
	 * <code>平台编号</code>
	 */
	private String platCode;
	/**
	 * <code>id类型:1-规格;2-单位; 3-菜品菜类;4-套餐组;5-菜谱;6-菜谱菜类;7-菜品;8-sku ;9-图片</code>
	 */
	private Integer idType;
	/**
	 * <code>同步状态:0:失败;1:成功</code>
	 */
	private Integer syncStatus;
	/**
	 * <code>同步结果</code>
	 */
	private String syncResult;
	/**
	 * <code>扩展字段（请使用itemMappingExt，如需扩展字段，请联系相关人员）</code>
	 */
	private String ext;
	private ItemMappingExt itemMappingExt;
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

	public BaseItemMapping() {
		this.itemMappingExt = new ItemMappingExt();
	}

	public BaseItemMapping(String entityId, String tpShopId, String platCode, Integer idType, String localId, String tpId) {
		this();
		this.entityId = entityId;
		this.tpShopId = tpShopId;
		this.platCode = platCode;
		this.idType = idType;
		this.localId = localId;
		this.tpId = tpId;
	}

	public void setId(Long value) {
		this.id = value;
	}
	
	public Long getId() {
		return this.id;
	}

	public void setEntityId(String value) {
		this.entityId = value;
	}
	
	public String getEntityId() {
		return this.entityId==null ? "" : this.entityId;
	}

	public void setTpShopId(String value) {
		this.tpShopId = value;
	}
	
	public String getTpShopId() {
		return this.tpShopId==null ? "" : this.tpShopId;
	}

	public void setLocalId(String value) {
		this.localId = value;
	}
	
	public String getLocalId() {
		return this.localId==null ? "" : this.localId;
	}

	public void setCommonId(String value) {
		this.commonId = value;
	}
	
	public String getCommonId() {
		return this.commonId==null ? "" : this.commonId;
	}

	public void setTpId(String value) {
		this.tpId = value;
	}
	
	public String getTpId() {
		return this.tpId==null ? "" : this.tpId;
	}

	public void setPlatCode(String value) {
		this.platCode = value;
	}
	
	public String getPlatCode() {
		return this.platCode==null ? "" : this.platCode;
	}

	public void setIdType(Integer value) {
		this.idType = value;
	}
	
	public Integer getIdType() {
		return this.idType;
	}

	public void setSyncStatus(Integer value) {
		this.syncStatus = value;
	}
	
	public Integer getSyncStatus() {
		return this.syncStatus;
	}

	public void setSyncResult(String value) {
		this.syncResult = value;
	}
	
	public String getSyncResult() {
		return this.syncResult==null ? "" : this.syncResult;
	}

	private void setExt(String value) {
		if(null == value || "".equals(value)) {
			this.itemMappingExt = new ItemMappingExt();
			this.ext = value;
			return;
		}
		this.itemMappingExt = JSON.parseObject(value, ItemMappingExt.class);
		this.ext = value;
	}

	public String getExt() {
		return JSON.toJSONString(itemMappingExt);
	}

	public ItemMappingExt getItemMappingExt() {
		return itemMappingExt;
	}

	public void setItemMappingExt(ItemMappingExt itemMappingExt) {
		this.ext = JSON.toJSONString(itemMappingExt);
		this.itemMappingExt = itemMappingExt;
	}

	public void setIsValid(Integer value) {
		this.isValid = value;
	}
	
	public Integer getIsValid() {
		return this.isValid;
	}

	public void setOpTime(Long value) {
		this.opTime = value;
	}
	
	public Long getOpTime() {
		return this.opTime;
	}

	public void setCreateTime(Long value) {
		this.createTime = value;
	}
	
	public Long getCreateTime() {
		return this.createTime;
	}

	public void setLastVer(Integer value) {
		this.lastVer = value;
	}
	
	public Integer getLastVer() {
		return this.lastVer;
	}

}

