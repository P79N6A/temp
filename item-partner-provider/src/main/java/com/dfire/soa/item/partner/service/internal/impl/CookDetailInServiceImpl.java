package com.dfire.soa.item.partner.service.internal.impl;

import com.alibaba.fastjson.JSONObject;
import com.dfire.soa.item.bo.Menu;
import com.dfire.soa.item.bo.MenuSpecDetail;
import com.dfire.soa.item.partner.bo.CookDetailBO;
import com.dfire.soa.item.partner.bo.SpecBO;
import com.dfire.soa.item.partner.bo.SpecExtBO;
import com.dfire.soa.item.partner.bo.query.CookDetailQuery;
import com.dfire.soa.item.partner.constant.CommonConstant;
import com.dfire.soa.item.partner.constants.CommonConstants;
import com.dfire.soa.item.partner.domain.CookDO;
import com.dfire.soa.item.partner.domain.CookDetailDO;
import com.dfire.soa.item.partner.enums.EnumCookType;
import com.dfire.soa.item.partner.manager.ICookDetailManager;
import com.dfire.soa.item.partner.manager.ICookManager;
import com.dfire.soa.item.partner.service.internal.ICookDetailInService;
import com.dfire.soa.item.partner.util.TransformUtil;
import com.dfire.soa.item.platform.bo.ItemAssembleGroupRelationBO;
import com.dfire.soa.item.platform.service.IItemAssembleGroupRelationService;
import com.dfire.soa.item.service.IGetMenuService;
import com.dfire.soa.item.service.IGetSpecDetailService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.twodfire.exception.BizException;
import com.twodfire.share.result.Result;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: xiaoji
 * @Date: create on 2018/8/27
 * @Describle:
 */
@Service("cookDetailInService")
public class CookDetailInServiceImpl implements ICookDetailInService {

	@Resource
	private ICookDetailManager cookDetailManager;

	@Resource
	private ICookManager cookManager;

	@Resource
	private IGetSpecDetailService getSpecDetailService;

	@Resource
	private IGetMenuService getMenuService;

	@Resource
	private IItemAssembleGroupRelationService itemAssembleGroupRelationService;

	@Override
	public Boolean insert(CookDetailBO cookDetailBO) {
		return cookDetailManager.insert(TransformUtil.toCookDetailDO(cookDetailBO));
	}

	@Override
	public Boolean updateById(CookDetailBO cookDetailBO) {
		return cookDetailManager.updateById(TransformUtil.toCookDetailDO(cookDetailBO));
	}

	@Override
	public Boolean deleteById(String entityId, Long id) {
		/*CookDetailDO cookDetailDO = cookDetailManager.queryById(entityId, id);
		List<String> existMenuIds = cookDetailManager.queryMenuIdsByCookId(entityId, cookDetailDO.getCookId());
		List<String> itemIdList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(existMenuIds)) {
			for (String menuId : existMenuIds) {
				List<Menu> list = getMenusInSuitMenu(entityId, menuId);
				for (Menu menu : list) {
					itemIdList.add(menu.getId());
				}
			}
			if (itemIdList.contains(cookDetailDO.getMenuId())) {
				throw new BizException(CommonConstant.ERROR_MESSAGE_1001, CommonConstant.ERROR_CODE_1001);
			}
			return cookDetailManager.deleteById(entityId, id);
		}
		return Boolean.TRUE;*/
		return cookDetailManager.deleteById(entityId, id);
	}

	@Override
	public Integer batchInsert(List<CookDetailBO> cookDetailBOList) {
		return cookDetailManager.batchInsert(TransformUtil.toCookDetailDOList(cookDetailBOList));
	}

	@Override
	public Integer addCookDetailMenus(String entityId, Long cookId, List<String> menuIdList) {
		Integer count = 0;
		CookDO cookDO = cookManager.queryById(entityId, cookId);
		if (null == cookDO) {
			throw new BizException("多菜单不存在!");
		}
		List<Menu> menuList = new ArrayList<>();
		for (String menuId : menuIdList) {
			Result<Menu> menu = getMenuService.queryMenu(entityId, menuId);
			if (null != menu.getModel()) {
				menuList.add(menu.getModel());
			}
		}

		if (CollectionUtils.isNotEmpty(menuList)) {
			//查找目前已经关联的菜id列表
			List<String> validMenuIds = Lists.newArrayList();
			List<String> existMenuIds = cookDetailManager.queryMenuIdsByCookId(entityId, cookId);

			Iterator<Menu> iterator = menuList.iterator();
			while (iterator.hasNext()) {
			 	Menu menu = iterator.next();
				if (existMenuIds.contains(menu.getId())) {
					validMenuIds.add(menu.getId());
					iterator.remove();
				}
			}


			//套餐内商品处理不处理
			/*List<Menu> suitMenus = getCommonMenuList(entityId, menuList);//所有套餐内商品的集合

			if (CollectionUtils.isNotEmpty(suitMenus)) {
				for (Menu menu : suitMenus) {
					//如果该商品不在新添加的商品列表和已经关联的商品列表中
					if (!validMenuIds.contains(menu.getId()) && !existMenuIds.contains(menu.getId())) {
						menuList.add(menu); //套餐内的商品自动添加到菜单中
					}
				}
			}*/

			//menuList去重
			LinkedHashSet<Menu> set = new LinkedHashSet<Menu>(menuList.size());
			set.addAll(menuList);
			menuList.clear();
			menuList.addAll(set);

			Map<String, List<SpecBO>> map = getSpecDetailMap(entityId, menuList);

			List<CookDetailBO> cookDetailBOList = Lists.newArrayList();
			//cookDetailBO组装
			for (Menu menu : menuList) {
				CookDetailBO cookDetailBO = TransformUtil.menuToCookDetailBO(menu);
				cookDetailBO.setCookId(cookId);
				cookDetailBO.setExt("");
				cookDetailBO.setUsePriceSwitch(CommonConstants.UsePriceSwitch.SAME);
				SpecExtBO specExtBO = new SpecExtBO();
				if (map != null) {
					specExtBO.setSpecBOList(map.get(menu.getId()));
				}
				cookDetailBO.setSpecExtBO(specExtBO);
				cookDetailBOList.add(cookDetailBO);
			}
			count += cookDetailManager.batchInsert(TransformUtil.toCookDetailDOList(cookDetailBOList));

		}

		return count;
	}

	/**
	 * 获取规格信息map
	 *
	 * @param entityId
	 * @param menuList
	 * @return
	 */
	public Map<String, List<SpecBO>> getSpecDetailMap(String entityId, List<Menu> menuList) {
		Map<String, List<SpecBO>> map = new HashMap<>();
		for (Menu menu : menuList) {
			Result<List<MenuSpecDetail>> result = getSpecDetailService.queryMenuSpecDetail(menu.getId(), entityId);
			List<SpecBO> specBOList = new ArrayList<>();
			if (null != result.getModel()) {
				for (MenuSpecDetail menuSpecDetail : result.getModel()) {
					SpecBO specBO = new SpecBO();
					specBO.setSpecId(menuSpecDetail.getSpecDetailId());
					specBO.setSpecPrice(menuSpecDetail.getPriceScale());
					specBOList.add(specBO);
				}
				if (specBOList.size() > 15) {
					throw new BizException("菜品[" + menu.getName() + "]下规格数量超过15个，请修改！");
				}
				map.put(menu.getId(), specBOList);
			}
		}
		return map;
	}

	@Override
	public Boolean deleteByMenuId(String entityId, String menuId) {
		return cookDetailManager.deleteByMenuId(entityId, menuId);
	}

	/**
	 * 获取给定商品列表中所有套餐所含的普通商品列表（已去重）
	 *
	 * @param entityId
	 * @param menus
	 * @return
	 */
	private List<Menu> getCommonMenuList(String entityId, List<Menu> menus) {
		List<Menu> suitMenus = Lists.newArrayList();//所有套餐内商品的集合
		if (menus == null) {
			return null;
		}
		for (Menu menu : menus) {
			if (menu.getIsInclude() == Menu.TYPE_SUIT) {
				suitMenus.addAll(getMenusInSuitMenu(entityId, menu.getId()));
			}
		}
		suitMenus = Lists.newArrayList(Sets.newHashSet(suitMenus));
		return suitMenus;
	}

	/**
	 * 找到单个套餐内所有商品集合(可能重复)
	 *
	 * @param entityId
	 * @param suitMenuId
	 * @return
	 */
	private List<Menu> getMenusInSuitMenu(String entityId, String suitMenuId) {
		List<Menu> suitMenus = Lists.newArrayList();//套餐内商品的集合
		Result<List<ItemAssembleGroupRelationBO>> suitMenuGroupVos = itemAssembleGroupRelationService.selectByItemAssembleId(entityId, suitMenuId);
		if (CollectionUtils.isNotEmpty(suitMenuGroupVos.getModel())) {
			for (ItemAssembleGroupRelationBO suitMenuGroupVo : suitMenuGroupVos.getModel()) {
				Result<Menu> menuResult = getMenuService.queryMenu(entityId, suitMenuGroupVo.getItemId());
				suitMenus.add(menuResult.getModel());

			}
		}
		return suitMenus;
	}

	@Override
	public Integer batchDeleteByIdList(String entityId, List<Long> idList) {
		/*//数据库中已有的商品
		CookDO cookDO = cookManager.selectByType(entityId, EnumCookType.KOUBEI.getCode());
		List<String> existMenuIds = cookDetailManager.queryMenuIdsByCookId(entityId, cookDO.getId());
		//传进来的商品列表
		CookDetailQuery cookDetailQuery = new CookDetailQuery(entityId);
		cookDetailQuery.setIdList(idList);
		cookDetailQuery.setUsePage(true);
		List<CookDetailDO> cookDetailDOList = cookDetailManager.selectByQuery(cookDetailQuery);
		List<String> menuIdList = cookDetailDOList.stream().map(cookDetailDO -> cookDetailDO.getMenuId()).collect(Collectors.toList());

		//获取剩余商品列表
		List<String> residualMenus = Lists.newArrayList();
		for (String menuId : existMenuIds) {
			if (!menuIdList.contains(menuId)) {
				residualMenus.add(menuId);
			}
		}

		List<String> itemIdList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(residualMenus)) {
			for (String id : residualMenus) {
				List<Menu> list = getMenusInSuitMenu(entityId, id);
				for (Menu menu : list) {
					itemIdList.add(menu.getId());
				}
			}
		}

		//不能删除的商品集合
		List<String> noDeleteMenuIds = Lists.newArrayList();
		for (String menuId : menuIdList) {
			if (itemIdList.contains(menuId)) {
				noDeleteMenuIds.add(menuId);
			}
		}
		List<String> canDeleteMenuIds = Lists.newArrayList(); //可以删除的菜单id列表
		for (String menuId : menuIdList) {
			if (!noDeleteMenuIds.contains(menuId)) {
				canDeleteMenuIds.add(menuId);
			}
		}
		return cookDetailManager.deleteByMenuIdList(entityId, canDeleteMenuIds);*/
		return cookDetailManager.batchDeleteByIdList(entityId, idList);
	}

	@Override
	public List<CookDetailBO> selectByQuery(CookDetailQuery cookDetailQuery) {
		return TransformUtil.toCookDetailBOList(cookDetailManager.selectByQuery(cookDetailQuery));
	}

	@Override
	public List<String> queryMenuIdsByCookId(String entityId, Long cookId) {
		return cookDetailManager.queryMenuIdsByCookId(entityId, cookId);
	}

	@Override
	public CookDetailBO queryById(String entityId, Long id) {
		return TransformUtil.toCookDetailBO(cookDetailManager.queryById(entityId, id));
	}

	public List<String> queryEntityIdWihtFewCookDetail() {
		return cookDetailManager.queryEntityIdWithFewCookDetail();
	}


}
