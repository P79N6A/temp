package com.dfire.soa.item.partner.bo.base;

import com.alibaba.fastjson.JSON;
import com.dfire.soa.item.partner.bo.ItemMappingExt;
import com.dfire.soa.item.partner.bo.ItemMenuMappingExt;

import java.io.Serializable;

/**
 * ItemMenuMapping-基本字段
 * Created by zhishi.
 */
public class BaseItemMenuMapping implements Serializable{
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
	 * <code>本地菜类id</code>
	 */
	private String localItemId;
	/**
	 * <code>第三方菜类id</code>
	 */
	private String tpItemId;
	/**
	 * <code>本地菜单id</code>
	 */
	private String localMenuId;
	/**
	 * <code>第三方菜单id</code>
	 */
	private String tpMenuId;
	/**
	 * <code>同步状态 0:失败 1:成功</code>
	 */
	private Integer syncStatus;
	/**
	 * <code>同步结果</code>
	 */
	private String syncResult;
	/**
	 * <code>扩展字段（请使用itemMenuMappingExt，如需扩展字段，请联系相关人员）</code>
	 */
	private String ext;
	private ItemMenuMappingExt itemMenuMappingExt;
	/**
	 * <code>平台编号</code>
	 */
	private String platCode;
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

	public BaseItemMenuMapping() {
		this.setItemMenuMappingExt(new ItemMenuMappingExt());
	}

	public BaseItemMenuMapping(String entityId, String tpShopId, String platCode, String localItemId, String tpItemId, String localMenuId, String tpMenuId) {
		this();
		this.entityId = entityId;
		this.tpShopId = tpShopId;
		this.platCode = platCode;
		this.localItemId = localItemId;
		this.tpItemId = tpItemId;
		this.localMenuId = localMenuId;
		this.tpMenuId = tpMenuId;
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

	public void setLocalItemId(String value) {
		this.localItemId = value;
	}
	
	public String getLocalItemId() {
		return this.localItemId==null ? "" : this.localItemId;
	}

	public void setTpItemId(String value) {
		this.tpItemId = value;
	}
	
	public String getTpItemId() {
		return this.tpItemId==null ? "" : this.tpItemId;
	}

	public void setLocalMenuId(String value) {
		this.localMenuId = value;
	}
	
	public String getLocalMenuId() {
		return this.localMenuId==null ? "" : this.localMenuId;
	}

	public void setTpMenuId(String value) {
		this.tpMenuId = value;
	}
	
	public String getTpMenuId() {
		return this.tpMenuId==null ? "" : this.tpMenuId;
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
		this.itemMenuMappingExt = JSON.parseObject(value, ItemMenuMappingExt.class);
		this.ext = value;
	}

	public String getExt() {
		return JSON.toJSONString(itemMenuMappingExt);
	}

	public ItemMenuMappingExt getItemMenuMappingExt() {
		return itemMenuMappingExt;
	}

	public void setItemMenuMappingExt(ItemMenuMappingExt itemMenuMappingExt) {
		this.ext = JSON.toJSONString(itemMenuMappingExt);
		this.itemMenuMappingExt = itemMenuMappingExt;
	}

	public void setPlatCode(String value) {
		this.platCode = value;
	}
	
	public String getPlatCode() {
		return this.platCode==null ? "" : this.platCode;
	}

	public Integer getIsValid() {
		return isValid;
	}

	public void setIsValid(Integer isValid) {
		this.isValid = isValid;
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

	public String getTpShopId() {
		return tpShopId==null ? "" : this.tpShopId;
	}

	public void setTpShopId(String tpShopId) {
		this.tpShopId = tpShopId;
	}

}

