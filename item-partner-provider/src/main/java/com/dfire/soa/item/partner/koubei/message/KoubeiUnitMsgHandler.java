package com.dfire.soa.item.partner.koubei.message;

import com.alibaba.fastjson.JSON;
import com.dfire.rest.util.common.util.OpenRestUtil;
import com.dfire.soa.item.dto.UnitExtDto;
import com.dfire.soa.item.partner.constant.CommonConstant;
import com.dfire.soa.item.partner.koubei.KouBeiCheckUtil;
import com.dfire.soa.item.partner.koubei.handler.KoubeiUnitHandler;
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
import java.util.HashMap;
import java.util.Objects;

/**
 * 处理单位消息
 * Created by zhishi on 2018/5/11 0011.
 */
@Component
@MessageTag(tag = {CommonConstant.Notify.UNIT_DELETE_TAG, CommonConstant.Notify.UNIT_INSERT_TAG, CommonConstant.Notify.UNIT_UPDATE_TAG})
public class KoubeiUnitMsgHandler implements ConsumerCallBack {
    @Resource
    private KoubeiUnitHandler koubeiUnitHandler;
    @Resource
    private KouBeiCheckUtil kouBeiCheckUtil;
    private static final Logger bizLog = LoggerFactory.getLogger(CommonConstant.BIZ_LOG);

    @Override
    public boolean process(AsyncMsg msg) {
        HashMap<String, String> dataMap = null;
        String tag = null;
        try {
            tag = msg.getTag();
            dataMap = msg.getContent();
            // 控制消息的超时时间
            if(OpenRestUtil.isResend(msg.getStartDeliverTime(), CommonConstant.MQ_MESSAGE_EXPIRE_TIME)) {
                bizLog.info("[kb_databack][info] msg has been expired. tag: {}, msgId: {}", tag, msg.getMsgID());
                return true;
            }
            UnitExtDto unitAfter = JSON.parseObject(dataMap.get("data_after"), UnitExtDto.class);
            UnitExtDto unitBefore = JSON.parseObject(dataMap.get("data_before"), UnitExtDto.class);
            boolean isDeleteTag = Objects.equals(tag, CommonConstant.Notify.UNIT_DELETE_TAG) || (Objects.equals(tag, CommonConstant.Notify.UNIT_UPDATE_TAG) && unitAfter==null);

            //绑定关系校验
            ShopBindVo shopBindVo = kouBeiCheckUtil.getShopBindVo(dataMap.get("entity_id"));
            if (shopBindVo==null || shopBindVo.getBindStatus()!=1 || StringUtils.isBlank(shopBindVo.getMerchantId()) || StringUtils.isBlank(shopBindVo.getShopId()) ||
                    (StringUtils.isNotBlank(shopBindVo.getExtendFields()) && Objects.equals(JSON.parseObject(shopBindVo.getExtendFields(), ShopBindExtendFieldsVo.class).getSyncStatus(), 3) && !isDeleteTag)){
                return true;
            }
            String merchantId = shopBindVo.getMerchantId();
            String shopId = shopBindVo.getShopId();

            //口碑-单位tag处理
            switch (tag) {
                case CommonConstant.Notify.UNIT_INSERT_TAG://单位-新增
                    return koubeiUnitHandler.insertOrUpdate(merchantId, shopId, unitAfter);

                case CommonConstant.Notify.UNIT_UPDATE_TAG://单位-更新
                    if (unitAfter != null && unitAfter.getIsValid() == 1) {
                        return koubeiUnitHandler.insertOrUpdate(merchantId, shopId, unitAfter);
                    }else {
                        return koubeiUnitHandler.delete(merchantId, shopId, unitBefore);
                    }

                case CommonConstant.Notify.UNIT_DELETE_TAG://单位-删除
                    return koubeiUnitHandler.delete(merchantId, shopId, unitAfter);
            }
        }catch (BizException e) {
            bizLog.warn("[kb_databack]KoubeiUnitMsgHandle.process(msg) BizException. tag: " + JSON.toJSONString(tag) + ", dataMap:" + JSON.toJSONString(dataMap), e);
            return true;
        }catch (Exception e) {
            bizLog.error("[kb_databack]KoubeiUnitMsgHandle.process(msg) Exception. tag: " + JSON.toJSONString(tag) + ", dataMap:" + JSON.toJSONString(dataMap), e);
            return false;
        }
        return true;
    }
}
