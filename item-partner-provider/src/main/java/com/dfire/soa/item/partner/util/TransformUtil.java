package com.dfire.soa.item.partner.util;

import com.alibaba.fastjson.JSONObject;
import com.dfire.soa.item.bo.Menu;
import com.dfire.soa.item.bo.MultipleMenu;
import com.dfire.soa.item.bo.MultipleMenuElement;
import com.dfire.soa.item.partner.bo.CookBO;
import com.dfire.soa.item.partner.bo.CookDetailBO;
import com.dfire.soa.item.partner.bo.SpecBO;
import com.dfire.soa.item.partner.bo.SpecExtBO;
import com.dfire.soa.item.partner.constants.CommonConstants;
import com.dfire.soa.item.partner.domain.CookDO;
import com.dfire.soa.item.partner.domain.CookDetailDO;
import com.dfire.soa.item.partner.enums.EnumCookSubType;
import com.dfire.soa.item.partner.enums.EnumCookType;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author: xiaoji
 * @Date: create on 2018/8/27
 * @Describle:
 */
public class TransformUtil {

	public static CookDO toCookDO(CookBO cookBO) {
		if (cookBO == null) {
			return null;
		}
		CookDO cookDO = new CookDO();
		cookDO.setId(cookBO.getId());
		cookDO.setEntityId(cookBO.getEntityId());
		cookDO.setName(cookBO.getName());
		cookDO.setStatus(cookBO.getStatus());
		cookDO.setType(cookBO.getType());
		cookDO.setSubType(cookBO.getSubType());
		cookDO.setCreateTime(cookBO.getCreateTime());
		cookDO.setOpTime(cookBO.getOpTime());
		cookDO.setIsValid(cookBO.getIsValid());
		cookDO.setLastVer(cookBO.getLastVer());
		return cookDO;
	}

	public static CookBO toCookBO(CookDO cookDO) {
		if (cookDO == null) {
			return null;
		}
		CookBO cookBO = new CookBO();
		cookBO.setId(cookDO.getId());
		cookBO.setEntityId(cookDO.getEntityId());
		cookBO.setName(cookDO.getName());
		cookBO.setStatus(cookDO.getStatus());
		cookBO.setType(cookDO.getType());
		cookBO.setSubType(cookDO.getSubType());
		cookBO.setCreateTime(cookDO.getCreateTime());
		cookBO.setOpTime(cookDO.getOpTime());
		cookBO.setIsValid(cookDO.getIsValid());
		cookBO.setLastVer(cookDO.getLastVer());
		return cookBO;
	}

	public static List<CookDO> toCookDOList(List<CookBO> cookBOList) {
		List<CookDO> cookDOList = new ArrayList<>();
		if (CollectionUtils.isEmpty(cookBOList)) {
			return Collections.EMPTY_LIST;
		}
		for (CookBO cookBO : cookBOList) {
			CookDO cookDO = toCookDO(cookBO);
			cookDOList.add(cookDO);
		}
		return cookDOList;
	}

	public static List<CookBO> toCookBOList(List<CookDO> cookDOList) {
		List<CookBO> cookBOList = new ArrayList<>();
		if (CollectionUtils.isEmpty(cookDOList)) {
			return Collections.EMPTY_LIST;
		}
		for (CookDO cookDO : cookDOList) {
			CookBO cookBO = toCookBO(cookDO);
			cookBOList.add(cookBO);
		}
		return cookBOList;
	}

	public static CookDetailDO toCookDetailDO(CookDetailBO cookDetailBO) {
		if (cookDetailBO == null) {
			return null;
		}
		CookDetailDO cookDetailDO = new CookDetailDO();
		cookDetailDO.setId(cookDetailBO.getId());
		cookDetailDO.setEntityId(cookDetailBO.getEntityId());
		cookDetailDO.setCookId(cookDetailBO.getCookId());
		cookDetailDO.setMenuId(cookDetailBO.getMenuId());
		cookDetailDO.setPrice(cookDetailBO.getPrice());
		cookDetailDO.setMemberPrice(cookDetailBO.getMemberPrice());
		if (null != cookDetailBO.getSpecExtBO()) {
			List<SpecBO> specBOList = cookDetailBO.getSpecExtBO().getSpecBOList();
			cookDetailDO.setSpecExtra(JSONObject.toJSONString(specBOList));
		}else{
			cookDetailDO.setSpecExtra("");
		}
		cookDetailDO.setUsePriceSwitch(cookDetailBO.getUsePriceSwitch());
		cookDetailDO.setCreateTime(cookDetailBO.getCreateTime());
		cookDetailDO.setOpTime(cookDetailBO.getOpTime());
		cookDetailDO.setIsValid(cookDetailBO.getIsValid());
		cookDetailDO.setExt("");
		cookDetailDO.setLastVer(cookDetailBO.getLastVer());
		return cookDetailDO;
	}


	public static CookDetailBO toCookDetailBO(CookDetailDO cookDetailDO) {
		if (cookDetailDO == null) {
			return null;
		}
		CookDetailBO cookDetailBO = new CookDetailBO();
		cookDetailBO.setId(cookDetailDO.getId());
		cookDetailBO.setEntityId(cookDetailDO.getEntityId());
		cookDetailBO.setCookId(cookDetailDO.getCookId());
		cookDetailBO.setMenuId(cookDetailDO.getMenuId());
		cookDetailBO.setPrice(cookDetailDO.getPrice());
		cookDetailBO.setMemberPrice(cookDetailDO.getMemberPrice());
		SpecExtBO specExtBO = new SpecExtBO();
		List<SpecBO> list = JSONObject.parseArray(cookDetailDO.getSpecExtra(), SpecBO.class);
		specExtBO.setSpecBOList(list);
		cookDetailBO.setSpecExtBO(specExtBO);
		cookDetailBO.setCreateTime(cookDetailDO.getCreateTime());
		cookDetailBO.setOpTime(cookDetailDO.getOpTime());
		cookDetailBO.setUsePriceSwitch(cookDetailDO.getUsePriceSwitch());
		cookDetailBO.setIsValid(cookDetailDO.getIsValid());
		cookDetailBO.setLastVer(cookDetailDO.getLastVer());
		return cookDetailBO;
	}

	public static List<CookDetailBO> toCookDetailBOList(List<CookDetailDO> cookDetailDOList) {
		List<CookDetailBO> cookDetailBOList = new ArrayList<>();
		if (CollectionUtils.isEmpty(cookDetailDOList)) {
			return Collections.EMPTY_LIST;
		}
		for (CookDetailDO cookDetailDO : cookDetailDOList) {
			CookDetailBO cookDetailBO = toCookDetailBO(cookDetailDO);
			cookDetailBOList.add(cookDetailBO);
		}
		return cookDetailBOList;
	}

	public static List<CookDetailDO> toCookDetailDOList(List<CookDetailBO> cookDetailBOList) {
		List<CookDetailDO> cookDetailDOList = new ArrayList<>();
		if (CollectionUtils.isEmpty(cookDetailBOList)) {
			return Collections.EMPTY_LIST;
		}
		for (CookDetailBO cookDetailBO : cookDetailBOList) {
			CookDetailDO cookDetailDO = toCookDetailDO(cookDetailBO);
			cookDetailDOList.add(cookDetailDO);
		}
		return cookDetailDOList;
	}

	public static List<CookBO> multipleMenuToCookBOList(List<MultipleMenu> multipleMenuList) {
		if (CollectionUtils.isEmpty(multipleMenuList)) {
			return Collections.EMPTY_LIST;
		}
		List<CookBO> cookBOList = new ArrayList<>();
		for (MultipleMenu multipleMenu : multipleMenuList) {
			CookBO cookBO = multipleMenuToCookBO(multipleMenu);
			cookBOList.add(cookBO);
		}
		return cookBOList;
	}

	public static CookBO multipleMenuToCookBO(MultipleMenu multipleMenu) {
		CookBO cookBO = new CookBO();
		cookBO.setEntityId(multipleMenu.getEntityId());
		cookBO.setName(multipleMenu.getName());
		cookBO.setStatus(1);
		cookBO.setType(EnumCookType.KOUBEI.getCode());
		cookBO.setSubType(EnumCookSubType.EAT_IN.getCode());
		cookBO.setSubType(multipleMenu.getType());
		cookBO.setCreateTime(System.currentTimeMillis());
		cookBO.setOpTime(System.currentTimeMillis());
		cookBO.setIsValid(CommonConstants.IsValid.VALID);
		return cookBO;
	}

	public static CookDetailBO menuToCookDetailBO(Menu menu) {
		if (null == menu) {
			return null;
		}
		CookDetailBO cookDetailBO = new CookDetailBO();
		cookDetailBO.setMenuId(menu.getId());
		cookDetailBO.setPrice(menu.getPrice());
		cookDetailBO.setMemberPrice(menu.getMemberPrice());
		cookDetailBO.setExt("");
		cookDetailBO.setEntityId(menu.getEntityId());
		cookDetailBO.setCreateTime(System.currentTimeMillis());
		cookDetailBO.setOpTime(System.currentTimeMillis());
		cookDetailBO.setIsValid(CommonConstants.IsValid.VALID);
		return cookDetailBO;
	}


	public static List<CookDetailBO> elementToCookDetailBOList(List<MultipleMenuElement> multipleMenuElementList) {
		if (CollectionUtils.isEmpty(multipleMenuElementList)) {
			return Collections.EMPTY_LIST;
		}
		List<CookDetailBO> cookDetailBOList = new ArrayList<>();
		for (MultipleMenuElement multipleMenuElement : multipleMenuElementList) {
			CookDetailBO cookDetailBO = elementToCookDetailBO(multipleMenuElement);
			cookDetailBOList.add(cookDetailBO);
		}
		return cookDetailBOList;
	}

	public static CookDetailBO elementToCookDetailBO(MultipleMenuElement multipleMenuElement) {
		if (null == multipleMenuElement) {
			return null;
		}
		CookDetailBO cookDetailBO = new CookDetailBO();
		cookDetailBO.setEntityId(multipleMenuElement.getEntityId());
		cookDetailBO.setMenuId(multipleMenuElement.getMenuId());
		cookDetailBO.setPrice(multipleMenuElement.getPrice());
		cookDetailBO.setMemberPrice(multipleMenuElement.getMemberPrice());
		cookDetailBO.setUsePriceSwitch(multipleMenuElement.getUseDefaultPriceSwitch());
		cookDetailBO.setCreateTime(multipleMenuElement.getCreateTime());
		cookDetailBO.setOpTime(multipleMenuElement.getOpTime());
		cookDetailBO.setIsValid(Integer.valueOf(multipleMenuElement.getIsValid()));
		cookDetailBO.setLastVer(multipleMenuElement.getLastVer());
		cookDetailBO.setExt("");
		return cookDetailBO;
	}
}
