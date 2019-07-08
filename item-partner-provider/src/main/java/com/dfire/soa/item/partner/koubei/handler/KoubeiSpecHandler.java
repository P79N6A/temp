package com.dfire.soa.item.partner.koubei.handler;

import com.alibaba.fastjson.JSON;
import com.dfire.soa.item.bo.Spec;
import com.dfire.soa.item.partner.constant.CommonConstant;
import com.dfire.soa.item.partner.koubei.KouBeiCheckUtil;
import com.dfire.soa.item.partner.koubei.KouBeiDeleteUtil;
import com.dfire.soa.item.service.IGetSpecDetailService;
import com.twodfire.exception.BizException;
import com.twodfire.share.result.Result;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * Created by heque on 2018/5/9 0009.
 */
@Component
public class KoubeiSpecHandler {
    @Resource
    private KouBeiCheckUtil kouBeiCheckUtil;
    @Resource
    private IGetSpecDetailService getSpecDetailService;
    @Resource
    private KouBeiDeleteUtil kouBeiDeleteUtil;
    private static final Logger bizLog = LoggerFactory.getLogger(CommonConstant.BIZ_LOG);
    /**
     * 新增/修改规格
     * @param merchantId merchantId
     * @param spec
     * @return boolean
     */
    public boolean pushkoubeiSpecAddAndUpdate(String merchantId, String shopId, Spec spec){
        String tpId = kouBeiCheckUtil.checkSpecId(merchantId, shopId, spec.getId(), spec.getEntityId(), true,  spec.getLastVer());
        return StringUtils.isNotBlank(tpId);
    }

    /**
     * 删除单位处理
     * @param merchantId merchantId
     * @param spec
     * @return boolean
     */
    public boolean pushkoubeiSpecDelete(String merchantId, String shopId, Spec spec){
        String entityId = spec.getEntityId();

        //查询规格
        Result<List<Spec>> specListResult = getSpecDetailService.querySpecList(entityId);
        if(!specListResult.isSuccess()){
            bizLog.error("[kb_databack][error]getSpecDetailService.querySpecList(entityId) failed. entityId: "+ JSON.toJSONString(entityId) +", specListResult: "+ JSON.toJSONString(specListResult));
            throw new BizException("[kb_databack][error]查询规格信息失败");
        }else if(specListResult.getModel() != null && specListResult.getModel().stream().anyMatch(vo -> Objects.equals(vo.getId(), spec.getId()))){
            return false;
        }

        return kouBeiDeleteUtil.deleteSpecId(merchantId, shopId, spec);
    }


}
