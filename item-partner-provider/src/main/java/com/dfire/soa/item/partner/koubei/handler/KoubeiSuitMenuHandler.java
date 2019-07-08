package com.dfire.soa.item.partner.koubei.handler;

import com.alibaba.fastjson.JSON;
import com.dfire.open.takeout.bo.kb.KbDish;
import com.dfire.soa.item.bo.Menu;
import com.dfire.soa.item.bo.SuitMenuChange;
import com.dfire.soa.item.bo.SuitMenuDetail;
import com.dfire.soa.item.partner.bo.CookBO;
import com.dfire.soa.item.partner.bo.CookDetailBO;
import com.dfire.soa.item.partner.bo.SpecBO;
import com.dfire.soa.item.partner.bo.SpecExtBO;
import com.dfire.soa.item.partner.bo.query.CookDetailQuery;
import com.dfire.soa.item.partner.constant.CommonConstant;
import com.dfire.soa.item.partner.enums.EnumCookType;
import com.dfire.soa.item.partner.koubei.KouBeiCheckUtil;
import com.dfire.soa.item.partner.koubei.KouBeiDeleteUtil;
import com.dfire.soa.item.partner.service.internal.ICookDetailInService;
import com.dfire.soa.item.partner.service.internal.ICookInService;
import com.dfire.soa.item.partner.util.TransformUtil;
import com.dfire.soa.item.platform.bo.ItemAssembleGroupBO;
import com.dfire.soa.item.platform.service.IItemAssembleGroupService;
import com.dfire.soa.item.service.IGetMenuService;
import com.dfire.soa.item.service.IGetSuitMenuService;
import com.twodfire.exception.BizException;
import com.twodfire.share.result.Result;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 处理菜品组消息-口碑
 * Created by zhishi on 2018/5/9 0009.
 */
@Component
public class KoubeiSuitMenuHandler {
    @Resource
    private KouBeiCheckUtil kouBeiCheckUtil;
    @Resource
    private IGetSuitMenuService getSuitMenuService;
    @Resource
    private KouBeiDeleteUtil kouBeiDeleteUtil;
	@Resource
	private ICookDetailInService cookDetailInService;

	@Resource
	private ICookInService cookInService;

	@Resource
	private IGetMenuService getMenuService;

	@Resource
	private IItemAssembleGroupService itemAssembleGroupService;

    private static final Logger bizLog = LoggerFactory.getLogger(CommonConstant.BIZ_LOG);

    /**
     * 新增/修改菜品组处理
     * @param merchantId merchantId
     * @param suitMenuDetail 菜品组
     * @return boolean
     */
    public boolean insertOrUpdate(String merchantId, String shopId, SuitMenuDetail suitMenuDetail){
        //同步菜品组
        kouBeiCheckUtil.checkGroupId(merchantId, shopId, suitMenuDetail.getEntityId(), suitMenuDetail.getId(), true, suitMenuDetail.getLastVer());

        //同步菜品
        KbDish kbDish = new KbDish();//通用参数
        String dishId = kouBeiCheckUtil.checkDishId(merchantId, shopId, suitMenuDetail.getEntityId(), suitMenuDetail.getSuitMenuId(), true, null, kbDish);
        if(StringUtils.isNotEmpty(dishId)) {
            //同步菜品所有sku
            kouBeiCheckUtil.checkBatchSkuId(merchantId, shopId, suitMenuDetail.getEntityId(), suitMenuDetail.getSuitMenuId(), true, null, kbDish);

            //同步菜谱明细
            kouBeiCheckUtil.checkDishCookMapping(merchantId, shopId, suitMenuDetail.getEntityId(), suitMenuDetail.getSuitMenuId());
        }

        return true;
    }

    /**
     * 删除菜品组处理
     * @param merchantId merchantId
     * @param suitMenuDetail 菜品组
     * @return boolean
     */
    public boolean delete(String merchantId, String shopId, SuitMenuDetail suitMenuDetail){
        String entityId = suitMenuDetail.getEntityId();
        String localId = suitMenuDetail.getId();

        //查询菜品组信息（suitMenuDetailId->groupId）
        Result<SuitMenuDetail> suitMenuDetailResult = getSuitMenuService.findSuitMenuDetail(entityId, localId);
        if (!suitMenuDetailResult.isSuccess()){
            bizLog.error("[kb_databack][error]getSuitMenuService.findSuitMenuDetail(entityId, suitMenuDetailId) failed. entityId: "+ JSON.toJSONString(entityId)+ ", suitMenuDetailId: "+ JSON.toJSONString(localId)+ ", suitMenuDetailResult: "+ JSON.toJSONString(suitMenuDetailResult));
            throw new BizException("[kb_databack][error]查询菜品组信息失败");
        }else if(suitMenuDetailResult.getModel() != null){
            return false;
        }

        return kouBeiDeleteUtil.deleteGroupId(merchantId, shopId, suitMenuDetail);
    }

    /**
     * 新增/修改菜品组-可选菜处理
     * @param merchantId merchantId
     * @param suitMenuChange 菜品组-可选菜
     * @return boolean
     */
    public boolean insertOrUpdateBySuitMenuChange(String merchantId, String shopId, SuitMenuChange suitMenuChange){
        Result<SuitMenuDetail> suitMenuDetailResult = getSuitMenuService.findSuitMenuDetail(suitMenuChange.getEntityId(), suitMenuChange.getSuitMenuDetailId());
        if (!suitMenuDetailResult.isSuccess()){
            bizLog.error("[kb_databack][error]getSuitMenuService.findSuitMenuDetail(entityId, suitMenuDetailId) failed. entityId: "+ JSON.toJSONString(suitMenuChange.getEntityId())+ ", suitMenuDetailId: "+ JSON.toJSONString(suitMenuChange.getSuitMenuDetailId())+ ", suitMenuDetailResult: "+ JSON.toJSONString(suitMenuDetailResult));
            throw new BizException("[kb_databack][error]查询菜品组信息失败");
        }else if(suitMenuDetailResult.getModel()==null){
            return true;
        }
        return this.insertOrUpdate(merchantId, shopId, suitMenuDetailResult.getModel());
    }

	/**
	 * 新增套餐子菜加到口碑菜谱
	 *
	 * @param suitMenuChange
	 * @return
	 */
	public Boolean syncAddCookDetail(SuitMenuChange suitMenuChange) {
		String entityId = suitMenuChange.getEntityId();
		String menuId = suitMenuChange.getMenuId();
		CookBO cookBO = cookInService.selectByType(entityId, EnumCookType.KOUBEI.getCode());
		if (null == cookBO) {
			return Boolean.TRUE;
		}
		String itemAssembleGroupId = suitMenuChange.getSuitMenuDetailId();
		ItemAssembleGroupBO itemAssembleGroupBO = itemAssembleGroupService.selectById(entityId, itemAssembleGroupId).getModel();
		Menu menu = getMenuService.queryMenu(entityId, menuId).getModel();

		CookDetailQuery cookDetailQuery = new CookDetailQuery(entityId);
		cookDetailQuery.setMenuId(itemAssembleGroupBO.getItemAssembleId());
		cookDetailQuery.setCookId(cookBO.getId());
		cookDetailQuery.setIsValid(1);
		List<CookDetailBO> cookDetailBOList = cookDetailInService.selectByQuery(cookDetailQuery);

		bizLog.info("sync suitMenuChange. tag: {}, entityId: {}, cookId:{}, menuId: {},itemAssembleId:{}", "SUIT_MENU_CHANGE_INSERT", entityId, cookBO.getId(), menuId,itemAssembleGroupBO.getItemAssembleId());

		try {
			if (!CollectionUtils.isEmpty(cookDetailBOList)) {
				CookDetailBO cookDetailBO = createNewCookDetailBO(entityId, cookBO.getId(), menu);
				cookDetailInService.insert(cookDetailBO);
			}
		} catch (Exception e) {
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
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
		SpecExtBO specExtBO = new SpecExtBO();
		specExtBO.setSpecBOList(map.get(menu.getId()));
		cookDetailBO.setSpecExtBO(specExtBO);
		return cookDetailBO;
	}

}
