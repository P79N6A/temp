package com.dfire.soa.item.partner.koubei.message;

import com.alibaba.fastjson.JSON;
import com.dfire.rest.util.common.util.OpenRestUtil;
import com.dfire.soa.item.bo.Spec;
import com.dfire.soa.item.partner.constant.CommonConstant;
import com.dfire.soa.item.partner.koubei.KouBeiCheckUtil;
import com.dfire.soa.item.partner.koubei.handler.KoubeiSpecHandler;
import com.dfire.soa.thirdbind.vo.ShopBindExtendFieldsVo;
import com.dfire.soa.thirdbind.vo.ShopBindVo;
import com.twodfire.async.message.client.consumer.support.ConsumerCallBack;
import com.twodfire.async.message.client.consumer.support.annotation.MessageTag;
import com.twodfire.async.message.client.to.AsyncMsg;
import com.twodfire.exception.BizException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;

/**
 * 处理规格
 * Created by heque on 2018/5/9 0009.
 */
@Component
@MessageTag(tag = {CommonConstant.Notify.SPEC_INSERT_TAG, CommonConstant.Notify.SPEC_UPDATE_TAG})
public class KoubeiSpecMsgHandler implements ConsumerCallBack {
    @Resource
    private KoubeiSpecHandler koubeiSkuHandler;
    @Resource
    private KouBeiCheckUtil kouBeiCheckUtil;

    /** 日志：业务 */
    private static final Logger bizLog = LoggerFactory.getLogger(CommonConstant.BIZ_LOG);


    @Override
    public boolean process(AsyncMsg msg) {
        Map<String,String> dataMap = null;
        String tag = null;
        try{
            tag = msg.getTag();
            dataMap = msg.getContent();
            // 控制消息的超时时间
            if(OpenRestUtil.isResend(msg.getStartDeliverTime(), CommonConstant.MQ_MESSAGE_EXPIRE_TIME)) {
                bizLog.info("[kb_databack][info] msg has been expired. tag: {}, msgId: {}", tag, msg.getMsgID());
                return true;
            }
            Spec specBefore = JSON.parseObject(dataMap.get("data_before"), Spec.class);
            Spec specAfter = JSON.parseObject(dataMap.get("data_after"), Spec.class);
            boolean isDeleteTag = Objects.equals(tag, CommonConstant.Notify.SPEC_UPDATE_TAG) && (specAfter == null || specAfter.getIsValid()==0);

            //绑定关系校验
            ShopBindVo shopBindVo = kouBeiCheckUtil.getShopBindVo(dataMap.get("entity_id"));
            if (shopBindVo==null || shopBindVo.getBindStatus()!=1 || StringUtils.isBlank(shopBindVo.getMerchantId()) || StringUtils.isBlank(shopBindVo.getShopId()) ||
                    (StringUtils.isNotBlank(shopBindVo.getExtendFields()) && Objects.equals(JSON.parseObject(shopBindVo.getExtendFields(), ShopBindExtendFieldsVo.class).getSyncStatus(), 3) && !isDeleteTag)){
                return true;
            }
            String merchantId = shopBindVo.getMerchantId();
            String shopId = shopBindVo.getShopId();

            //口碑-规格tag处理
            switch (tag){
                case CommonConstant.Notify.SPEC_INSERT_TAG://规格-新增
                return koubeiSkuHandler.pushkoubeiSpecAddAndUpdate(merchantId, shopId, specAfter);

                case CommonConstant.Notify.SPEC_UPDATE_TAG://规格-修改
                    if(specAfter != null && specAfter.getIsValid() == 1){//更新
                        return koubeiSkuHandler.pushkoubeiSpecAddAndUpdate(merchantId, shopId, specAfter);
                    }else if(specAfter != null && specAfter.getIsValid() == 0){//删除
                        return koubeiSkuHandler.pushkoubeiSpecDelete(merchantId, shopId, specAfter);
                    }else {
                        return koubeiSkuHandler.pushkoubeiSpecDelete(merchantId, shopId, specBefore);
                    }

            }
        }catch (BizException e) {
            bizLog.warn("[kb_databack]KoubeiSpecMsgHandler.process(msg) BizException. tag: " + JSON.toJSONString(tag) + ", dataMap:" + JSON.toJSONString(dataMap), e);
            return true;
        }catch (Exception e) {
            bizLog.error("[kb_databack]KoubeiSpecMsgHandler.process(msg) Exception. tag: " + JSON.toJSONString(tag) + ", dataMap:" + JSON.toJSONString(dataMap), e);
            return false;
        }
        return true;
    }

}
