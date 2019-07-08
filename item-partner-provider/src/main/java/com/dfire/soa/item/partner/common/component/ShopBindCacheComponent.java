package com.dfire.soa.item.partner.common.component;

import com.dfire.rest.util.common.constant.LogConstants;
import com.dfire.soa.item.partner.common.guava.BaseGuavaCache;
import com.dfire.soa.thirdbind.service.IShopBindService;
import com.dfire.soa.thirdbind.vo.ShopBindVo;
import com.twodfire.share.result.Result;
import com.twodfire.share.util.ResultUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created on 2018/9/4.
 *
 * @author <a href="mailto:maodou@2dfire.com">毛豆</a>
 */
@Component
public class ShopBindCacheComponent extends BaseGuavaCache<String, ShopBindVo> {

    /** 日志：业务 */
    private Logger bizLogger = LoggerFactory.getLogger(LogConstants.BIZ_LOG);

    @Resource
    private IShopBindService shopBindService;

    public static final String KEY_SHOP_BIND = "key_shop_bind:";

    public ShopBindVo loadData(String entityId, String source) {
        ShopBindVo shopBindVo = null;
        String key = KEY_SHOP_BIND + entityId;
        try {
            shopBindVo = cache.get(key, () -> {
                Result<ShopBindVo> result = shopBindService.getByEntityIdAndSource(entityId, source);
                if(ResultUtil.isModelNotNull(result)) {
                    return result.getModel();
                } else{
                    ShopBindVo shopBind = new ShopBindVo();
                    shopBind.setEntityId(entityId);
                    return shopBind;
                }
            });
        } catch (Exception e) {
            bizLogger.error("ShopBindCacheComponent Exception entityId:{}", entityId, e);
        }
        return shopBindVo;
    }

    public ShopBindVo loadData(String entityId) {
        ShopBindVo shopBindVo = null;
        try {
            String key = KEY_SHOP_BIND + entityId;
            shopBindVo = cache.get(key, () -> {
                Result<ShopBindVo> result = shopBindService.getAliShopBindByEntityId(entityId);
                if(ResultUtil.isModelNotNull(result)) {
                    return result.getModel();
                } else{
                    ShopBindVo shopBind = new ShopBindVo();
                    shopBind.setEntityId(entityId);
                    return shopBind;
                }
            });
        } catch (Exception e) {
            bizLogger.error("ShopBindCacheComponent Exception entityId:{}", entityId, e);
        }
        return shopBindVo;
    }

    public ShopBindVo loadData(String merchantId, String shopId, String source) {
        ShopBindVo shopBindVo = null;
        try {
            shopBindVo = cache.get(KEY_SHOP_BIND+merchantId+shopId+source, () -> {
                Result<ShopBindVo> result = shopBindService.getByMerchantIdAndShopId(merchantId,shopId, source);
                if(ResultUtil.isModelNotNull(result)) {
                    return result.getModel();
                } else{
                    ShopBindVo shopBind = new ShopBindVo();
                    shopBind.setMerchantId(merchantId);
                    shopBind.setShopId(shopId);
                    shopBind.setSource(source);
                    return shopBind;
                }
            });
        } catch (Exception e) {
            bizLogger.error("ShopBindCacheComponent Exception merchantId:{},shopId:{}", merchantId, shopId, e);
        }
        return shopBindVo;
    }
}
