package com.dfire.soa.item.partner.bo.query;

import java.util.List;

/**
 * 
 * Created by zhishi.
 */
public class ItemMenuMappingQuery extends BaseQuery{
	//字段名
	public static final String TABLE = "ItemMenuMapping";
	public static final String ID = "ID";
	public static final String ENTITY_ID = "ENTITY_ID";
	public static final String LOCAL_ITEM_ID = "LOCAL_ITEM_ID";
	public static final String TP_ITEM_ID = "TP_ITEM_ID";
	public static final String LOCAL_MENU_ID = "LOCAL_MENU_ID";
	public static final String TP_MENU_ID = "TP_MENU_ID";
	public static final String SYNC_STATUS = "SYNC_STATUS";
	public static final String SYNC_RESULT = "SYNC_RESULT";
	public static final String EXT = "EXT";
	public static final String PLAT_CODE = "PLAT_CODE";
	public static final String IS_VALID = "IS_VALID";
	public static final String OP_TIME = "OP_TIME";
	public static final String CREATE_TIME = "CREATE_TIME";
	public static final String LAST_VER = "LAST_VER";
	
	//字段信息
	/**
	 * <code>主键</code>
	 */
	private Long id;
	/**
	 * <code>二维火的店铺id(必填)</code>
	 */
	private String entityId;
	/**
	 * <code>第三方的门店id</code>
	 */
	private String tpShopId;
	/**
	 * <code>本地菜品id</code>
	 */
	private String localItemId;
	/**
	 * <code>第三方菜品id</code>
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
	 * <code>扩展字段</code>
	 */
	private String ext;
	/**
	 * <code>平台编号</code>
	 */
	private String platCode;
	/**
	 * <code>0-无效; 1-有效</code>
	 */
	private Integer isValid = 1;
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
	/**
	 * <code>id列表</code>
	 */
	private List<String> idList;
	/**
	 * <code>localItemId列表</code>
	 */
	private List<String> localItemIdList;
	/**
	 * <code>tpItemId列表</code>
	 */
	private List<String> tpItemIdList;
	/**
	 * <code>localMenuId列表</code>
	 */
	private List<String> localMenuIdList;
	/**
	 * <code>tpMenuId列表</code>
	 */
	private List<String> tpMenuIdList;

	public ItemMenuMappingQuery() {
	}

	public ItemMenuMappingQuery(String entityId, String tpShopId, String platCode, String localItemId, String tpItemId, String localMenuId, String tpMenuId) {
		this.entityId = entityId;
		this.tpShopId = tpShopId;
		this.platCode = platCode;
		this.localItemId = localItemId;
		this.tpItemId = tpItemId;
		this.localMenuId = localMenuId;
		this.tpMenuId = tpMenuId;
	}
	
	//getter、setter方法
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
		return this.entityId;
	}
	
	public void setLocalItemId(String value) {
		this.localItemId = value;
	}
	
	public String getLocalItemId() {
		return this.localItemId;
	}
	
	public void setTpItemId(String value) {
		this.tpItemId = value;
	}
	
	public String getTpItemId() {
		return this.tpItemId;
	}
	
	public void setLocalMenuId(String value) {
		this.localMenuId = value;
	}
	
	public String getLocalMenuId() {
		return this.localMenuId;
	}
	
	public void setTpMenuId(String value) {
		this.tpMenuId = value;
	}
	
	public String getTpMenuId() {
		return this.tpMenuId;
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
		return this.syncResult;
	}
	
	public void setExt(String value) {
		this.ext = value;
	}
	
	public String getExt() {
		return this.ext;
	}
	
	public void setPlatCode(String value) {
		this.platCode = value;
	}
	
	public String getPlatCode() {
		return this.platCode;
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
	
	public List<String> getIdList() {
		return idList;
	}

	public void setIdList(List<String> idList) {
		this.idList = idList;
	}

	public String getTpShopId() {
		return tpShopId;
	}

	public void setTpShopId(String tpShopId) {
		this.tpShopId = tpShopId;
	}

	public List<String> getLocalItemIdList() {
		return localItemIdList;
	}

	public void setLocalItemIdList(List<String> localItemIdList) {
		this.localItemIdList = localItemIdList;
	}

	public List<String> getTpItemIdList() {
		return tpItemIdList;
	}

	public void setTpItemIdList(List<String> tpItemIdList) {
		this.tpItemIdList = tpItemIdList;
	}

	public List<String> getLocalMenuIdList() {
		return localMenuIdList;
	}

	public void setLocalMenuIdList(List<String> localMenuIdList) {
		this.localMenuIdList = localMenuIdList;
	}

	public List<String> getTpMenuIdList() {
		return tpMenuIdList;
	}

	public void setTpMenuIdList(List<String> tpMenuIdList) {
		this.tpMenuIdList = tpMenuIdList;
	}
}

