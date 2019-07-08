package com.dfire.soa.item.partner.koubei.handler;

import com.alibaba.fastjson.JSON;
import com.dfire.open.takeout.bo.kb.KbDish;
import com.dfire.soa.item.bo.Menu;
import com.dfire.soa.item.partner.constant.CommonConstant;
import com.dfire.soa.item.partner.koubei.KouBeiCheckUtil;
import com.dfire.soa.item.partner.koubei.KouBeiDeleteUtil;
import com.dfire.soa.item.service.IGetMenuService;
import com.twodfire.exception.BizException;
import com.twodfire.share.result.Result;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * Created by heque on 2018/5/9 0009.
 */
@Component
public class KoubeiDishHandler {
    @Resource
    private IGetMenuService getMenuService;
    @Resource
    private KouBeiCheckUtil kouBeiCheckUtil;
    @Resource
    private KouBeiDeleteUtil kouBeiDeleteUtil;
    private static final Logger bizLog = LoggerFactory.getLogger(CommonConstant.BIZ_LOG);

	/**
	 * /**
	 * 新增/修改菜品处理
	 *
	 * @param merchantId merchantId
	 * @param menuId     菜品Id
	 * @param entityId
	 * @param type       0:更新sku;1:不更新sku(商品加料，做法)
	 * @return
	 */
	public boolean pushkoubeiDishAddAndUpdate(String merchantId, String shopId, String menuId, String entityId, Integer type) {
		//同步菜品
		KbDish kbDish = new KbDish();//通用参数
		String dishId = kouBeiCheckUtil.checkDishId(merchantId, shopId, entityId, menuId, true, null, kbDish);
		if (StringUtils.isNotEmpty(dishId) && type != 1) {
			//同步菜品所有sku
			kouBeiCheckUtil.checkBatchSkuId(merchantId, shopId, entityId, menuId, true, null, kbDish);

			//同步菜谱明细
			kouBeiCheckUtil.checkDishCookMapping(merchantId, shopId, entityId, menuId);
		}

		return true;
	}

    /**
     * 删除菜品处理
     * @param shopId shopId
     * @param merchantId merchantId
     * @param menu 菜品
     * @return boolean
     */
    public boolean pushkoubeiDishDelete(String shopId, String merchantId, Menu menu){
        String entityId = menu.getEntityId();
        String menuId = menu.getId();

        //查询菜品
        Result<Menu> menuResult = getMenuService.findMenuAndProp(entityId, menuId);
        if(!menuResult.isSuccess()){
            bizLog.error("[kb_databack][error]getMenuService.findMenuAndProp(entityId, menuId) failed. entityId: "+ JSON.toJSONString(entityId) +", menuId: "+ JSON.toJSONString(menuId) +", menuResult: "+ JSON.toJSONString(menuResult));
            throw new BizException("[kb_databack][error]查询菜品信息失败");
        } else if(menuResult.getModel()!=null){
            return false;
        }

        //先刪除菜譜-菜品
        kouBeiDeleteUtil.deleteCookDish(shopId, merchantId, menu.getEntityId(), menu.getId());

        //删除默认sku
        String defaultSpecId = entityId + CommonConstant.KOUBEI_DEFAULT_SPEC_ID;
        kouBeiDeleteUtil.deleteSkuId(merchantId, shopId, entityId, menuId, defaultSpecId);

        //删除菜品
        return kouBeiDeleteUtil.deleteDishId(merchantId, shopId, menu);

    }

}