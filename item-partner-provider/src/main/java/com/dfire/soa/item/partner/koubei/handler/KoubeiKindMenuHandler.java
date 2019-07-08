package com.dfire.soa.item.partner.koubei.handler;

import com.alibaba.fastjson.JSON;
import com.dfire.soa.item.bo.KindMenu;
import com.dfire.soa.item.partner.constant.CommonConstant;
import com.dfire.soa.item.partner.koubei.KouBeiCheckUtil;
import com.dfire.soa.item.partner.koubei.KouBeiDeleteUtil;
import com.dfire.soa.item.service.IGetMenuService;
import com.twodfire.exception.BizException;
import com.twodfire.share.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * 处理菜类消息-口碑
 * Created by zhishi on 2018/5/9 0009.
 */
@Component
public class KoubeiKindMenuHandler {
    @Resource
    private IGetMenuService getMenuService;
    @Resource
    private KouBeiCheckUtil kouBeiCheckUtil;
    @Resource
    private KouBeiDeleteUtil kouBeiDeleteUtil;
    private static final Logger bizLog = LoggerFactory.getLogger(CommonConstant.BIZ_LOG);

    /**
     * 新增/修改菜类处理
     * @param merchantId merchantId
     * @param kindMenu 菜类
     * @return boolean
     */
    public boolean insertOrUpdate(String merchantId, String shopId, KindMenu kindMenu){
        kouBeiCheckUtil.checkKindMenuId(CommonConstant.ITEM_COOK_CATEGORY, merchantId, shopId, kindMenu.getEntityId(), kindMenu.getId(), true, kindMenu.getLastVer());
        kouBeiCheckUtil.checkKindMenuId(CommonConstant.ITEM_CATEGORY, merchantId, shopId, kindMenu.getEntityId(), kindMenu.getId(), true, kindMenu.getLastVer());
        return true;
    }

    /**
     * 删除菜类处理
     * @param merchantId merchantId
     * @param kindMenu 菜类
     * @return boolean
     */
    public boolean delete(String merchantId, String shopId, KindMenu kindMenu){
        //查询菜类
        Result<KindMenu> kindMenuResult = getMenuService.findKindMenu(kindMenu.getEntityId(), kindMenu.getId());
        if(!kindMenuResult.isSuccess()){
            bizLog.error("[kb_databack][error]getMenuService.findKindMenu(entityId, kindMenuId) failed. entityId: "+ JSON.toJSONString(kindMenu.getEntityId()) +", kindMenuId: "+ JSON.toJSONString(kindMenu.getId()) +", kindMenuResult: "+ JSON.toJSONString(kindMenuResult));
            throw new BizException("[kb_databack][error]查询菜类信息失败");
        } else if(kindMenuResult.getModel()!=null){
            return false;
        }

        return kouBeiDeleteUtil.deleteKindMenuId(CommonConstant.ITEM_CATEGORY, merchantId, shopId, kindMenu) && kouBeiDeleteUtil.deleteKindMenuId(CommonConstant.ITEM_COOK_CATEGORY, merchantId, shopId, kindMenu);
    }
}
