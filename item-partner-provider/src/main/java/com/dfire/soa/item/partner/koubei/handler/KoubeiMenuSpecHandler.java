package com.dfire.soa.item.partner.koubei.handler;

import com.alibaba.fastjson.JSON;
import com.dfire.soa.item.bo.MenuSpecDetail;
import com.dfire.soa.item.bo.SpecDetail;
import com.dfire.soa.item.partner.constant.CommonConstant;
import com.dfire.soa.item.partner.koubei.KouBeiCheckUtil;
import com.dfire.soa.item.partner.koubei.KouBeiDeleteUtil;
import com.dfire.soa.item.service.IGetSpecDetailService;
import com.twodfire.exception.BizException;
import com.twodfire.share.result.Result;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * Created by heque on 2018/5/16 0016.
 */
@Component
public class KoubeiMenuSpecHandler {
    @Resource
    private KouBeiCheckUtil kouBeiCheckUtil;
    @Resource
    private IGetSpecDetailService getSpecDetailService;
    @Resource
    private KouBeiDeleteUtil kouBeiDeleteUtil;


    private static final Logger bizLog = LoggerFactory.getLogger(CommonConstant.BIZ_LOG);

    /**
     * 新增/修改菜品规格关联
     * @param merchantId merchantId
     * @param menuSpecDetail 菜品规格
     * @return boolean
     */
    public boolean pushkoubeiMenuSpecAddAndUpdate(String merchantId, String shopId, MenuSpecDetail menuSpecDetail){
        String specDetailId = menuSpecDetail.getSpecDetailId();
        String entityId = menuSpecDetail.getEntityId();
        String menuId = menuSpecDetail.getMenuId();
        Integer lastVer = menuSpecDetail.getLastVer();
        String defaultSpecId = entityId + CommonConstant.KOUBEI_DEFAULT_SPEC_ID;
        //删除默认sku
        kouBeiDeleteUtil.deleteSkuId(merchantId, shopId, entityId, menuId, defaultSpecId);
        //同步sku
        kouBeiCheckUtil.checkSkuId(merchantId, shopId, entityId, menuId, specDetailId,true, lastVer, null);

        return true;
    }

    /**
     * 删除菜品规格关联（sku）
     * @param merchantId merchantId
     * @param menuSpecDetail 菜品规格
     * @return boolean
     */
    public boolean pushkoubeiMenuSpecDelete(String merchantId, String shopId, MenuSpecDetail menuSpecDetail){
        String specDetailId = menuSpecDetail.getSpecDetailId();
        String entityId = menuSpecDetail.getEntityId();
        String menuId = menuSpecDetail.getMenuId();
        String specId;

        //查询菜品-规格
        Result<List<MenuSpecDetail>> menuSpecDetailsResult = getSpecDetailService.queryMenuSpecDetail(menuId, entityId);
        if(!menuSpecDetailsResult.isSuccess()){
            bizLog.error("[kb_databack][error]getSpecDetailService.queryMenuSpecDetail(menuId, entityId) failed. menuId: "+ JSON.toJSONString(menuId)+", entityId: "+ JSON.toJSONString(entityId) + "menuSpecDetailsResult :"  + JSON.toJSONString(menuSpecDetailsResult));
            throw new BizException("[kb_databack][error]查询菜品规格详情失败！");
        }else if (menuSpecDetailsResult.getModel() != null && menuSpecDetailsResult.getModel().stream().anyMatch(vo -> Objects.equals(vo.getId(), menuSpecDetail.getId()))){
            return false;
        }

        //查询规格
        Result<SpecDetail> specDetailResult =  getSpecDetailService.queryAllSpecDetail(specDetailId,entityId);
        if(!specDetailResult.isSuccess() || specDetailResult.getModel() == null){
            bizLog.error("getSpecDetailService.queryAllSpecDetail(specDetailId, entityId) failed. specDetailId: {}, entityId: {}" , JSON.toJSONString(specDetailId), JSON.toJSONString(entityId));
            throw new BizException("[kb_databack][error]查询规格Id失败!");
        }else {
            specId = specDetailResult.getModel().getSpecId();
        }

        //先删除菜谱-sku关系
        kouBeiDeleteUtil.deleteCookSku(entityId, merchantId, entityId, menuId, specId);

        //删除sku
        boolean result = kouBeiDeleteUtil.deleteSkuId(merchantId, shopId, entityId, menuId, specId);
        if (!result){
            return false;
        }

        //添加默认sku
        Result<List<MenuSpecDetail>> menuSpecDetailList = getSpecDetailService.queryMenuSpecDetail(menuId, entityId);
        if(!menuSpecDetailList.isSuccess()){
            bizLog.error("[kb_databack][error]getSpecDetailService.queryMenuSpecDetail(menuId, entityId) is fail! menuId: " + JSON.toJSONString(menuId) + "entityId: " + JSON.toJSONString(entityId) + " menuSpecDetailList: " + JSON.toJSONString(menuSpecDetailList));
            throw new BizException("[kb_databack][error]查询菜品所有规格详情信息失败！");
        }else if(CollectionUtils.isEmpty(menuSpecDetailList.getModel())){ //菜品无其它规格sku
            kouBeiCheckUtil.checkSkuId(merchantId, shopId, entityId, menuId, null, false, null, null);
        }

        return true;
    }

}
