package com.dfire.soa.item.partner.koubei.handler;

import com.alibaba.fastjson.JSON;
import com.dfire.soa.item.constants.ShopConstants;
import com.dfire.soa.item.dto.UnitExtDto;
import com.dfire.soa.item.partner.constant.CommonConstant;
import com.dfire.soa.item.partner.koubei.KouBeiCheckUtil;
import com.dfire.soa.item.partner.koubei.KouBeiDeleteUtil;
import com.dfire.soa.item.service.IGetMenuService;
import com.twodfire.exception.BizException;
import com.twodfire.share.result.Result;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;


/**
 * 处理单位消息-口碑
 * Created by zhishi on 2018/5/9 0009.
 */
@Component
public class KoubeiUnitHandler {
    @Resource
    private KouBeiCheckUtil kouBeiCheckUtil;
    @Resource
    private IGetMenuService getMenuService;
    @Resource
    private KouBeiDeleteUtil kouBeiDeleteUtil;
    private static final Logger bizLog = LoggerFactory.getLogger(CommonConstant.BIZ_LOG);

    /**
     * 新增/修改单位处理
     * @param merchantId merchantId
     * @param unit 单位
     * @return boolean
     */
    public boolean insertOrUpdate(String merchantId, String shopId, UnitExtDto unit){
        String tpId = kouBeiCheckUtil.checkUnitId(merchantId, shopId, unit.getEntityId(), unit.getUnitId(), true, unit.getLastVer());
        return StringUtils.isNotBlank(tpId);
    }

    /**
     * 删除单位处理
     * @param merchantId merchantId
     * @param unit 单位
     * @return boolean
     */
    public boolean delete(String merchantId, String shopId, UnitExtDto unit){
        String entityId = unit.getEntityId();
        String localId = unit.getUnitId();

        //查询单位
        Result<List<UnitExtDto>> unitExtDtosResult = getMenuService.queryUnitExtV2(entityId, ShopConstants.INDUSTRY_RESTAURANT);
        if(!unitExtDtosResult.isSuccess()){
            bizLog.error("[kb_databack][error]getMenuService.findKindMenu(entityId, kindMenuId) failed. entityId: "+ JSON.toJSONString(entityId) +", unitId: "+ JSON.toJSONString(localId) +", unitExtDtosResult: "+ JSON.toJSONString(unitExtDtosResult));
            throw new BizException("[kb_databack][error]查询单位信息失败");
        }else if (CollectionUtils.isNotEmpty(unitExtDtosResult.getModel()) && unitExtDtosResult.getModel().stream().anyMatch(vo -> Objects.equals(vo.getUnitId(), unit.getUnitId()))){
            return false;
        }

        return kouBeiDeleteUtil.deleteUnitId(merchantId, shopId, unit);
    }

}
