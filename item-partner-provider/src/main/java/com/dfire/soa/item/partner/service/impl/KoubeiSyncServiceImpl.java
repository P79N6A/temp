package com.dfire.soa.item.partner.service.impl;

import com.alibaba.fastjson.JSON;
import com.dfire.open.takeout.bo.kb.*;
import com.dfire.open.takeout.enumeration.KouBeiCookBizTypeEnum;
import com.dfire.open.takeout.enumeration.KouBeiDishBizTypeEnum;
import com.dfire.open.takeout.enumeration.KouBeiSyncTypeEnum;
import com.dfire.open.takeout.service.IKouBeiDishCookService;
import com.dfire.rest.util.common.exception.OpenApiException;
import com.dfire.soa.boss.whitelist.bo.WhiteList;
import com.dfire.soa.boss.whitelist.enums.EnumWhiteListType;
import com.dfire.soa.boss.whitelist.service.IWhiteListService;
import com.dfire.soa.item.bo.KindMenu;
import com.dfire.soa.item.bo.Menu;
import com.dfire.soa.item.bo.Spec;
import com.dfire.soa.item.bo.Taste;
import com.dfire.soa.item.constants.ShopConstants;
import com.dfire.soa.item.dto.UnitExtDto;
import com.dfire.soa.item.enums.EnumEntityType;
import com.dfire.soa.item.partner.bo.*;
import com.dfire.soa.item.partner.bo.query.CookDetailQuery;
import com.dfire.soa.item.partner.bo.query.ItemMappingQuery;
import com.dfire.soa.item.partner.bo.query.ItemMenuMappingQuery;
import com.dfire.soa.item.partner.constant.CommonConstant;
import com.dfire.soa.item.partner.constants.CommonConstants;
import com.dfire.soa.item.partner.constants.MessageTag;
import com.dfire.soa.item.partner.enums.EnumCookSubType;
import com.dfire.soa.item.partner.enums.EnumCookType;
import com.dfire.soa.item.partner.koubei.KouBeiCheckUtil;
import com.dfire.soa.item.partner.koubei.KouBeiDeleteUtil;
import com.dfire.soa.item.partner.koubei.handler.KoubeiCookDishHandler;
import com.dfire.soa.item.partner.koubei.handler.KoubeiDishSelloutHandler;
import com.dfire.soa.item.partner.koubei.service.IKoubeiSyncService;
import com.dfire.soa.item.partner.mapper.ExportAdditionMapper;
import com.dfire.soa.item.partner.rocketmq.IItemRmqService;
import com.dfire.soa.item.partner.service.internal.ICookDetailInService;
import com.dfire.soa.item.partner.service.internal.ICookInService;
import com.dfire.soa.item.partner.service.internal.IItemMappingService;
import com.dfire.soa.item.partner.service.internal.IItemMenuMappingService;
import com.dfire.soa.item.query.KindMenuQuery;
import com.dfire.soa.item.service.IGetAdditionService;
import com.dfire.soa.item.service.IGetMenuService;
import com.dfire.soa.item.service.IGetSpecDetailService;
import com.dfire.soa.item.service.IGetTasteService;
import com.dfire.soa.item.vo.KindAndTasteVo;
import com.dfire.soa.msstate.bo.MenuBalance;
import com.dfire.soa.msstate.query.MenuBalanceQuery;
import com.dfire.soa.msstate.service.IMenuBalanceClientService;
import com.dfire.soa.oplog.constant.OpFromEnum;
import com.dfire.soa.oplog.service.IFireLogClientAsyncService;
import com.dfire.soa.rest.service.IRestSeatService;
import com.dfire.soa.shop.bo.Shop;
import com.dfire.soa.shop.query.ShopQuery;
import com.dfire.soa.shop.service.IShopBindClientService;
import com.dfire.soa.shop.service.IShopClientService;
import com.dfire.soa.thirdbind.service.IDishObjectMappingService;
import com.dfire.soa.thirdbind.service.IShopBindService;
import com.dfire.soa.thirdbind.vo.ShopBindExtendFieldsVo;
import com.dfire.soa.thirdbind.vo.ShopBindVo;
import com.dfire.soa.turtle.pojo.Seat;
import com.dfire.soa.turtle.service.ISeatService;
import com.twodfire.exception.BizException;
import com.twodfire.redis.CodisService;
import com.twodfire.share.result.Result;
import com.twodfire.share.result.ResultSupport;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.dfire.rest.util.common.enumeration.TpTakeoutOpResultEn.*;

/**
 * 口碑数据同步service
 * Created by GanShu on 2018/8/30 0030.
 */
@Service
public class KoubeiSyncServiceImpl implements IKoubeiSyncService {

    /**
     * 日志：业务
     */
    private static Logger bizLogger = LoggerFactory.getLogger(CommonConstant.BIZ_LOG);

    @Resource
    private KoubeiCookDishHandler koubeiCookDishHandler;
    @Resource
    private CodisService codisService;
//    @Resource
//    private CodisService codisService;
    @Resource
    private IKouBeiDishCookService kouBeiDishCookService;
    @Resource
    private IItemMappingService itemMappingService;
    @Resource
    private ICookDetailInService cookDetailInService;
    @Resource
    private ICookInService cookInService;
    @Resource
    private IItemMenuMappingService itemMenuMappingService;
    @Resource
    private IGetMenuService getMenuService;
    @Resource
    private IGetSpecDetailService getSpecDetailService;
    @Resource
    private IFireLogClientAsyncService fireLogClientAsyncService;
    @Resource
    private KouBeiCheckUtil kouBeiCheckUtil;
    @Resource
    private KouBeiDeleteUtil kouBeiDeleteUtil;
    @Resource
    private IMenuBalanceClientService menuBalanceClientService;
    @Resource
    private KoubeiDishSelloutHandler koubeiDishSelloutHandler;
    @Resource
    private IItemRmqService itemRmqService;
    @Resource
    private IDishObjectMappingService dishObjectMappingService;
    @Resource
    private IGetAdditionService getAdditionService;
	@Resource
	private IShopBindClientService shopBindClientService;
	@Resource
	private IRestSeatService restSeatService;
	@Resource
	private ISeatService seatService;
	@Resource
    private IShopClientService shopClientService;
	@Resource
	private IShopBindService shopBindService;

    private static final String KOUBEI_ACCESS_TOKEN_KEY = "fengniao_access_token_key:";
    private static final String KOUBEI_BRAND_ENTITY = "koubei_brand_entity:";
    private String failMsg = "操作失败！请联系二维火客服，我们将会帮您解决。";

    @Async
    @Override
    public Result<SyncResultBo> itemSync(String entityId, String platCode) {
        Result<SyncResultBo> result = new ResultSupport<>();
        SyncResultBo syncResultBo = new SyncResultBo(1);//新版同步结果 1-全量同步 2-批量同步
        syncResultBo.setSyncStatus(1);                            //1-同步中 2-同步完成 3-同步失败 4-未同步 5-已同步
        syncResultBo.setType(1);                                  //1-规格; 2-单位; 3-菜品菜类; 4-套餐组; 5-菜谱; 6-菜谱菜类; 7-菜品;
        syncResultBo.setEntityId(entityId);                       //entityId
        result.setModel(syncResultBo);
        String merchantId;
        String shopId;
        String key1 = CommonConstant.KOUBEI_ITEM_SYNC_RESULT + platCode + entityId;//缓存同步结果的key
        ShopBindVo shopBindVo = null;
        try {

            // 绑定关系校验
            this.saveLog(entityId, "itemSync", "[itemSync-1]菜品全量同步开始！");

            //先加白名单
            try {
                List<String> entityIdList = new ArrayList<>();
                entityIdList.add(entityId);
                Map<String, Boolean> map = whiteListService.getWhiteListMap(entityIdList, EnumWhiteListType.ITEM_ADDITION).getModel();
                Result<Integer> cashVersion = shopBindClientService.getShopVersionByEntityId(entityId);
                if (cashVersion != null) {
                    if (cashVersion.getModel().intValue() >= 5810 && !map.get(entityId)) {
                        WhiteList whiteList = new WhiteList();
                        whiteList.setEntityId(entityId);
                        whiteList.setOpUserId("kbBinding");
                        whiteList.setEntityType(EnumEntityType.SHOP.getCode());
                        whiteList.setType(EnumWhiteListType.ITEM_ADDITION.getCode());
                        whiteList.setIsValid(Boolean.TRUE);
                        Result whiteResult = whiteListService.add2WhiteList(whiteList);
                        if (whiteResult.isSuccess()) {
                            bizLogger.info("添加白名单成功! entityId:{}", entityId);
                        } else {
                            bizLogger.error("add whiteList fail.entityId:{},reason:{}", entityId, whiteResult.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                bizLogger.error("add whitList error.", e);
            }

            codisService.setObject(key1, syncResultBo, 30*60);
            shopBindVo = kouBeiCheckUtil.getShopBindVo(entityId);
            if (shopBindVo==null || shopBindVo.getBindStatus()!=1 || StringUtils.isBlank(shopBindVo.getMerchantId()) || StringUtils.isBlank(shopBindVo.getShopId()) ){
                throw new BizException("[kb_databack][error]店铺未绑定口碑店，或关联状态为已关闭!");
            }
            merchantId = shopBindVo.getMerchantId();
            shopId = shopBindVo.getShopId();
            codisService.del(KOUBEI_ACCESS_TOKEN_KEY + merchantId);//清除token缓存
            this.saveLog(entityId, "itemSync", "[itemSync-1]菜品全量同步参数，entityId:"+ entityId +", merchantId:"+ merchantId + ", shopId: "+ shopId);


            //1同步规格
            Result<List<Spec>> specsResult = getSpecDetailService.querySpecList(entityId);
            if(!specsResult.isSuccess()){
                bizLogger.error("[kb_databack][error]getSpecDetailService.querySpecList(entityId) failed. entityId: "+ JSON.toJSONString(entityId) +", specsResult: "+ JSON.toJSONString(specsResult));
                throw new BizException("[kb_databack][error]查询规格信息失败");
            }else if(CollectionUtils.isNotEmpty(specsResult.getModel())){
                for(Spec spec : specsResult.getModel()){
                    syncResultBo.setBusinessId(spec.getId());
                    kouBeiCheckUtil.checkSpecId(merchantId, shopId, spec.getId(), entityId,true,null);
                }
            }
            this.saveLog(entityId, "itemSync", "[itemSync-2]同步规格完成！");

            //2同步单位
            syncResultBo.setType(2);
            codisService.setObject(key1, syncResultBo, 30*60);
            Result<List<UnitExtDto>> unitExtDtosResult = getMenuService.queryUnitExtV2(entityId, ShopConstants.INDUSTRY_RESTAURANT);
            if(!unitExtDtosResult.isSuccess()){
                bizLogger.error("[kb_databack][error]getMenuService.queryUnitExtV2(entityId, industry) failed. entityId: "+ JSON.toJSONString(entityId) +", industry: "+ JSON.toJSONString(ShopConstants.INDUSTRY_RESTAURANT) +", unitExtDtosResult: "+ JSON.toJSONString(unitExtDtosResult));
                throw new BizException("[kb_databack][error]查询店铺单位信息失败");
            }else if (CollectionUtils.isNotEmpty(unitExtDtosResult.getModel())){
                for (UnitExtDto unitExtDto : unitExtDtosResult.getModel()){
                    syncResultBo.setBusinessId(unitExtDto.getUnitId());
                    kouBeiCheckUtil.checkUnitId(merchantId, shopId, entityId, unitExtDto.getUnitId(), true, null);
                }
            }
            this.saveLog(entityId, "itemSync", "[itemSync-3]同步单位完成！");

			//9同步加料
			syncResultBo.setType(9);
			codisService.setObject(key1, syncResultBo, 30 * 60);
			Result<List<Menu>> menuListResult = getAdditionService.listAdditionAll(entityId);
			if (!menuListResult.isSuccess()) {
				bizLogger.error("[kb_databack][error]getAdditionService.listAdditionAll(entityId) failed. entityId: " + JSON.toJSONString(entityId) + ", menuListResult: " + JSON.toJSONString(menuListResult));
				throw new BizException("[kb_databack][error]查询店铺加料信息失败");
			} else if (CollectionUtils.isNotEmpty(menuListResult.getModel())) {
				for (Menu menu : menuListResult.getModel()) {
					syncResultBo.setBusinessId(menu.getId());
					kouBeiCheckUtil.checkAffiliateId(merchantId, shopId, menu.getId(), entityId, true, null);
				}
			}
			this.saveLog(entityId, "itemSync", "[itemSync-8]同步加料完成！");

            //3同步菜类
            syncResultBo.setType(3);
            codisService.setObject(key1, syncResultBo, 30*60);
            KindMenuQuery kindMenuQuery = new KindMenuQuery(entityId);
            kindMenuQuery.setPageSize(KindMenuQuery.MAX_PAGE_SIZE);
            kindMenuQuery.setIsIncludeNE(2);
            Result<List<KindMenu>> kindMenusResult = getMenuService.getKindMenuListByQuery(kindMenuQuery);
            if(!kindMenusResult.isSuccess()){
                bizLogger.error("[kb_databack][error]getMenuService.getKindMenus(entityId) failed. entityId: "+ JSON.toJSONString(entityId)+ ", kindMenusResult: "+ JSON.toJSONString(kindMenusResult));
                throw new BizException("[kb_databack][error]查询店铺菜类信息失败");
            }else if (CollectionUtils.isNotEmpty(kindMenusResult.getModel())){
                for (KindMenu kindMenu : kindMenusResult.getModel()){
                    syncResultBo.setBusinessId(kindMenu.getId());
                    kouBeiCheckUtil.checkKindMenuId(CommonConstant.ITEM_CATEGORY, merchantId, shopId, entityId, kindMenu.getId(), true, null);
                    kouBeiCheckUtil.checkKindMenuId(CommonConstant.ITEM_COOK_CATEGORY, merchantId, shopId, entityId, kindMenu.getId(), true, null);
                }
            }
            this.saveLog(entityId, "itemSync", "[itemSync-4]同步菜类完成！");

            //同步默认规格
            String defaultSpecId = entityId + CommonConstant.KOUBEI_DEFAULT_SPEC_ID;
            syncResultBo.setType(1);
            syncResultBo.setBusinessId(null);
            kouBeiCheckUtil.checkSpecId(merchantId, shopId, defaultSpecId, entityId, true, null);//同步默认规格
            this.saveLog(entityId, "itemSync", "[itemSync-5]同步默认规格完成！");

            //4同步菜品、菜品sku
            syncResultBo.setType(7);
            codisService.setObject(key1, syncResultBo, 30*60);
            int page = 1;
            int pageSize = 200;
            int count = 0;
            int failed = 0;
            StringBuilder errMsg = new StringBuilder();
            while (true) {
                Result<List<Menu>> menusResult = getMenuService.getMenus(entityId, page++, pageSize);
                List<Menu> menuList = menusResult.getModel();
                if (!menusResult.isSuccess()) {
                    bizLogger.error("[kb_databack][error]getMenuService.getMenus(entityId, page, pageSize) failed. entityId: " + JSON.toJSONString(entityId) + ", page: " + JSON.toJSONString(page) + ", pageSize: " + JSON.toJSONString(pageSize) + ", menusResult: " + JSON.toJSONString(menusResult));
                    throw new BizException("[kb_databack][error]查询店铺菜品信息失败");
                } else if (CollectionUtils.isNotEmpty(menuList)) {
                    for (Menu menu : menuList) {
                        try {
                            if(menu.getType() == 1) {  // type=1 表示预约商品
                                continue;
                            }
                            if(count%20==0){
                                this.saveLog(entityId, "itemSync", "[itemSync-6]正在同步菜品，已同步数:"+ count + ",目前失败数："+ failed);
                            }
                            bizLogger.info("[kb_databack][info]IKoubeiSyncService.itemSync(entityId). entityId:{} the {} dish", entityId, ++count);

                            //同步
                            KbDish kbDish = new KbDish();//通用参数
                            String dishId = kouBeiCheckUtil.checkDishId(merchantId, shopId, entityId, menu.getId(), true, null, kbDish);//菜品
                            if (StringUtils.isNotEmpty(dishId)) {
                                kouBeiCheckUtil.checkBatchSkuId(merchantId, shopId, entityId, menu.getId(), true, null, kbDish);//菜品sku
                            }
                        } catch (Exception e) {
                            bizLogger.error("[kb_databack][error][dish]IKoubeiSyncService.itemSync(entityId). entityId: {}, menu:{}", JSON.toJSONString(entityId), JSON.toJSON(menu), e);
                            failed++;
                            errMsg.append(failed).append(". \"").append(menu.getName()).append("\", 失败信息：").append(e.getMessage()).append("        ");
                        }
                    }
                }else {
                    break;
                }
            }
            syncResultBo.setSuccessCount(count - failed);
            syncResultBo.setFailCount(failed);
            this.saveLog(entityId, "itemSync", "[itemSync-6]同步菜品完成，总菜品数："+ count +", 失败数："+ failed +"。" + (failed==0 ? "" : "    以下展示失败的菜品信息：" + errMsg));

            //同步菜谱
            CookBO cookBO = cookInService.selectByType(entityId, EnumCookType.KOUBEI.getCode());
            if(cookBO == null) {
                cookBO = new CookBO();
                cookBO.setEntityId(entityId);
                cookBO.setName(CommonConstant.KOUBEI_COOK_NAME);
                cookBO.setStatus(CommonConstants.Status.USING);
                cookBO.setType(EnumCookType.KOUBEI.getCode());
                cookBO.setSubType(EnumCookSubType.EAT_IN.getCode());
                boolean success = cookInService.insert(cookBO);
                if(!success) {
                    throw new BizException("[kb_databack][error]生成口碑菜谱失败！");
                }
                cookBO = cookInService.selectByType(entityId, EnumCookType.KOUBEI.getCode());
            }
            koubeiCookDishHandler.syncCook(cookBO, merchantId, entityId, shopId);
            this.addCookAndDetail(entityId, merchantId, shopId, cookBO);
            this.saveLog(entityId, "itemSync", "[batchSync-7]同步菜谱完成！（菜品全量同步已结束）");

            //更新同步状态
            ShopBindExtendFieldsVo shopBindExtendFieldsVo = StringUtils.isBlank(shopBindVo.getExtendFields()) ? new ShopBindExtendFieldsVo() : JSON.parseObject(shopBindVo.getExtendFields(), ShopBindExtendFieldsVo.class);
            shopBindExtendFieldsVo.setFailedId(null);
            shopBindExtendFieldsVo.setFailedType(null);
            shopBindExtendFieldsVo.setSyncStatus(2);
            shopBindVo.setShopBindExtendFieldsVo(shopBindExtendFieldsVo);
            shopBindVo.setExtendFields(JSON.toJSONString(shopBindExtendFieldsVo));
            kouBeiCheckUtil.updateShopBindVo(shopBindVo);


            //同步完成
            syncResultBo.setSyncStatus(2);
            codisService.setObject(key1, syncResultBo, 30*60);
            itemRmqService.rocketMqTransmit("ITEM_SYNC", JSON.toJSON(syncResultBo));//发送消息通知

        } catch (BizException e) {
            bizLogger.error("[kb_databack][error]IKoubeiSyncService.itemSync(entityId) BizException. entityId: " + JSON.toJSONString(entityId), e);
            this.saveLog(entityId, "itemSync", "[itemSync-eror]菜品全量同步中断："+ e.getMessage());

            //保存全量同步状态
            if(shopBindVo!=null){
                ShopBindExtendFieldsVo shopBindExtendFieldsVo = StringUtils.isBlank(shopBindVo.getExtendFields()) ? new ShopBindExtendFieldsVo() : JSON.parseObject(shopBindVo.getExtendFields(), ShopBindExtendFieldsVo.class);
                shopBindExtendFieldsVo.setSyncStatus(3);
                shopBindExtendFieldsVo.setFailedType(syncResultBo.getType());
                shopBindExtendFieldsVo.setFailedId(syncResultBo.getBusinessId());
                shopBindVo.setShopBindExtendFieldsVo(shopBindExtendFieldsVo);
                shopBindVo.setExtendFields(JSON.toJSONString(shopBindExtendFieldsVo));
                kouBeiCheckUtil.updateShopBindVo(shopBindVo);
            }

            syncResultBo.setSyncStatus(3);
            syncResultBo.setErrorMsg(e.getMessage().contains("KOUBEI商品同步失败") ? failMsg : e.getMessage().replaceAll("\\[kb_databack\\]\\[error\\]", ""));
            codisService.setObject(key1, syncResultBo, 30*60);
            itemRmqService.rocketMqTransmit("ITEM_SYNC", JSON.toJSON(syncResultBo));//发送消息通知
            result.setSuccess(false);
            result.setMessage("服务器内部异常");
            return result;
        } catch (Exception e) {
            bizLogger.error("[kb_databack][error]IKoubeiSyncService.itemSync(entityId). entityId: " + JSON.toJSONString(entityId), e);
            this.saveLog(entityId, "itemSync", "[itemSync-eror]菜品全量同步中断："+ e.getMessage());

            //保存全量同步状态
            if(shopBindVo!=null){
                ShopBindExtendFieldsVo shopBindExtendFieldsVo = StringUtils.isBlank(shopBindVo.getExtendFields()) ? new ShopBindExtendFieldsVo() : JSON.parseObject(shopBindVo.getExtendFields(), ShopBindExtendFieldsVo.class);
                shopBindExtendFieldsVo.setSyncStatus(3);
                shopBindExtendFieldsVo.setFailedType(syncResultBo.getType());
                shopBindExtendFieldsVo.setFailedId(syncResultBo.getBusinessId());
                shopBindVo.setShopBindExtendFieldsVo(shopBindExtendFieldsVo);
                shopBindVo.setExtendFields(JSON.toJSONString(shopBindExtendFieldsVo));
                kouBeiCheckUtil.updateShopBindVo(shopBindVo);
            }

            syncResultBo.setSyncStatus(3);
            syncResultBo.setErrorMsg("服务器内部错误");
            codisService.setObject(key1, syncResultBo, 30*60);
            itemRmqService.rocketMqTransmit("ITEM_SYNC", JSON.toJSON(syncResultBo));//发送消息通知
            result.setSuccess(false);
            result.setMessage("服务器内部异常");
            return result;
        }

        //同步完成后，同步菜品估清情况
        this.selloutSync(entityId, platCode);

        try {
            Result<List<Seat>> seatListResult = seatService.querySeatList(entityId);
            if(!seatListResult.isSuccess() || seatListResult.getModel() == null) {
                bizLogger.error("[kb_databack] query seat failed or seat list is empty. entityId: {}, result: {}", entityId, JSON.toJSONString(seatListResult));
            } else {
                // 同步桌位信息
                Result<Boolean> syncSeatResult = restSeatService.pushSeatList(entityId, seatListResult.getModel());
                if(!syncSeatResult.isSuccess()) {
                    bizLogger.error("[kb_databack] sync seat failed. entityId: {}, syncSeatResult: {}", entityId, JSON.toJSONString(syncSeatResult));
                }
            }
        } catch (Exception e) {
            bizLogger.error("[kb_databack] sync seat failed. entityId: {}", entityId, e);
        }
        return result;
    }


    private void itemSyncForBrand(String brandEntityId, String entityId, String platCode) {
        String key = KOUBEI_BRAND_ENTITY + brandEntityId;
        this.itemSync(entityId, platCode);
        codisService.lpop(key);
    }

    @Async
    @Override
    public void brandItemSync(String brandEntityId, List<String> entityIdList, String platCode) {
        String key = KOUBEI_BRAND_ENTITY + brandEntityId;
        if(CollectionUtils.isEmpty(entityIdList)) {
            return;
        }
        try {
            this.saveLog(brandEntityId, "itemSync", "连锁商品下发同步开始");
            shopBindService.queryById(0);//
            for(String entityId : entityIdList) {
                codisService.rpush(key, 3600, entityId); // 缓存一小时
            }
            for(String entityId : entityIdList) {
                this.itemSyncForBrand(brandEntityId, entityId, platCode);
            }
            bizLogger.info("[kb_databack] brand item sync finished. brandEntityId: {}, entityIdList: {}, key: {}", brandEntityId, JSON.toJSONString(entityIdList), key);
            this.saveLog(brandEntityId, "itemSync", "连锁商品下发同步完成");
        } catch (Exception e) {
            bizLogger.error("[kb_databack] fail to sync brand item. brandEntityId: {}, entityIdList: {}, key: {}", brandEntityId, JSON.toJSONString(entityIdList), key, e);
        }
    }

    public Result<BrandSyncResultBo> getBrandItemSyncResult(String brandEntityId, String platCode) {
        Result<BrandSyncResultBo> result = new ResultSupport<>();
        BrandSyncResultBo brandSyncResultBo = new BrandSyncResultBo();
        brandSyncResultBo.setEntityId(brandEntityId);
        try {
            String key = KOUBEI_BRAND_ENTITY + brandEntityId;
            List<String> syncEntityList = codisService.lrange(key,0, -1);  //  获得所有的列表元素
            if(CollectionUtils.isNotEmpty(syncEntityList)) {
                brandSyncResultBo.setEntityType(2);
                result.setModel(brandSyncResultBo);
                return result;
            }
            ShopQuery shopQuery = new ShopQuery();
            shopQuery.setBrandEntityId(brandEntityId);
            Result<List<Shop>> shopListResult = shopClientService.getShopListByQuery(shopQuery);
            if(!shopListResult.isSuccess() || shopListResult.getModel() == null) {
                bizLogger.error("[kb_databack] fail to get brand entity. brandEntityId: {}, shopListResult: {}", brandEntityId, JSON.toJSONString(shopListResult));
                return new ResultSupport<>(error260);
            }
            List<String> entityIdList = new ArrayList<>();
            List<Shop> shopList = shopListResult.getModel();
            for(Shop shop : shopList) {
                entityIdList.add(shop.getEntityId());  //  连锁下的所有门店
            }
            Result<List<ShopBindVo>> shopBindVoListResult = shopBindService.getAliShopBindListByEntityIds(entityIdList);
            if(!shopBindVoListResult.isSuccess() || shopBindVoListResult.getModel() == null) {
                bizLogger.error("[kb_databack] fail to get shopBindVo. brandEntityId: {}, shopBindVoListResult: {}", brandEntityId, JSON.toJSONString(shopBindVoListResult));
                return new ResultSupport<>(error261);
            }
            List<ShopBindVo> shopBindVoList = shopBindVoListResult.getModel();
            List<String> entityIdList_ = new ArrayList<>();
            for(ShopBindVo shopBindVo : shopBindVoList) {
                entityIdList_.add(shopBindVo.getEntityId());
            }
            List<BrandSyncResultBo> brandSyncResultBoList = itemMappingService.batchQueryFailCount(platCode, entityIdList_);
            int shopCount = brandSyncResultBoList.size();
            for(BrandSyncResultBo brandSyncResultBo_ : brandSyncResultBoList) {
                brandSyncResultBo_.setEntityType(1);
            }
            brandSyncResultBo.setChildren(brandSyncResultBoList);
            brandSyncResultBo.setShopCount(shopCount);
            brandSyncResultBo.setEntityType(2);
            result.setModel(brandSyncResultBo);
            bizLogger.info("[kb_databack] brand item sync result. brandEntityId: {}, brandSyncResultBo: {} ", brandEntityId, JSON.toJSONString(brandSyncResultBo));
            return result;
        } catch (Exception e) {
            bizLogger.info("[kb_databack] fail to get brand entity. brandEntityId: {} ", brandEntityId, e);
            return new ResultSupport<>(SYSTEM_ERROR);
        }
    }

    @Override
    public Result<SyncResultBo> getItemSyncResult(String entityId, String platCode) {
        Result<SyncResultBo> result = new ResultSupport<>();
        String key1 = CommonConstant.KOUBEI_ITEM_SYNC_RESULT + platCode + entityId;
        try {
            //查询缓存
            SyncResultBo syncResult = (SyncResultBo)codisService.getObject(key1);
            if(syncResult!=null) {
                result.setModel(syncResult);
                if(syncResult.getSyncStatus()!=1) {
                    codisService.del(key1);
                }
                bizLogger.info("[kb_databack]getItemSyncResult(entityId, platCode) . result: "+ JSON.toJSONString(result));
                return result;
            }

            //检查同步信息（shopBindVo扩展字段）
            SyncResultBo syncResultBo = new SyncResultBo(1);
            syncResultBo.setEntityId(entityId);
            ShopBindVo shopBindVo = kouBeiCheckUtil.getShopBindVo(entityId);
            ShopBindExtendFieldsVo shopBindExtendFieldsVo = shopBindVo == null || StringUtils.isBlank(shopBindVo.getExtendFields()) ? null : JSON.parseObject(shopBindVo.getExtendFields(), ShopBindExtendFieldsVo.class);
            if (shopBindExtendFieldsVo==null || shopBindExtendFieldsVo.getSyncStatus()==null) { //未同步
                syncResultBo.setSyncStatus(4);
            }else if (shopBindExtendFieldsVo.getSyncStatus()==2) {//同步完成
                syncResultBo.setSyncStatus(5);//已同步
                //是否没有口碑菜谱
                List<CookBO> cookBOs = cookInService.selectByEntityId(entityId);
                if(CollectionUtils.isEmpty(cookBOs)){
                    syncResultBo.setSyncStatus(4);//未同步
                }
            } else if(shopBindExtendFieldsVo.getFailedType()==null && StringUtils.isBlank(shopBindExtendFieldsVo.getFailedId())) {
                syncResultBo.setSyncStatus(4);
            }else if(shopBindExtendFieldsVo.getFailedType()!=null && StringUtils.isNotBlank(shopBindExtendFieldsVo.getFailedId())) {
                ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopBindVo.getShopId(), platCode, shopBindExtendFieldsVo.getFailedType(), shopBindExtendFieldsVo.getFailedId(), null);
                itemMappingQuery.setSyncStatus(0);
                List<ItemMapping> itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
                if (CollectionUtils.isEmpty(itemMappings)) {
                    syncResultBo.setSyncStatus(4);
                } else {
                    //同步失败
                    syncResultBo.setSyncStatus(3);
                    syncResultBo.setType(shopBindExtendFieldsVo.getFailedType());
                    syncResultBo.setBusinessId(shopBindExtendFieldsVo.getFailedId());
                    syncResultBo.setErrorMsg(itemMappings.get(0).getSyncResult());
                }
            } else {
                syncResultBo.setSyncStatus(3);
                syncResultBo.setType(shopBindExtendFieldsVo.getFailedType()==null ? 1 : shopBindExtendFieldsVo.getFailedType());
                syncResultBo.setBusinessId(shopBindExtendFieldsVo.getFailedId());
            }
            result.setModel(syncResultBo);
        } catch (Exception e) {
            bizLogger.error("[kb_databack] fail to get item sync result. entityId: {}, platCode: {}", entityId, platCode, e);
            result.setSuccess(false);
            result.setMessage("服务器开小差了，请重试");
            return result;
        }

        return result;
    }

    @Override
    public Result<SyncResultBo> batchItemSync(String entityId, String platCode, Map<Integer, List<String>> map) {
        Result<SyncResultBo> result = new ResultSupport<>();
        SyncResultBo syncResultBo = new SyncResultBo(2);//1-全量同步 2-批量同步
        syncResultBo.setSyncStatus(1);//1-同步中 2-同步完成 3-同步失败
        syncResultBo.setEntityId(entityId);
        result.setModel(syncResultBo);
        String merchantId;
        String shopId;
        String key2 = CommonConstant.KOUBEI_BATCH_ITEM_SYNC_RESULT + platCode + entityId;//缓存同步结果的key
        String key2Params = CommonConstant.KOUBEI_BATCH_ITEM_SYNC_MAP + platCode + entityId;//缓存同步参数的key
        try {

            // 初始化处理-绑定关系校验
            this.saveLog(entityId, "batchItemSync", "[batchItemSync-1]菜品批量同步开始！");
            codisService.setObject(key2Params, map, 30*60);
            codisService.setObject(key2, syncResultBo, 30*60);
            ShopBindVo shopBindVo = kouBeiCheckUtil.getShopBindVo(entityId);
            if (shopBindVo==null || shopBindVo.getBindStatus()!=1 || StringUtils.isBlank(shopBindVo.getMerchantId()) || StringUtils.isBlank(shopBindVo.getShopId()) ){
                throw new BizException("店铺未绑定口碑店，或关联状态为已关闭!");
            }
            merchantId = shopBindVo.getMerchantId();
            shopId = shopBindVo.getShopId();
            this.saveLog(entityId, "batchItemSync", "[batchItemSync-2]菜品批量同步参数，entityId:"+ entityId +", merchantId:"+ merchantId + ", shopId: "+ shopId);

            //查询菜品
            Map<String, Menu> menuMap = new HashMap<>();
            if(map!=null && CollectionUtils.isNotEmpty(map.get(7))){
                Result<List<Menu>> menusResult = getMenuService.batchQueryMenuDetailList(entityId, map.get(7));
                if (!menusResult.isSuccess()){
                    bizLogger.error("[kb_databack][error]getMenuService.batchQueryMenuDetailList(entityId, menuIds) failed. entityId:{}, menuIds:{}", JSON.toJSONString(entityId), JSON.toJSONString(map.get(7)));
                }else if(CollectionUtils.isNotEmpty(menusResult.getModel())){
                    menusResult.getModel().forEach(menu -> menuMap.put(menu.getId(), menu));
                }
            }

            //批量同步
            int success = 0;
            int failed = 0;
            StringBuilder errMsg = new StringBuilder();
            if(map!=null && CollectionUtils.isNotEmpty(map.keySet())) {
                for (Integer idType : map.keySet()){
                    if(map.get(idType)==null){
                        continue;
                    }
                    for(String localId : map.get(idType)) {
                        try {
                            switch (idType) {
                                case 1://规格
                                    kouBeiCheckUtil.checkSpecId(merchantId, shopId, localId, entityId, true, null);
                                    break;
                                case 2://单位
                                    kouBeiCheckUtil.checkUnitId(merchantId, shopId, entityId, localId, true, null);
                                    break;
                                case 3://菜类
                                    kouBeiCheckUtil.checkKindMenuId(CommonConstant.ITEM_CATEGORY, merchantId, shopId, entityId, localId, true, null);
                                    kouBeiCheckUtil.checkKindMenuId(CommonConstant.ITEM_COOK_CATEGORY, merchantId, shopId, entityId, localId, true, null);
                                    break;
                                case 4://套餐组
                                    kouBeiCheckUtil.checkGroupId(merchantId, shopId, entityId, localId, true, null);
                                    break;
								case 9://加料
									kouBeiCheckUtil.checkAffiliateId(merchantId, shopId, localId, entityId, true, null);
									break;
                                case 7://菜品(菜品+sku+菜谱明细)
                                    boolean deleteFlag = false;// 是否删除该菜品

                                    //查询失败原因
                                    List<Integer> idTypeList = new ArrayList<>();
                                    idTypeList.add(7);
                                    idTypeList.add(8);
                                    ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopId, "107", null, localId, null);
                                    itemMappingQuery.setSyncStatus(0);
                                    itemMappingQuery.setIdTypeList(idTypeList);
                                    List<ItemMapping> itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
                                    if(CollectionUtils.isNotEmpty(itemMappings)){
                                        for(ItemMapping itemMapping : itemMappings){
                                            if("参数有误参数有问题".equals(itemMapping.getSyncResult()) && itemMapping.getCreateTime()<1540299600000L){
                                                deleteFlag = true;
                                            }
                                        }
                                    }

                                    //删除菜品映射
                                    if(deleteFlag) {
                                        kouBeiCheckUtil.deleteItemMappingAndItemMenuMapping(entityId, shopId, localId);
                                    }

                                    //同步菜品
                                    KbDish kbDish = new KbDish();
                                    String dishId = kouBeiCheckUtil.checkDishId(merchantId, shopId, entityId, localId, true, null, kbDish);//菜品
                                    if (StringUtils.isNotEmpty(dishId)) {
                                        //sku
                                        kouBeiCheckUtil.checkBatchSkuId(merchantId, shopId, entityId, localId, true, null, kbDish);//菜品sku
                                        //菜谱明细
                                        CookDetailQuery cookDetailQuery = new CookDetailQuery(entityId);
                                        cookDetailQuery.setMenuId(localId);
                                        cookDetailQuery.setPageSize(CookDetailQuery.MAX_PAGE_SIZE);
                                        List<CookDetailBO> cookDetailBOs = cookDetailInService.selectByQuery(cookDetailQuery);
                                        if (CollectionUtils.isNotEmpty(cookDetailBOs)) {
                                            for (CookDetailBO cookDetailBO : cookDetailBOs) {
                                                koubeiCookDishHandler.addCookDetailBODetail(cookDetailBO, entityId, merchantId, cookDetailBO.getCookId(), shopId);
                                            }
                                        }
                                        success++;
                                    } else {
                                        failed++;
                                    }
                                    break;
                            }
                        }catch (BizException e){
                            if(idType==7) {
                                failed++;
                                errMsg.append(failed).append(". \"").append(menuMap.get(localId)==null ? "未找到菜品" : menuMap.get(localId).getName()).append("\", 失败信息：").append(e.getMessage()).append("        ");
                            }
                            bizLogger.error("[kb_databack][error]IKoubeiSyncService.batchItemSync(entityId) BizException. entityId:{}, idType:{}, localId:{}", JSON.toJSONString(entityId), JSON.toJSONString(idType), JSON.toJSONString(localId), e);
                        }catch (Exception e){
                            if(idType==7) {
                                failed++;
                                errMsg.append(failed).append(". \"").append(menuMap.get(localId)==null ? "未找到菜品" : menuMap.get(localId).getName()).append("\", 失败信息：").append(e.getMessage()).append("        ");
                            }
                            bizLogger.error("[kb_databack][error]IKoubeiSyncService.batchItemSync(entityId). entityId:{}, idType:{}, localId:{}", JSON.toJSONString(entityId), JSON.toJSONString(idType), JSON.toJSONString(localId), e);
                        }
                    }
                }
            }
            syncResultBo.setSuccessCount(success);
            syncResultBo.setFailCount(failed);
            this.saveLog(entityId, "batchItemSync", "[batchItemSync-3]批量同步完成，菜品同步数："+ (success+failed) +", 失败数："+ failed +"。" + (failed==0 ? "" : "    以下展示失败的菜品信息：" + errMsg));


            //发送同步结果
            syncResultBo.setSyncStatus(2);
            codisService.setObject(key2, syncResultBo, 30*60);
            itemRmqService.rocketMqTransmit("ITEM_SYNC", JSON.toJSON(syncResultBo));//发送消息通知
        } catch (BizException e) {
            bizLogger.error("[kb_databack][error]IKoubeiSyncService.batchItemSync(entityId) BizException. entityId: " + JSON.toJSONString(entityId), e);
            this.saveLog(entityId, "batchItemSync", "[batchItemSync-eror]菜品批量同步中断："+ e.getMessage());
            syncResultBo.setSyncStatus(3);
            syncResultBo.setErrorMsg(e.getMessage());
            codisService.setObject(key2, syncResultBo, 30*60);
            itemRmqService.rocketMqTransmit("ITEM_SYNC", JSON.toJSON(syncResultBo));//发送消息通知
            result.setSuccess(false);
            result.setMessage("服务器内部异常");
            return result;
        } catch (Exception e) {
            bizLogger.error("[kb_databack][error]IKoubeiSyncService.batchItemSync(entityId). entityId: " + JSON.toJSONString(entityId), e);
            this.saveLog(entityId, "batchItemSync", "[batchItemSync-eror]菜品批量同步中断："+ e.getMessage());
            syncResultBo.setSyncStatus(3);
            syncResultBo.setErrorMsg("服务器内部错误");
            codisService.setObject(key2, syncResultBo, 30*60);
            itemRmqService.rocketMqTransmit("ITEM_SYNC", JSON.toJSON(syncResultBo));//发送消息通知
            result.setSuccess(false);
            result.setMessage("服务器内部异常");
            return result;
        }

        //同步完成后，同步菜品估清情况
        this.selloutSync(entityId, platCode);

        return result;
    }

    @Override
    public Result<SyncResultBo> getBatchItemSyncResult(String entityId, String platCode) {
        Result<SyncResultBo> result = new ResultSupport<>();
        String key2 = CommonConstant.KOUBEI_BATCH_ITEM_SYNC_RESULT + platCode + entityId;
        try {
            //查询缓存
            SyncResultBo syncResultBo = (SyncResultBo)codisService.getObject(key2);
            if(syncResultBo!=null) {
                result.setModel(syncResultBo);
                if(syncResultBo.getSyncStatus()!=1) {
                    codisService.del(key2);
                }
                bizLogger.info("[kb_databack]getBatchItemSyncResult(entityId, platCode) . result: "+ JSON.toJSONString(result));
                return result;
            }

            //返回未同步
            syncResultBo = new SyncResultBo(2);//1全量同步 2批量同步
            syncResultBo.setEntityId(entityId);
            syncResultBo.setSyncStatus(4);
            result.setModel(syncResultBo);
        } catch (Exception e) {
            bizLogger.error("[kb_databack] fail to get cook detail sync result. entityId: {}, platCode: {}", entityId, platCode, e);
            result.setSuccess(false);
            result.setMessage("服务器开小差了，请重试");
            return result;
        }
        return result;
    }

    @Override
    public Result<Map<Integer, List<SimpleSyncResultBo>>> getSimpleSyncResult(String entityId, String platCode, Map<Integer, List<String>> map, Integer syncStatus) {
        Result<Map<Integer, List<SimpleSyncResultBo>>> result = new ResultSupport<>();
        Map<Integer, List<SimpleSyncResultBo>> resultMap = new HashMap<>();
        String key2 = CommonConstant.KOUBEI_BATCH_ITEM_SYNC_RESULT + platCode + entityId;//批量同步结果的key
        String key2Params = CommonConstant.KOUBEI_BATCH_ITEM_SYNC_MAP + platCode + entityId;//批量同步参数的key
        List<SimpleSyncResultBo> idType1 = new ArrayList<>();
        List<SimpleSyncResultBo> idType2 = new ArrayList<>();
        List<SimpleSyncResultBo> idType3 = new ArrayList<>();
        List<SimpleSyncResultBo> idType4 = new ArrayList<>();
        List<SimpleSyncResultBo> idType7 = new ArrayList<>();
        List<SimpleSyncResultBo> idType9 = new ArrayList<>();
        resultMap.put(1, idType1);
        resultMap.put(2, idType2);
        resultMap.put(3, idType3);
        resultMap.put(4, idType4);
        resultMap.put(7, idType7);
        resultMap.put(9, idType9);

        result.setModel(resultMap);
        try {

            // 绑定关系校验
            ShopBindVo shopBindVo = kouBeiCheckUtil.getShopBindVo(entityId);
            if (shopBindVo==null || shopBindVo.getBindStatus()!=1 || StringUtils.isBlank(shopBindVo.getMerchantId()) || StringUtils.isBlank(shopBindVo.getShopId()) ){
                throw new BizException("店铺未绑定口碑店，或关联状态为已关闭!");
            }
            String shopId = shopBindVo.getShopId();

            //查询各个类型的同步结果
            //1规格
            if (map == null || map.keySet().contains(1)) {
                Integer idType = 1;
                ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopId, platCode, idType, null, null);
                itemMappingQuery.setLocalIdList(map == null ? null : map.get(idType));
                itemMappingQuery.setPageSize(ItemMenuMappingQuery.MAX_PAGE_SIZE);
                List<ItemMapping> itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
                if (CollectionUtils.isNotEmpty(itemMappings)) {
                    itemMappings.forEach(itemMapping -> {
                        SimpleSyncResultBo simpleSyncResultBo = new SimpleSyncResultBo(itemMapping.getId(), entityId, platCode, shopId, idType, itemMapping.getLocalId(), itemMapping.getSyncStatus()==1 ? 2 : 3 , itemMapping.getSyncResult()!=null && itemMapping.getSyncResult().contains("KOUBEI商品同步失败") ? failMsg : itemMapping.getSyncResult(), itemMapping.getOpTime());
                        idType1.add(simpleSyncResultBo);
                    });
                }
            }else {
                resultMap.remove(1);
            }
            //2单位
            if (map == null || map.keySet().contains(2)) {
                Integer idType = 2;
                ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopId, platCode, idType, null, null);
                itemMappingQuery.setLocalIdList(map == null ? null : map.get(idType));
                itemMappingQuery.setPageSize(ItemMenuMappingQuery.MAX_PAGE_SIZE);
                List<ItemMapping> itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
                if (CollectionUtils.isNotEmpty(itemMappings)) {
                    itemMappings.forEach(itemMapping -> {
                        SimpleSyncResultBo simpleSyncResultBo = new SimpleSyncResultBo(itemMapping.getId(), entityId, platCode, shopId, idType, itemMapping.getLocalId(), itemMapping.getSyncStatus()==1 ? 2 : 3, itemMapping.getSyncResult()!=null && itemMapping.getSyncResult().contains("KOUBEI商品同步失败") ? failMsg : itemMapping.getSyncResult(), itemMapping.getOpTime());
                        idType2.add(simpleSyncResultBo);
                    });
                }
            }else {
                resultMap.remove(2);
            }
            //3菜类(菜类+菜谱菜类)
            if (map == null || map.keySet().contains(3)) {
                Integer idType = 3;
                List<Integer> idTypes = new ArrayList<>();
                idTypes.add(idType);//菜类
                idTypes.add(6);//菜谱菜类

                ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopId, platCode, null, null, null);
                itemMappingQuery.setIdTypeList(idTypes);
                itemMappingQuery.setLocalIdList(map == null ? null : map.get(idType));
                itemMappingQuery.setPageSize(ItemMenuMappingQuery.MAX_PAGE_SIZE);
                List<ItemMapping> itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
                if (CollectionUtils.isNotEmpty(itemMappings)) {
                    HashMap<String, ItemMapping> itemMappingMap = new HashMap<>();
                    itemMappings.forEach(itemMapping -> {
                        ItemMapping oldItemMapping = itemMappingMap.get(itemMapping.getLocalId());
                        itemMappingMap.put(itemMapping.getLocalId(), oldItemMapping==null || itemMapping.getSyncStatus()==0 ? itemMapping :  oldItemMapping);
                    });
                    //判断菜类、菜谱菜类是否都成功
                    for(String kindMenuId : itemMappingMap.keySet()){//先同步的是6菜谱菜类
                        ItemMapping itemMapping = itemMappingMap.get(kindMenuId);

                        Integer status = itemMapping.getSyncStatus();
                        String msg = status == 1 ? null : itemMapping.getSyncResult()!=null && itemMapping.getSyncResult().contains("KOUBEI商品同步失败") ? failMsg : itemMapping.getSyncResult();
                        SimpleSyncResultBo simpleSyncResultBo = new SimpleSyncResultBo(itemMapping.getId(), entityId, platCode, shopId, idType, itemMapping.getLocalId(), status==1 ? 2 : 3, msg, itemMapping.getOpTime());
                        idType3.add(simpleSyncResultBo);
                    }
                }
            }else {
                resultMap.remove(3);
            }
            //4套餐分组
            if (map == null || map.keySet().contains(4)) {
                Integer idType = 4;
                ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopId, platCode, idType, null, null);
                itemMappingQuery.setLocalIdList(map == null ? null : map.get(idType));
                itemMappingQuery.setPageSize(ItemMenuMappingQuery.MAX_PAGE_SIZE);
                List<ItemMapping> itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
                if (CollectionUtils.isNotEmpty(itemMappings)) {
                    itemMappings.forEach(itemMapping -> {
                        SimpleSyncResultBo simpleSyncResultBo = new SimpleSyncResultBo(itemMapping.getId(), entityId, platCode, shopId, idType, itemMapping.getLocalId(), itemMapping.getSyncStatus()==1 ? 2 : 3, itemMapping.getSyncResult()!=null && itemMapping.getSyncResult().contains("KOUBEI商品同步失败") ? failMsg : itemMapping.getSyncResult(), itemMapping.getOpTime());
                        idType4.add(simpleSyncResultBo);
                    });
                }
            }else {
                resultMap.remove(4);
            }
			//9加料
			if (map == null || map.keySet().contains(9)) {
				Integer idType = 9;
				ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopId, platCode, idType, null, null);
				itemMappingQuery.setLocalIdList(map == null ? null : map.get(idType));
				itemMappingQuery.setPageSize(ItemMenuMappingQuery.MAX_PAGE_SIZE);
				List<ItemMapping> itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
				if (CollectionUtils.isNotEmpty(itemMappings)) {
					itemMappings.forEach(itemMapping -> {
						SimpleSyncResultBo simpleSyncResultBo = new SimpleSyncResultBo(itemMapping.getId(), entityId, platCode, shopId, idType, itemMapping.getLocalId(), itemMapping.getSyncStatus() == 1 ? 2 : 3, itemMapping.getSyncResult() != null && itemMapping.getSyncResult().contains("KOUBEI商品同步失败") ? failMsg : itemMapping.getSyncResult(), itemMapping.getOpTime());
						idType9.add(simpleSyncResultBo);
					});
				}
			} else {
				resultMap.remove(9);
			}
            //7菜品(菜品+sku+菜谱明细)
            if (map == null || map.keySet().contains(7)) {
                Integer idType = 7;
                List<String> paramMenuIds = map == null ? null : map.get(idType);

                //查询菜谱明细
                CookDetailQuery cookDetailQuery = new CookDetailQuery(entityId);
                cookDetailQuery.setMenuIdList(paramMenuIds);
                cookDetailQuery.setPageSize(CookDetailQuery.MAX_PAGE_SIZE);
                List<CookDetailBO> cookDetailBOs = cookDetailInService.selectByQuery(cookDetailQuery);
                if(CollectionUtils.isNotEmpty(cookDetailBOs)) {
                    List<String> menuIds = cookDetailBOs.stream().map(CookDetailBO::getMenuId).collect(Collectors.toList());
                    String cookId = String.valueOf(cookDetailBOs.get(0).getCookId());
                    HashMap<String, ItemMapping> itemMappingMap = new HashMap<>();
                    Map<String, ItemMenuMapping> itemMenuMappingMap = new HashMap<>();

                    //查询菜谱明细映射
                    ItemMenuMappingQuery itemMenuMappingQuery = new ItemMenuMappingQuery(entityId, shopId, platCode, null, null, cookId, null);
                    itemMenuMappingQuery.setLocalItemIdList(paramMenuIds);
                    itemMenuMappingQuery.setPageSize(ItemMenuMappingQuery.MAX_PAGE_SIZE);
                    List<ItemMenuMapping> itemMenuMappings = itemMenuMappingService.getItemMenuMappingListByQuery(itemMenuMappingQuery);
                    if (CollectionUtils.isNotEmpty(itemMenuMappings)) {
                        itemMenuMappings.forEach(itemMenuMapping -> itemMenuMappingMap.put(itemMenuMapping.getLocalItemId(), itemMenuMapping));
                    }

                    //查询菜品、sku
                    List<Integer> idTypes = new ArrayList<>();
                    idTypes.add(idType);//菜品
                    idTypes.add(8);//sku
                    ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopId, platCode, null, null, null);
                    itemMappingQuery.setIdTypeList(idTypes);
                    itemMappingQuery.setLocalIdList(paramMenuIds);
                    itemMappingQuery.setPageSize(ItemMenuMappingQuery.MAX_PAGE_SIZE * 5);
                    List<ItemMapping> itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
                    if (CollectionUtils.isNotEmpty(itemMappings)) {
                        //将失败的sku、菜品（若有，否则选用一个成功的）放至itemMappingMap
                        itemMappings.stream().filter(itemMapping -> menuIds.contains(itemMapping.getLocalId())).forEach(itemMapping -> {
                            ItemMapping oldItemMapping = itemMappingMap.get(itemMapping.getLocalId());
                            itemMappingMap.put(itemMapping.getLocalId(), oldItemMapping==null || itemMapping.getSyncStatus()<oldItemMapping.getSyncStatus() || (Objects.equals(itemMapping.getSyncStatus(), oldItemMapping.getSyncStatus()) && itemMapping.getOpTime()>oldItemMapping.getOpTime()) ? itemMapping : oldItemMapping);
                        });
                    }

                    //封装结果
                    for(CookDetailBO cookDetailBO : cookDetailBOs){
                        ItemMenuMapping itemMenuMapping = itemMenuMappingMap.get(cookDetailBO.getMenuId());//菜谱同步信息
                        ItemMapping itemMapping = itemMappingMap.get(cookDetailBO.getMenuId());//菜品、sku同步信息

                        Integer status;
                        String msg;
                        if(itemMapping!=null && itemMapping.getSyncStatus()==1 && itemMenuMapping==null){//无菜谱明细映射菜谱
                            if(System.currentTimeMillis()> 20*1000+cookDetailBO.getOpTime()){
                                status = 3;
                                msg = "菜品同步失败，请点击下方\"立即同步\"按钮重试";
                            }else {
                                status = 1;
                                msg = "同步中";
                            }
                        }else if(itemMenuMapping!=null && itemMapping!=null && itemMenuMapping.getSyncStatus()==1 && itemMapping.getSyncStatus()==1 ){//同步成功
                            status = 2;
                            msg = "同步成功";
                        }else {
                            status = 3;
                            msg = itemMapping==null ? "菜品未同步" :
                                    itemMapping.getSyncStatus()==0 ? (itemMapping.getSyncResult()!=null && itemMapping.getSyncResult().contains("KOUBEI商品同步失败") ? failMsg : itemMapping.getSyncResult()) :
                                            itemMenuMapping==null ? "菜品未同步至菜谱" : (itemMenuMapping.getSyncResult()!=null && itemMenuMapping.getSyncResult().contains("KOUBEI商品同步失败") ? failMsg : itemMenuMapping.getSyncResult());
                        }
                        Long opTime = itemMapping==null && itemMenuMapping==null ? null : itemMapping!=null && itemMenuMapping!=null ? Math.max(itemMapping.getOpTime(), itemMenuMapping.getOpTime()) : itemMapping!=null ? itemMapping.getOpTime() :  itemMenuMapping.getOpTime();
                        SimpleSyncResultBo simpleSyncResultBo = new SimpleSyncResultBo(null, entityId, platCode, shopId, idType, cookDetailBO.getMenuId(), status, msg, opTime);
                        idType7.add(simpleSyncResultBo);
                    }
                }
            }else {
                resultMap.remove(7);
            }

            //批量同步中处理
            SyncResultBo syncResultBo2 = (SyncResultBo)codisService.getObject(key2);
            if (syncResultBo2 != null && syncResultBo2.getSyncStatus() == 1) {//批量同步中
                //获取缓存的批量同步参数信息
                Map<Integer, List<String>> params = (Map<Integer, List<String>>) codisService.getObject(key2Params);
                if (params!=null && CollectionUtils.isNotEmpty(params.keySet())) {
                    for (Integer idType : params.keySet()) {
                        if (map != null && !map.keySet().contains(idType)) {
                            continue;
                        }
                        //赋值批量同步中状态
                        List<String> ids = params.get(idType);
                        resultMap.get(idType).removeIf(simpleSyncResultBo -> ids.contains(simpleSyncResultBo.getLocalId()));
                        ids.forEach(ss -> {
                            if (map == null || map.get(idType) == null || map.get(idType).contains(ss)) {
                                resultMap.get(idType).add(new SimpleSyncResultBo(null, entityId, platCode, shopId, idType, ss, 1, "同步中", null));
                            }
                        });
                    }
                }
            }

            //数据处理
            for (Integer idType : resultMap.keySet()) {
                //同步状态处理
                if (syncStatus != null) {
                    resultMap.put(idType, resultMap.get(idType).stream().filter(simpleSyncResultBo -> Objects.equals(simpleSyncResultBo.getSyncStatus(), syncStatus)).collect(Collectors.toList()));
                }

                // 错误文案修改
                resultMap.get(idType).forEach(simpleSyncResultBo -> {
                    if(StringUtils.isNotBlank(simpleSyncResultBo.getSyncResult())) {
                        simpleSyncResultBo.setSyncResult(simpleSyncResultBo.getSyncResult().replace("无效的应用授权令牌", "口碑功能授权失效，请进入“口碑功能授权”重新授权后再同步商品！"));
                        simpleSyncResultBo.setSyncResult(simpleSyncResultBo.getSyncResult().replace("获取口碑token失败", "口碑功能授权失效，请进入“口碑功能授权”重新授权后再同步商品！"));
                        simpleSyncResultBo.setSyncResult(simpleSyncResultBo.getSyncResult().replace("商户未授权当前接口", "口碑功能授权失效，请进入“口碑功能授权”重新授权后再同步商品！"));

                        simpleSyncResultBo.setSyncResult(simpleSyncResultBo.getSyncResult().replace("参数有误参数有问题", "菜品数据异常，请重试！"));
                        simpleSyncResultBo.setSyncResult(simpleSyncResultBo.getSyncResult().replace("商户下的菜品已经存在,不能重复创建", "不允许同名菜品或套餐，请修改菜品或套餐名称！"));
                        simpleSyncResultBo.setSyncResult(simpleSyncResultBo.getSyncResult().replace("未满足同步要求，菜品编码限32个字符", "菜品编码超过32位字数限制，请检查修改！"));
                        simpleSyncResultBo.setSyncResult(simpleSyncResultBo.getSyncResult().replace("未满足同步要求，菜品名称限32个字符", "菜品名称超过32位字数限制，请检查修改！"));

                        simpleSyncResultBo.setSyncResult(simpleSyncResultBo.getSyncResult().replace("未满足同步要求，套餐起点份数限最多1份", "口碑套餐起点份数最多为1份，请修改起点份数！"));
                        simpleSyncResultBo.setSyncResult(simpleSyncResultBo.getSyncResult().replace("未满足同步要求，套餐分组数量限制设置过大", "分组内子菜总和要大于分组数量限制！"));
                        simpleSyncResultBo.setSyncResult(simpleSyncResultBo.getSyncResult().replace("系统繁忙", "菜品信息存在特殊字符等情况，请检查修改！"));
                        simpleSyncResultBo.setSyncResult(simpleSyncResultBo.getSyncResult().replace("查询菜品失败，或菜品已删除", "套餐内子菜不能单独删除！"));
                        simpleSyncResultBo.setSyncResult(simpleSyncResultBo.getSyncResult().replace("查询菜品（/子菜）失败，或菜品（/子菜）已删除", "套餐内子菜不能单独删除！"));
                        simpleSyncResultBo.setSyncResult(simpleSyncResultBo.getSyncResult().replace("查询菜品单位失败，或单位已删除", "菜品单位已经删除，请添加新单位后重新关联商品！"));
						simpleSyncResultBo.setSyncResult(simpleSyncResultBo.getSyncResult().replace("查询菜品加料失败，或加料已删除", "菜品加料已经删除，请添加新加料后重新关联商品！"));
						simpleSyncResultBo.setSyncResult(simpleSyncResultBo.getSyncResult().replace("查询菜品菜类失败，或菜类已删除", "菜品分类已经删除，请添加分类后重新关联商品！"));
                        simpleSyncResultBo.setSyncResult(simpleSyncResultBo.getSyncResult().replace("查询菜品规格失败，或规格已删除", "菜品规格已经删除，请添加规格后重新关联商品！"));
                        simpleSyncResultBo.setSyncResult(simpleSyncResultBo.getSyncResult().replace("查询菜品（/子菜）规格失败，或规格已删除", "菜品规格已经删除，请添加规格后重新关联商品！"));
                        simpleSyncResultBo.setSyncResult(simpleSyncResultBo.getSyncResult().replace("查询菜品（/子菜）规格失败，或规格已删除", "菜品规格已经删除，请添加规格后重新关联商品！"));
                    }
                });
            }

        } catch (Exception e) {
            bizLogger.error("[kb_databack] fail to get item sync result. entityId: {}, platCode: {}", entityId, platCode, e);
            result.setModel(null);
            result.setSuccess(false);
            result.setMessage("服务器开小差了，请重试");
            return result;
        }

        return result;
    }

    public Result selloutSync(String entityId, String platCode){
        String merchantId;
        String shopId;
        try{
            // 绑定关系校验
            ShopBindVo shopBindVo = kouBeiCheckUtil.getShopBindVo(entityId);
            if (shopBindVo==null || shopBindVo.getBindStatus()!=1 || StringUtils.isBlank(shopBindVo.getMerchantId()) || StringUtils.isBlank(shopBindVo.getShopId()) ||
                    (StringUtils.isNotBlank(shopBindVo.getExtendFields()) && Objects.equals(JSON.parseObject(shopBindVo.getExtendFields(), ShopBindExtendFieldsVo.class).getSyncStatus(), 3))){
                throw new BizException("店铺未绑定口碑店，或关联状态为已关闭、或同步状态为失败!");
            }
            merchantId = shopBindVo.getMerchantId();
            shopId = shopBindVo.getShopId();


            //估清菜品
            int balancePageIndex = 1;
            int balancePageSize = 200;
            int balanceCount = 0;
            int balanceFailed = 0;
            StringBuilder balanceErrMsg = new StringBuilder();
            while (true) {
                MenuBalanceQuery menuBalanceQuery = new MenuBalanceQuery();
                menuBalanceQuery.setEntityId(entityId);
                menuBalanceQuery.setPageIndex(balancePageIndex++);
                menuBalanceQuery.setPageSize(balancePageSize);
                Result<List<MenuBalance>> menuBalancesResult = menuBalanceClientService.getMenuBalanceListByQuery(menuBalanceQuery);
                if (!menuBalancesResult.isSuccess()) {
                    bizLogger.error("[kb_databack][error]menuBalanceClientService.getMenuBalanceList(entityId, new ArrayList<>()) failed. entityId: " + JSON.toJSONString(entityId) + ", menuBalancesResult: " + JSON.toJSONString(menuBalancesResult));
                    throw new BizException("[kb_databack][error]查询菜品估清信息失败");
                } else if (CollectionUtils.isNotEmpty(menuBalancesResult.getModel())) {
                    for (MenuBalance menuBalance : menuBalancesResult.getModel()) {
                        try {

                            Result selloutSyncResult = koubeiDishSelloutHandler.syncDishSellout(menuBalance, merchantId, shopId);
                            if(selloutSyncResult == null) {
                                bizLogger.error("[kb_databack] there is no request bean. itemId: {}, merchantId: {}, shopId: {}", menuBalance.getMenuId(), merchantId, shopId);
                                continue;
                            }else if (!selloutSyncResult.isSuccess()) {
                                bizLogger.error("[kb_databack][error]kouBeiDishCookService.dishSelloutSync(kbDishSelloutRequest, merchantId, \"update\", \"estimated\") failed.: merchantId: " + JSON.toJSONString(merchantId) + ", selloutSyncResult: " + JSON.toJSONString(selloutSyncResult));
                            }
                        } catch (Exception e) {
                            bizLogger.error("[kb_databack][error][sellout]IKoubeiSyncService.batchItemSync(entityId). entityId: {}, menuBalance:{}", JSON.toJSONString(entityId), JSON.toJSON(menuBalance), e);
                            balanceErrMsg.append(++balanceFailed).append(". \"").append(menuBalance.getMenuId()).append("\", 失败信息：").append(e.getMessage()).append("        ");
                        }
                        balanceCount++;
                    }
                }else {
                    break;
                }
            }
            this.saveLog(entityId, "selloutSync", "[selloutSync]同步菜品估清完成，总需估清数："+ balanceCount +", 失败数："+ balanceFailed +"。" + (balanceFailed==0 ? "" : "    以下展示失败的菜品信息：" + balanceErrMsg));

            return new ResultSupport();
        }catch (Exception e){
            this.saveLog(entityId, "selloutSync", "[selloutSync]同步菜品估清失败："+ e.getMessage());
            return new ResultSupport(false, "", e.getMessage());
        }
    }



    public Result batchDelByMerchantIdShopId(String entityId, String merchantId, String shopId) {
        Result result = new ResultSupport();
        try {
            this.saveLog(entityId, "itemDel", "[itemDel-1]菜品全量删除开始！");
            //店铺先设置为同步失败(不允许增量同步)
            ShopBindVo shopBindVo = kouBeiCheckUtil.getShopBindVo(entityId);

            if(shopBindVo == null || StringUtils.isEmpty(shopBindVo.getMerchantId())) {
                this.saveLog(entityId, "itemDel", "绑定关系不存在或已解绑");
                result.setMessage("绑定关系不存在或已解绑");
                return result;
            }

            ShopBindExtendFieldsVo shopBindExtendFieldsVo = StringUtils.isBlank(shopBindVo.getExtendFields()) ? new ShopBindExtendFieldsVo() : JSON.parseObject(shopBindVo.getExtendFields(), ShopBindExtendFieldsVo.class);
            shopBindExtendFieldsVo.setSyncStatus(3);
            shopBindExtendFieldsVo.setFailedType(null);
            shopBindExtendFieldsVo.setFailedId(null);
            shopBindVo.setShopBindExtendFieldsVo(shopBindExtendFieldsVo);
            shopBindVo.setExtendFields(JSON.toJSONString(shopBindExtendFieldsVo));
            kouBeiCheckUtil.updateShopBindVo(shopBindVo);

            shopId = StringUtils.isNotBlank(shopId) ? shopId : shopBindVo.getShopId();

            //查询需要删除的映射数据
            ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopId, String.valueOf(CommonConstant.KOUBEI_PLATFORM), null, null, null);
            List<ItemMapping> itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
            if(CollectionUtils.isEmpty(itemMappings)){
                this.saveLog(entityId, "itemDel", "[itemDel-2]菜品全量删除结束！");
                return result;
            }

            if(StringUtils.isNotBlank(shopId)) {
                //删除口碑菜品数据
                Result<List<KbDishCookResponse>> kbDishCookResponseListResult = kouBeiDishCookService.queryCookByMerchantIdShopId(merchantId, shopId);
                if (!kbDishCookResponseListResult.isSuccess() && !kbDishCookResponseListResult.getResultCode().equals(error248.getCode())) {
                    throw new OpenApiException(error227.getCode(), error227.getMessage(), "fail to query cooks.");
                }
                List<KbDishCookResponse> kbDishCookResponses = kbDishCookResponseListResult.getModel() == null ? new ArrayList<>() : kbDishCookResponseListResult.getModel();
                for (KbDishCookResponse kbDishCookResponse : kbDishCookResponses) {
                    KbDishCookRequest request = new KbDishCookRequest();
                    KbDishCook kbDishCook = kbDishCookResponse.getKbDishCook();
                    request.setKbDishCook(kbDishCook);
                    Result<KbDishCookResponse> delCookResult = kouBeiDishCookService.dishCookSync(request, KouBeiSyncTypeEnum.del, KouBeiCookBizTypeEnum.cook);
                    if (!delCookResult.isSuccess()) {
                        bizLogger.error("[kb_databack] fail to del koubei cook. entityId: {}, merchantId: {}, shopId: {}, cookId: {}, result: {}", entityId, merchantId, shopId, kbDishCook.getCookId(), JSON.toJSONString(delCookResult));
                    }
                }
                Result<List<KbDishResponse>> kbDishResponseListResult = kouBeiDishCookService.queryDishesByShopId(merchantId, shopId);
                if (!kbDishResponseListResult.isSuccess() && !kbDishResponseListResult.getResultCode().equals(error248.getCode())) {
                    throw new OpenApiException(error227.getCode(), error227.getMessage(), "fail to query dishes.");
                }
                List<KbDishResponse> kbDishes = kbDishResponseListResult.getModel() == null ? new ArrayList<>() : kbDishResponseListResult.getModel();
                for (KbDishResponse kbDishResponse : kbDishes) {
                    KbDishRequest request = new KbDishRequest();
                    KbDish kbDish = kbDishResponse.getKbDish();
                    request.setKbDish(kbDish);
                    Result<KbDishResponse> delDishResult = kouBeiDishCookService.dishSync(request, KouBeiSyncTypeEnum.del, KouBeiDishBizTypeEnum.dish);
                    if (!delDishResult.isSuccess()) {
                        bizLogger.error("[kb_databack] fail to del koubei dish. entityId: {}, merchantId: {}, shopId: {}, dishId: {}, result: {}", entityId, merchantId, shopId, kbDish.getDishId(), JSON.toJSONString(delDishResult));
                    }
                }

				//删口碑加料数据
				Result<KbDishAdditionResponse> listResult = kouBeiDishCookService.queryAdditionListByMaterialId(merchantId, null, 200, 1);
				KbDishAdditionResponse kbDishAdditionResponse = listResult.getModel();
				if(null!=kbDishAdditionResponse){
					List<KbDishAddition> kbDishAdditionList = kbDishAdditionResponse.getKbDishAddition();
					int num2;
					if (Integer.valueOf(kbDishAdditionResponse.getTotalCount()) >= 200) {
						int num = Integer.valueOf(kbDishAdditionResponse.getTotalCount()) % 200;
						num2 = Integer.valueOf(kbDishAdditionResponse.getTotalCount()) / 200;
						if (num != 0) {
							num2 += 1;
						}
						if (num2 >= 2) {
							for (int i = 2; i <= num2; i++) {
								kbDishAdditionList.addAll(kouBeiDishCookService.queryAdditionListByMaterialId(merchantId, null, 200, i).getModel().getKbDishAddition());
							}
						}
					}

					List<KbDishAddition> deleteList = new ArrayList<>();
					for (KbDishAddition kbDishAddition : kbDishAdditionList) {
						if (shopId.equals(kbDishAddition.getPublicId())) {
							deleteList.add(kbDishAddition);
						}
					}

					if (CollectionUtils.isNotEmpty(deleteList)) {
						for (KbDishAddition kbDishAddition : deleteList) {
							if (shopId.equals(kbDishAddition.getPublicId())) {
								kouBeiDishCookService.dishAdditionSync(kbDishAddition, KouBeiSyncTypeEnum.del);
							}
						}
					}
				}
            }

            //删除映射数据
            int delItemCount = itemMappingService.batchDeleteByEntityId(entityId, null, String.valueOf(CommonConstant.KOUBEI_PLATFORM));
            int delMenuDetailCount = itemMenuMappingService.batchDeleteByEntityId(entityId, null, String.valueOf(CommonConstant.KOUBEI_PLATFORM));
            int delCookDetailCount = 0;
            //删除所有的cookDetail
            CookDetailQuery cookDetailQuery = new CookDetailQuery(entityId);
            cookDetailQuery.setUsePage(true);
            List<CookDetailBO> cookDetailBOs = cookDetailInService.selectByQuery(cookDetailQuery);
            if(CollectionUtils.isNotEmpty(cookDetailBOs)){
                delCookDetailCount = cookDetailInService.batchDeleteByIdList(entityId, cookDetailBOs.stream().map(CookDetailBO::getId).collect(Collectors.toList()));
            }
            bizLogger.info("[kb_databack] batch del relation mapping. delItemCount: {}, delMenuDetailCount: {}, delCookDetailCount:{}, entityId: {}", delItemCount, delMenuDetailCount, delCookDetailCount, entityId);

            this.saveLog(entityId, "itemDel", "[itemDel-2]菜品全量删除结束！");
        } catch (OpenApiException e) {
            bizLogger.error("[kb_databack] entityId: {}, merchantId: {}, shopId: {}", entityId, merchantId, shopId, e);
            this.saveLog(entityId, "itemDel", "[itemDel-error]菜品全量删除中断："+ e.getMsg());
            result.setSuccess(false);
            result.setMessage(e.getMsg());
        } catch (Exception e) {
            bizLogger.error("[kb_databack] fail to batch delete koubei items. entityId: {}, merchantId: {}, shopId: {}", entityId, merchantId, shopId, e);
            this.saveLog(entityId, "itemDel", "[itemDel-error]菜品全量删除中断："+ e.getMessage());
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        return result;
    }





    public Result batchDelMapping(String entityId, String merchantId, String shopId){
        Result result = new ResultSupport();
        try {
//            itemMappingService.batchDeleteByEntityId(entityId, shopId, String.valueOf(CommonConstant.KOUBEI_PLATFORM));
//            itemMenuMappingService.batchDeleteByEntityId(entityId, shopId, String.valueOf(CommonConstant.KOUBEI_PLATFORM));
            itemMappingService.batchDeleteByEntityId(entityId, null, String.valueOf(CommonConstant.KOUBEI_PLATFORM));
            itemMenuMappingService.batchDeleteByEntityId(entityId, null, String.valueOf(CommonConstant.KOUBEI_PLATFORM));
        }  catch (Exception e) {
            bizLogger.error("[kb_databack] fail to batch delete koubei items. entityId: {}, merchantId: {}, shopId: {}", entityId, merchantId, shopId, e);
            result.setSuccess(false);
        }
        return result;
    }

    public Result addCookAndDetail(String entityId, String merchantId, String shopId, CookBO cookBO) {
        Result result = new ResultSupport();
        long cookId = cookBO.getId();
        try {
            ItemMapping itemMapping = itemMappingService.getTpId(String.valueOf(CommonConstant.KOUBEI_PLATFORM), CommonConstant.ITEM_COOK, entityId, String.valueOf(cookId), shopId);
            if(itemMapping == null) {
                boolean success = koubeiCookDishHandler.syncCook(cookBO, merchantId, entityId, shopId);
                if(!success) {
                    bizLogger.error("[kb_databack] fail to sync cook. entityId: {}, merchantId: {}, shopId: {}, cookId: {}", entityId, merchantId, shopId, cookId);
                    result.setSuccess(false);
                    return result;
                }
            }

            CookDetailQuery query = new CookDetailQuery(entityId);
            query.setCookId(cookBO.getId());
            query.setPageSize(CookDetailQuery.MAX_PAGE_SIZE);
            List<CookDetailBO> cookDetailBOs = cookDetailInService.selectByQuery(query);
            for(CookDetailBO cookDetailBO : cookDetailBOs) {
                try {
                    koubeiCookDishHandler.addCookDetailBODetail(cookDetailBO, entityId, merchantId, cookId,  shopId);
                } catch (Exception e) {
                    bizLogger.error("[kb_databack] fail to add cook detail. entityId: {}, merchantId: {}, shopId: {}, cookId: {}", entityId, merchantId, shopId, cookId, e);
                }
            }
        } catch (Exception e) {
            bizLogger.error("[kb_databack] fail to add cook and detail. entityId: {}, merchantId: {}, shopId: {}, cookId: {}", entityId, merchantId, shopId, cookId, e);
            result.setSuccess(false);
        }
        return result;
    }

    private void saveLog(String entityId, String action, String msg){
        try {
            fireLogClientAsyncService.saveLog(entityId, "batchSync", action, "sys", "sys", OpFromEnum.RetailAdapter.getValue(), 0, msg);
        }catch (Exception e){
            bizLogger.error("[kb_databack][error]fireLogClientAsyncService.saveLog(entityId, \"itemSync\", \"itemSync\", \"sys\", \"sys\", OpFromEnum.Yardcontent_BackWeb_BossMgCenter.getValue(), 0, msg). entityId:{}, msg:{}", entityId, msg, e);
        }
    }

    public Result clearAndSync(String entityId) {
        Result result = new ResultSupport();
        bizLogger.info("[kb_databack] syncHistoryOfSynchronizing: entityId：{}", entityId);
        try {
            ShopBindVo shopBindVo = kouBeiCheckUtil.getShopBindVo(entityId);
            if (shopBindVo == null || shopBindVo.getBindStatus() != 1 || StringUtils.isBlank(shopBindVo.getMerchantId()) || StringUtils.isBlank(shopBindVo.getShopId())) {
                throw new BizException("[kb_databack][error]店铺未绑定口碑店，或关联状态为已关闭!");
            }
            String merchantId = shopBindVo.getMerchantId();
            String shopId = shopBindVo.getShopId();

            //删除菜品
            this.batchDelByMerchantIdShopId(entityId, merchantId, shopId);
            //同步菜品
            this.itemSync(entityId, String.valueOf(CommonConstant.KOUBEI_PLATFORM));
        } catch (Exception e) {
            bizLogger.error("[kb_databack] syncHistoryOfSynchronizing: entityId：{}", entityId, e);
        }

        return result;
    }

    @Override
    public Result checkDish(String entityId, String menuId, boolean isNeedDelete) {
        Result result = new ResultSupport();
        bizLogger.info("[kb_databack] checkDish: entityId：{}， menuId{}， isNeedDelete{}", entityId, menuId, isNeedDelete);
        try {
            ShopBindVo shopBindVo = kouBeiCheckUtil.getShopBindVo(entityId);
            if (shopBindVo == null || shopBindVo.getBindStatus() != 1 || StringUtils.isBlank(shopBindVo.getMerchantId()) || StringUtils.isBlank(shopBindVo.getShopId())) {
                throw new BizException("[kb_databack][error]店铺未绑定口碑店，或关联状态为已关闭!");
            }
            String merchantId = shopBindVo.getMerchantId();
            String shopId = shopBindVo.getShopId();

            //删除菜品
            if(isNeedDelete){
                Result<Menu> menuResult = getMenuService.queryMenu(entityId, menuId);
                if (!menuResult.isSuccess() ) {
                    throw new BizException("[kb_databack][error]查询菜品失败");
                }else if(menuResult.getModel()!=null){
                    Menu menu = menuResult.getModel();
                    //先刪除菜譜-菜品
                    kouBeiDeleteUtil.deleteCookDish(shopId, merchantId, menu.getEntityId(), menu.getId());
                    try {
                        //删除菜品
                        kouBeiDeleteUtil.deleteDishId(merchantId, shopId, menu);
                    } catch (Exception e) {
                        bizLogger.error("[kb_databack] checkDish() of deleteDishId: entityId：{}, menuId：{}", entityId, menuId, e);
                    }
                }
                //删除映射关系
                kouBeiCheckUtil.deleteItemMappingAndItemMenuMapping(entityId, shopId, menuId);
            }

            //同步菜品
            KbDish kbDish = new KbDish();//通用参数
            String dishId = kouBeiCheckUtil.checkDishId(merchantId, shopId, entityId, menuId, true, null, kbDish);
            if (StringUtils.isNotEmpty(dishId)) {
                //同步菜品所有sku
                kouBeiCheckUtil.checkBatchSkuId(merchantId, shopId, entityId, menuId, true, null, kbDish);

                //同步菜谱明细
                kouBeiCheckUtil.checkDishCookMapping(merchantId, shopId, entityId, menuId);
            }

        } catch (Exception e) {
            bizLogger.error("[kb_databack] checkDish(). entityId：{}， menuId{}， isNeedDelete{}", entityId, menuId, isNeedDelete, e );
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        return result;
    }

    public Result syncHistory() {
        Result result = new ResultSupport();
        List<String> entityIdList = cookInService.getEntityIdList(0L, System.currentTimeMillis());
        int count = 0;
        for(String entityId : entityIdList) {
            CookDetailQuery cookDetailQuery = new CookDetailQuery(entityId);
            List<CookDetailBO> cookDetailBOList = cookDetailInService.selectByQuery(cookDetailQuery);
            ShopBindVo shopBindVo = kouBeiCheckUtil.getShopBindVo(entityId);
            if(shopBindVo == null || StringUtils.isEmpty(shopBindVo.getMerchantId())) {
                result.setMessage("绑定关系不存在");
                return result;
            }
            for(CookDetailBO cookDetailBO : cookDetailBOList) {
                try {
                    ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopBindVo.getShopId(), "107", 7, cookDetailBO.getMenuId(), null);
                    List<ItemMapping> itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
                    if(CollectionUtils.isEmpty(itemMappings)){
                        continue;
                    }else if(itemMappings.get(0).getSyncStatus()==0){
                        continue;
                    }

                    ItemMenuMapping itemMenuMapping = itemMenuMappingService.getItemMenuMappingByLocalId(entityId, shopBindVo.getShopId(), "107", String.valueOf(cookDetailBO.getCookId()), cookDetailBO.getMenuId());
                    if(itemMenuMapping != null) {
                        continue;
                    }
                    HashMap<String, String> dataMap = new HashMap<>();
                    dataMap.put("entity_id", cookDetailBO.getEntityId());
                    dataMap.put("menu_id", cookDetailBO.getMenuId());
                    dataMap.put("cook_id", String.valueOf(cookDetailBO.getCookId()));
                    dataMap.put("send_time", String.valueOf(System.currentTimeMillis()));
                    dataMap.put("data_after", JSON.toJSONString(cookDetailBO));
                    String tag = MessageTag.TABLE_COOK_DETAIL.concat(MessageTag.SEPARATOR).concat(MessageTag.OPERATE_INSERT);
                    // 消息发送
                    itemRmqService.rocketMqTransmit(tag,dataMap);
                } catch (Exception e) {
                    bizLogger.error("[kb_databack] fail to add and detail. entityId: {}, cookDetailBO: {}", entityId, JSON.toJSONString(cookDetailBO), e);
                }
            }
            bizLogger.info("[kb_databack] shop count. count: {}", count);
        }
        return result;
    }


    public Result syncHistoryOfSameName() {
        Result result = new ResultSupport();
        Map<String, String> map = new HashMap<>();
        map.put("0014857361559a05016193556383425c","00148573");
        map.put("003024806619d9e401662df5c40d0e6b","00302480");
        map.put("0012953466826daa016695f744ec702f","00129534");
        map.put("003341776619dca5016634b571f55074","00334177");
        map.put("000505986001a4cc01600b79f747093a","00050598");
        map.put("00087750592569d30159366bcf596f7f","00087750");
        for (String menuId : map.keySet()){
            this.checkDish(map.get(menuId), menuId, true);
        }
        return result;
    }

    public Result queryKoubeiCook(int count) {
        List<String> entityIdList = cookInService.getEntityIdList(0L, System.currentTimeMillis());
        Result<List<String>> result = dishObjectMappingService.getAllEntityIdList(1, 15000);
        List<String> dishObjectEntityIdList = result.getModel();
        bizLogger.info("[koubei history shop cook] entityIdList: {}", JSON.toJSONString(entityIdList));
        bizLogger.info("[koubei history shop cook] dishObjectEntityIdList: {}", JSON.toJSONString(dishObjectEntityIdList));
        int i = 0;
        int j = 0;
        for(String entityId : dishObjectEntityIdList) {
            if(!entityIdList.contains(entityId)) {
                i ++;
                bizLogger.info("[koubei history shop cook] not dealt entityId: {},  count: {}", entityId, i);
//                if(i > count) {
//                    break;
//                }
                Result<List<KbDishCookResponse>> result1 = null;
                try {
                    ShopBindVo shopBindVo = kouBeiCheckUtil.getShopBindVo(entityId);
                    if(shopBindVo == null || StringUtils.isEmpty(shopBindVo.getMerchantId())) {
                        bizLogger.error("[koubei history shop cook] 绑定关系不存在. entityId: {}", entityId);
                        new ResultSupport("400", "绑定关系不存在！");
                    }
                    result1 = kouBeiDishCookService.queryCookByMerchantIdShopId(shopBindVo.getMerchantId(), shopBindVo.getShopId());
                } catch (Exception e) {
                    bizLogger.error("[koubei history shop cook] entityId: {}", entityId, e);
                }
                bizLogger.info("[koubei history shop cook]  entityId: {}, result: {}", entityId, JSON.toJSONString(result1));
            } else {
                j ++;
                bizLogger.info("[koubei history shop cook] has dealt entityId: {},  count: {}", entityId, j);
            }
        }
        return new ResultSupport();
    }

	@Resource
	private ExportAdditionMapper exportAdditionMapper;

    @Resource
	private IWhiteListService whiteListService;

    @Resource
	private IGetTasteService getTasteService;

	@Override
	public Result batchSyncForAddition(String[] entityIdList) {
		if (null == entityIdList) {
			bizLogger.info("start add whiteList");
			int count = exportAdditionMapper.queryCount();
			bizLogger.info("count:"+count);
			int i=1;
			while (count > 0) {
				List<ExportAddition> list = exportAdditionMapper.selectAll();
				List<String> entityIdList1 = list.stream().map(exportAddition -> exportAddition.getEntityId()).collect(Collectors.toList());
				Result<Map<String, Boolean>> mapResult = whiteListService.getWhiteListMap(entityIdList1, EnumWhiteListType.ITEM_ADDITION);
				for (Map.Entry entry : mapResult.getModel().entrySet()) {
					if (entry.getValue().equals(Boolean.TRUE)) {
						entityIdList1.remove(entry.getKey());
						exportAdditionMapper.delete(entry.getKey().toString());
					}
				}

				bizLogger.info("entityIdList:{}" + JSON.toJSONString(entityIdList1));
				if (CollectionUtils.isNotEmpty(entityIdList1)) {

					for (String entityId : entityIdList1) {
						try {
							WhiteList whiteList = new WhiteList();
							whiteList.setEntityId(entityId);
							whiteList.setOpUserId("kbBinding");
							whiteList.setEntityType(EnumEntityType.SHOP.getCode());
							whiteList.setType(EnumWhiteListType.ITEM_ADDITION.getCode());
							whiteList.setIsValid(Boolean.TRUE);
							Result result = whiteListService.add2WhiteList(whiteList);

							if (!result.isSuccess()) {
								exportAdditionMapper.delete(entityId);
								bizLogger.info("kbBinding add whiteList is fail. entityId:{},reason;{}", entityId, result.getMessage());
							} else {
								exportAdditionMapper.delete(entityId);
								bizLogger.info("kbBinding add whiteList is success. entityId:{}, i:{}", entityId,i);
								i++;
							}
						} catch (Exception e) {
						}
					}
				}
				bizLogger.info("count:" + count);
				count = exportAdditionMapper.queryCount();
				bizLogger.info("change count:" + count);

			}


		} else {
			int count = 1;
			for (String entityId : entityIdList) {
				Result<Integer> cashVersion = shopBindClientService.getShopVersionByEntityId(entityId);
				if (cashVersion.getModel() >= 5810) {
					bizLogger.info("[店铺清单]. entityId:{},reason:{},count:{}", entityId, "收银版本高于5810:" + cashVersion.getModel(), count);
					count++;
				} else {
					Result<List<Menu>> listResult = getAdditionService.listAdditionAll(entityId);
					Result<List<KindAndTasteVo>> listResult1 = getTasteService.queryKindAndTasteList(entityId);
					List<Taste> tasteList = new ArrayList<>();
					for (KindAndTasteVo kindAndTasteVo : listResult1.getModel()) {
						if (CollectionUtils.isNotEmpty(kindAndTasteVo.getTasteList())) {
							tasteList.addAll(kindAndTasteVo.getTasteList());
						}
					}
					if (CollectionUtils.isEmpty(listResult.getModel()) && CollectionUtils.isEmpty(tasteList)) {
						//移除白名单
						whiteListService.removeWhiteList(entityId, EnumWhiteListType.ITEM_ADDITION);
						bizLogger.info("[店铺清单]. entityId:{},reason:{},count:{}", entityId, "该店铺符合条件移除白名单", count);
						count++;
					} else {
						bizLogger.info("[店铺清单]. entityId:{},reason:{},count:{}", entityId, "该店铺使用加料或者备注", count);
						count++;
					}
				}
			}
			/*List<WhiteList> whiteListList = new ArrayList<>();
			for (String entityId : entityIdList) {
				WhiteList whiteList = new WhiteList();
				whiteList.setEntityId(entityId);
				whiteList.setOpUserId("kbBinding");
				whiteList.setEntityType(EnumEntityType.SHOP.getCode());
				whiteList.setType(EnumWhiteListType.ITEM_ADDITION.getCode());
				whiteList.setIsValid(Boolean.TRUE);
				whiteListList.add(whiteList);
				Result whiteResult = whiteListService.add2WhiteList(whiteList);
				if (!whiteResult.isSuccess()) {
					bizLogger.info("kbBinding add whiteList is fail. whiteListList:{},reason;{}", JSONObject.toJSON(whiteListList), whiteResult.getMessage());
				}
			}*/
		}

		return null;
	}

	private Boolean syncAddition(String entityId) {
		ShopBindVo shopBindVo = kouBeiCheckUtil.getShopBindVo(entityId);
		String merchantId = shopBindVo.getMerchantId();
		String shopId = shopBindVo.getShopId();
		Result<List<Menu>> menuListResult = getAdditionService.listAdditionAll(entityId);
		if (!menuListResult.isSuccess()) {
			throw new BizException("[kb_databack][error]查询店铺加料信息失败");
		} else if (CollectionUtils.isNotEmpty(menuListResult.getModel())) {
			for (Menu menu : menuListResult.getModel()) {
				kouBeiCheckUtil.checkAffiliateId(merchantId, shopId, menu.getId(), entityId, true, null);
			}
		}
		return Boolean.FALSE;
	}
}
