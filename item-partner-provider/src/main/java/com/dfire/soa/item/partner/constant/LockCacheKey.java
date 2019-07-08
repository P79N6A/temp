package com.dfire.soa.item.partner.constant;


import com.dfire.soa.item.partner.util.ValidateUtil;

public class LockCacheKey extends AbstractCacheKey {

	private static final String ADD_MENU_SPEC_DETAIL_KEY = "add_koubei_menu_spec_detail_key";//添加口碑规格

	private static final String ADD_MENU_ADDITION_KEY = "add_koubei_menu_addition_key";//添加口碑加料

    public LockCacheKey() {
        super("lock");
    }

    public String builderAddMenuSpecDetailLock(String entityId,String menuId){
		ValidateUtil.validateConstantThrowException(entityId);
		sb.append(SPLITTER).append(ADD_MENU_SPEC_DETAIL_KEY).append(SPLITTER).append("entityId").append(SPLITTER).append("menuId").append(SPLITTER);
		sb.append(entityId).append(SPLITTER).append(menuId);
		return sb.toString();
    }

	public String builderAddMenuAdditionLock(String entityId,String menuId){
		ValidateUtil.validateConstantThrowException(entityId);
		sb.append(SPLITTER).append(ADD_MENU_ADDITION_KEY).append(SPLITTER).append("entityId").append(SPLITTER).append("menuId").append(SPLITTER);
		sb.append(entityId).append(SPLITTER).append(menuId);
		return sb.toString();
	}
}
