package com.dfire.soa.item.partner.bo.query;

import java.util.List;

/**
 * 
 * Created by zhishi.
 */
public class ItemMappingQuery extends BaseQuery{
	//字段名
	public static final String TABLE = "ItemMapping";
	public static final String ID = "ID";
	public static final String ENTITY_ID = "ENTITY_ID";
	public static final String TP_SHOP_ID = "TP_SHOP_ID";
	public static final String LOCAL_ID = "LOCAL_ID";
	public static final String COMMON_ID = "COMMON_ID";
	public static final String TP_ID = "TP_ID";
	public static final String PLAT_CODE = "PLAT_CODE";
	public static final String ID_TYPE = "ID_TYPE";
	public static final String SYNC_STATUS = "SYNC_STATUS";
	public static final String SYNC_RESULT = "SYNC_RESULT";
	public static final String EXT = "EXT";
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
	 * <code>id类型:1-规格;2-单位; 3-菜品菜类;4-套餐组;5-菜谱;6-菜谱菜类;7-sku;8-图片</code>
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
	 * <code>扩展字段</code>
	 */
	private String ext;
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
	 * <code>idType列表</code>
	 */
	private List<Integer> idTypeList;
	/**
	 * <code>localId列表</code>
	 */
	private List<String> localIdList;
	/**
	 * <code>commonId列表</code>
	 */
	private List<String> commonIdList;
	/**
	 * <code>tpId列表</code>
	 */
	private List<String> tpIdList;

	public ItemMappingQuery() {
	}

	public ItemMappingQuery(String entityId, String tpShopId, String platCode, Integer idType, String localId, String tpId) {
		this.entityId = entityId;
		this.tpShopId = tpShopId;
		this.platCode = platCode;
		this.idType = idType;
		this.localId = localId;
		this.tpId = tpId;
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
	
	public void setTpShopId(String value) {
		this.tpShopId = value;
	}
	
	public String getTpShopId() {
		return this.tpShopId;
	}
	
	public void setLocalId(String value) {
		this.localId = value;
	}
	
	public String getLocalId() {
		return this.localId;
	}
	
	public void setCommonId(String value) {
		this.commonId = value;
	}
	
	public String getCommonId() {
		return this.commonId;
	}
	
	public void setTpId(String value) {
		this.tpId = value;
	}
	
	public String getTpId() {
		return this.tpId;
	}
	
	public void setPlatCode(String value) {
		this.platCode = value;
	}
	
	public String getPlatCode() {
		return this.platCode;
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
		return this.syncResult;
	}
	
	public void setExt(String value) {
		this.ext = value;
	}
	
	public String getExt() {
		return this.ext;
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
	
	public List<String> getIdList() {
		return idList;
	}

	public void setIdList(List<String> idList) {
		this.idList = idList;
	}

	public List<String> getLocalIdList() {
		return localIdList;
	}

	public void setLocalIdList(List<String> localIdList) {
		this.localIdList = localIdList;
	}

	public List<String> getCommonIdList() {
		return commonIdList;
	}

	public void setCommonIdList(List<String> commonIdList) {
		this.commonIdList = commonIdList;
	}

	public List<String> getTpIdList() {
		return tpIdList;
	}

	public void setTpIdList(List<String> tpIdList) {
		this.tpIdList = tpIdList;
	}

	public List<Integer> getIdTypeList() {
		return idTypeList;
	}

	public void setIdTypeList(List<Integer> idTypeList) {
		this.idTypeList = idTypeList;
	}
}

