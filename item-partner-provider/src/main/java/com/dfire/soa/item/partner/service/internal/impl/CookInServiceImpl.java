package com.dfire.soa.item.partner.service.internal.impl;

import com.alibaba.fastjson.JSONObject;
import com.dfire.soa.flame.UniqueIdGenerator;
import com.dfire.soa.item.bo.Menu;
import com.dfire.soa.item.bo.MenuSpecDetail;
import com.dfire.soa.item.bo.MultipleMenu;
import com.dfire.soa.item.bo.MultipleMenuElement;
import com.dfire.soa.item.enums.EnumMultiMenuType;
import com.dfire.soa.item.partner.bo.CookBO;
import com.dfire.soa.item.partner.bo.CookDetailBO;
import com.dfire.soa.item.partner.bo.SpecBO;
import com.dfire.soa.item.partner.bo.SpecExtBO;
import com.dfire.soa.item.partner.bo.query.CookDetailQuery;
import com.dfire.soa.item.partner.constant.CommonConstant;
import com.dfire.soa.item.partner.constants.CommonConstants;
import com.dfire.soa.item.partner.domain.CookDO;
import com.dfire.soa.item.partner.domain.CookDetailDO;
import com.dfire.soa.item.partner.enums.EnumCookSubType;
import com.dfire.soa.item.partner.enums.EnumCookType;
import com.dfire.soa.item.partner.manager.ICookDetailManager;
import com.dfire.soa.item.partner.manager.ICookManager;
import com.dfire.soa.item.partner.service.internal.ICookDetailInService;
import com.dfire.soa.item.partner.service.internal.ICookInService;
import com.dfire.soa.item.partner.util.TransformUtil;
import com.dfire.soa.item.service.IGetMenuService;
import com.dfire.soa.item.service.IMultiMenuReadService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: xiaoji
 * @Date: create on 2018/8/27
 * @Describle:
 */
@Service("cookInService")
public class CookInServiceImpl implements ICookInService {

	@Resource
	private ICookManager cookManager;

	@Resource
	private ICookDetailManager cookDetailManager;

	@Resource
	private IMultiMenuReadService multiMenuReadService;

	@Resource
	private IGetMenuService getMenuService;

	@Resource
	private ICookDetailInService cookDetailInService;
	private static final Logger log = LoggerFactory.getLogger(CommonConstant.BIZ_LOG);

	@Override
	public Boolean insert(CookBO cookBO) {
		Boolean result = cookManager.insert(TransformUtil.toCookDO(cookBO));
		return result;
	}

	@Override
	public Boolean updateById(CookBO cookBO) {
		Boolean result = cookManager.updateById(TransformUtil.toCookDO(cookBO));
		return result;
	}

	@Override
	public Boolean deleteById(String entityId, Long id) {
		Boolean result = cookManager.deleteById(entityId, id);
		return result;
	}

	@Override
	public Integer batchInsertByIdList(Map<String/*entityId*/, Long/*multipleMenuId*/> mapList) {
		Integer count = 0;

		for (Map.Entry map : mapList.entrySet()) {
			List<CookDetailBO> cookDetailBOList = new ArrayList<>();
			//没有口碑菜谱同时传进来0
			if (map.getValue().equals(0L)) {
				//2.没创建菜谱明细的生成口碑菜谱
				//2.1找堂食菜单上架且堂食可点的商品列表|多本堂食菜单情况下，商品库堂食上架且堂食可点的商品列表
				//2.2根据商品列表同步
				CookDO cookDO1 = new CookDO();
				cookDO1.setName("口碑菜谱");
				cookDO1.setEntityId(map.getKey().toString());
				cookDO1.setType(EnumCookType.KOUBEI.getCode());
				cookDO1.setSubType(EnumCookSubType.EAT_IN.getCode());
				cookDO1.setStatus(CommonConstants.Status.USING);
				cookDO1.setIsValid(CommonConstants.IsValid.VALID);
				Boolean flag = cookManager.insert(cookDO1);
				if (flag) {
					log.info("create new cook: entityId:{},cookId:{}", cookDO1.getEntityId(), cookDO1.getId());
				}
				List<Menu> menuList = findMenuListByEntityId(cookDO1.getEntityId());
				for (Menu menu : menuList) {
					CookDetailBO cookDetailBO = createNewCookDetailBO(cookDO1.getEntityId(), cookDO1.getId(), menu);
					cookDetailBOList.add(cookDetailBO);
				}
				count += cookDetailManager.batchInsert(TransformUtil.toCookDetailDOList(cookDetailBOList));
				if (count > 0) {
					log.info("finish insert new cookDetail: entityId:{}, cookId:{}, insertNum:{}", cookDO1.getEntityId(), cookDO1.getId(), count);
				}
				continue;
			}
			CookDO cookDO = cookManager.selectByType(String.valueOf(map.getKey()), 107);
			List<String> existMenuIdList = cookDetailManager.queryMenuIdsByCookId(map.getKey().toString(), cookDO.getId());
			//1.菜谱明细数量大于10，离开
			if (existMenuIdList.size() > 10 || null == cookDO) {
				continue;
			}

			/**
			 * 3.已经有菜谱明细的店先清空数据
			 * 3.1找堂食菜单上架且堂食可点的商品列表|多本堂食菜单情况下，商品库堂食上架且堂食可点的商品列表
			 * 3.2根据商品列表同步
			 */
			Integer num = cookDetailManager.deleteByMenuIdList(cookDO.getEntityId(), existMenuIdList);
			if (num <= 10) {
				log.info("finish clean cookDetail: entityId:{}, menuIdList:{}, deleteNum:{}", cookDO.getEntityId(), JSONObject.toJSON(existMenuIdList), num);
				List<Menu> menuList = findMenuListByEntityId(cookDO.getEntityId());
				for (Menu menu : menuList) {
					CookDetailBO cookDetailBO = createNewCookDetailBO(cookDO.getEntityId(), cookDO.getId(), menu);
					cookDetailBOList.add(cookDetailBO);
					log.info("restart insert cookDetail: entityId:{},cookDetailBO:{},", cookDO.getEntityId(), JSONObject.toJSON(cookDetailBO));
				}
				count += cookDetailManager.batchInsert(TransformUtil.toCookDetailDOList(cookDetailBOList));
				if (count > 0) {
					log.info("finish insert cookDetail: entityId:{},count:{},", cookDO.getEntityId(), count);
				}

			}
		}
		return count;
	}


	/**
	 * 查对应商品列表
	 *
	 * @param entityId
	 * @return
	 */
	private List<Menu> findMenuListByEntityId(String entityId) {
		List<Menu> menuList = new ArrayList<>();
		int[] type = new int[]{EnumMultiMenuType.DINE_IN.getCode()};
		List<MultipleMenu> multipleMenuList = multiMenuReadService.getMultipleMenuRemoveTakeOut(entityId, type, null).getModel();
		if (multipleMenuList.size() == 1 && !CollectionUtils.isEmpty(multipleMenuList) && multipleMenuList.get(0).getStatus() == 1) {
			//查堂食菜单
			List<MultipleMenuElement> multipleMenuElementList = multiMenuReadService.getMultipleMenuElementByMultipleMenuId(entityId, multipleMenuList.get(0).getId()).getModel();
			List<CookDetailBO> cookDetailBOList = TransformUtil.elementToCookDetailBOList(multipleMenuElementList);
			List<String> menuIdList = cookDetailBOList.stream().map(cookDetailBO -> cookDetailBO.getMenuId()).collect(Collectors.toList());
			for (String menuId : menuIdList) {
				Menu menu = getMenuService.findMenu(entityId, menuId).getModel();
				menuList.add(menu);
			}
		} else {
			//查商品库
			menuList = getMenuService.queryMenuList(entityId, -1).getModel();
		}
		//排除下架和堂食不可点的
		Iterator<Menu> iterator = menuList.iterator();
		while (iterator.hasNext()) {
			Menu menu = iterator.next();
			if (menu.getIsSelf() == 0 || menu.getIsReserve() == 0) {
				iterator.remove();
			}
		}
		return menuList;
	}

	/**
	 * 创建菜谱明细
	 *
	 * @param entityId
	 * @param cookId
	 * @param menu
	 * @return
	 */
	private CookDetailBO createNewCookDetailBO(String entityId, Long cookId, Menu menu) {
		CookDetailBO cookDetailBO = TransformUtil.menuToCookDetailBO(menu);
		cookDetailBO.setCookId(cookId);
		List<Menu> menuList = new ArrayList<>();
		menuList.add(menu);
		Map<String, List<SpecBO>> map = cookDetailInService.getSpecDetailMap(entityId, menuList);
		if (null != map) {
			SpecExtBO specExtBO = new SpecExtBO();
			specExtBO.setSpecBOList(map.get(menu.getId()));
			cookDetailBO.setSpecExtBO(specExtBO);
		}
		return cookDetailBO;
	}

	@Override
	public Integer batchDelete(Long time) {
		//int count = cookManager.batchDelete(time);
		int count = cookDetailManager.batchDelete(time);
		return count;
	}

	@Override
	public List<CookBO> selectByEntityId(String entityId) {
		return TransformUtil.toCookBOList(cookManager.selectByEntityId(entityId));
	}

	@Override
	public CookBO queryById(String entityId, Long id) {
		return TransformUtil.toCookBO(cookManager.queryById(entityId, id));
	}

	@Override
	public CookBO selectByType(String entityId, int type) {
		return TransformUtil.toCookBO(cookManager.selectByType(entityId, type));
	}

	@Override
	public List<String> getEntityIdList(long startTime, long endTime) {
		return cookManager.getEntityIdList(startTime, endTime);
	}

	@Override
	public void updateStatus() {
		cookManager.updateStatus();
	}

	@Override
	public Integer batchDeleteByMenuIdList(String[] entityIdList) {
		Integer count = 0;
		for (String entityId : entityIdList) {
			List<Long> deleteIdList = new ArrayList<>();
			CookDO cookDO = cookManager.selectByType(entityId, 107);
			List<String> menuIdList = cookDetailManager.queryMenuIdsByCookId(entityId, cookDO.getId());
			LinkedHashSet<String> set = new LinkedHashSet<String>(menuIdList.size());
			set.addAll(menuIdList);
			menuIdList.clear();
			menuIdList.addAll(set);
			log.info("query menuIdList: menuIdList:{}",JSONObject.toJSON(menuIdList));
			for (String menuId : menuIdList) {
				int num = queryCountByMenuIdAndCreateTime(entityId, cookDO.getId(), menuId);
				if (num > 1) {
					CookDetailQuery cookDetailQuery = new CookDetailQuery(entityId);
					cookDetailQuery.setCookId(cookDO.getId());
					cookDetailQuery.setMenuId(menuId);
					cookDetailQuery.setOrderBy("create_time desc");
					cookDetailQuery.setIsValid(1);
					cookDetailQuery.setUsePage(true);
					List<CookDetailDO> cookDetailDOList = cookDetailManager.selectByQuery(cookDetailQuery);
					cookDetailDOList.remove(cookDetailDOList.get(0));
					deleteIdList = cookDetailDOList.stream().map(cookDetailDO -> cookDetailDO.getId()).collect(Collectors.toList());
					log.info("query deleteIdList: entityId:{},idList:{}",entityId,JSONObject.toJSON(deleteIdList));
				}
			}
			count += cookDetailManager.batchDeleteByIdList(entityId, deleteIdList);
			log.info("finish delete cookDetailList: entityId:{},idList:{},count:{}",entityId,JSONObject.toJSON(deleteIdList),count);
			//count += cookDetailManager.batchDeleteByMenuIdListAndCreateTime(entityId,deleteMenuIdList);
		}

		return count;
	}

	@Override
	public Integer queryCountByMenuIdAndCreateTime(String entityId, Long cookId, String menuId) {
		return cookDetailManager.queryCountByMenuIdAndCreateTime(entityId, cookId, menuId);
	}


}
