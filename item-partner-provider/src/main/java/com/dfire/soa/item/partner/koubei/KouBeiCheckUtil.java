package com.dfire.soa.item.partner.koubei;

import com.alibaba.fastjson.JSON;
import com.dfire.open.takeout.bo.kb.*;
import com.dfire.open.takeout.enumeration.*;
import com.dfire.open.takeout.service.IKouBeiDishCookService;
import com.dfire.open.takeout.service.IKoubeiCommonService;
import com.dfire.soa.item.bo.*;
import com.dfire.soa.item.constants.ShopConstants;
import com.dfire.soa.item.dto.UnitExtDto;
import com.dfire.soa.item.partner.bo.*;
import com.dfire.soa.item.partner.bo.query.CookDetailQuery;
import com.dfire.soa.item.partner.bo.query.ItemMappingQuery;
import com.dfire.soa.item.partner.bo.query.ItemMenuMappingQuery;
import com.dfire.soa.item.partner.common.component.ShopBindCacheComponent;
import com.dfire.soa.item.partner.constant.CommonConstant;
import com.dfire.soa.item.partner.enums.EnumCookType;
import com.dfire.soa.item.partner.koubei.handler.KoubeiCookDishHandler;
import com.dfire.soa.item.partner.service.internal.ICookDetailInService;
import com.dfire.soa.item.partner.service.internal.ICookInService;
import com.dfire.soa.item.partner.service.internal.IItemMappingService;
import com.dfire.soa.item.partner.service.internal.impl.ItemMenuMappingService;
import com.dfire.soa.item.partner.util.ArithUtil;
import com.dfire.soa.item.partner.util.MD5Util;
import com.dfire.soa.item.query.MenuAdditionQuery;
import com.dfire.soa.item.query.SpecDetailQuery;
import com.dfire.soa.item.service.*;
import com.dfire.soa.item.vo.AdditionKindMenuVo;
import com.dfire.soa.item.vo.AdditionMenuVo;
import com.dfire.soa.thirdbind.service.IShopBindService;
import com.dfire.soa.thirdbind.vo.ShopBindVo;
import com.twodfire.exception.BizException;
import com.twodfire.redis.CodisService;
import com.twodfire.share.result.Result;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 口碑数据回流-工具类
 * Created by heque on 2018/5/11 0011.
 */
@Component
public class KouBeiCheckUtil {
    @Resource
    private IGetMenuService getMenuService;
    @Resource
    private IGetSuitMenuService getSuitMenuService;
    @Resource
    private IKouBeiDishCookService kouBeiDishCookService;
    @Resource
    private IShopBindService shopBindService;
    @Resource
    private IGetSpecDetailService getSpecDetailService;
    @Resource
    private IKoubeiCommonService koubeiCommonService;
    @Resource
    private IGetMakeService getMakeService;
    @Resource
    private CodisService codisService;
//    @Resource
//    private CodisService codisService;
    @Resource
    private KoubeiCookDishHandler koubeiCookDishHandler;
    @Resource
    private ICookInService cookInService;
    @Resource
    private ICookDetailInService cookDetailInService;
    @Resource
    private IItemMappingService itemMappingService;
    @Resource
    private ItemMenuMappingService itemMenuMappingService;
    @Resource
	private IGetAdditionService getAdditionService;
    @Resource
    private KouBeiDeleteUtil kouBeiDeleteUtil;
    @Resource
    private ShopBindCacheComponent shopBindCacheComponent;

    private static Logger bizLog = LoggerFactory.getLogger(CommonConstant.BIZ_LOG);

    /**
     * 获取规格Id(口碑)
     * @param merchantId merchantId
     * @param specId 规格Id
     * @param entityId entityId
     * @param isForceUpdate 是否强制更新（若已有映射关系）
     * @param lastVer 版本号（若有, 更新时需检查版本号。用于校验mq消息体的版本号）
     * @return 口碑规格id
     */
    public String checkSpecId(String merchantId, String shopId, String specId, String entityId, boolean isForceUpdate, Integer lastVer){
        String tpId = null;
        String platCode = String.valueOf(CommonConstant.KOUBEI_PLATFORM);
        byte idType = CommonConstant.ITEM_SPEC;
        String defaultSpecId = entityId + CommonConstant.KOUBEI_DEFAULT_SPEC_ID;
        String specName = CommonConstant.KOUBEI_DEFAULT_SPEC_NAME;

        //新增防重-自旋锁
        ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopId, platCode, (int)idType, specId, null);
        List<ItemMapping> itemMappings;
        ItemMapping itemMapping = null;
        Long cacheFlag;
        int count = 0;
        while(true) {
            count ++;
            if(count > 100) {
                break;
            }
            //查询规格spec映射关系(localId->tpId)
            itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
            itemMapping = CollectionUtils.isEmpty(itemMappings) ? null : itemMappings.get(0);
            tpId = itemMapping!=null && StringUtils.isNotBlank(itemMapping.getTpId()) ? itemMapping.getTpId() : null;
            if (StringUtils.isNotBlank(tpId)) {
                //是否强制更新菜类
                if (StringUtils.isNotBlank(tpId) && !isForceUpdate) {
                    return tpId;
                }
                break;
            }else {//需新增时，抢锁
                cacheFlag = codisService.setnx("specId"+ entityId + specId, 10, "true");
                if (cacheFlag != null && cacheFlag > 0) {
                    itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
                    itemMapping = CollectionUtils.isEmpty(itemMappings) ? null : itemMappings.get(0);
                    tpId = itemMapping!=null && StringUtils.isNotBlank(itemMapping.getTpId()) ? itemMapping.getTpId() : null;
                    break;
                }
            }
            try {
                Thread.sleep(CommonConstant.SLEEP_TIME);
            } catch (InterruptedException e) {
                throw new BizException("[kb_databack]线程中断异常");
            }
        }

        //查询规格
        if (!defaultSpecId.equals(specId)) {
            Result<List<Spec>> specListResult = getSpecDetailService.querySpecList(entityId);
            if (!specListResult.isSuccess() || CollectionUtils.isEmpty(specListResult.getModel()) || specListResult.getModel().stream().noneMatch(vo -> Objects.equals(vo.getId(), specId))) {
                bizLog.error("[kb_databack]getSpecDetailService.querySpecList(entityId) failed. entityId: " + JSON.toJSONString(entityId) + ", specId:" + JSON.toJSONString(specId) + " specListResult: " + JSON.toJSONString(specListResult));
                throw new BizException("[kb_databack]菜品规格已经删除，请添加规格后重新关联商品");
            }
            Spec spec = specListResult.getModel().stream().filter(vo -> Objects.equals(vo.getId(), specId)).findFirst().orElse(null);
            specName = spec.getName();
            if (StringUtils.isBlank(specName)){
                throw new BizException("[kb_databack]规格名称为空，请检查修改");
            }else if (specName.length()>32){
                throw new BizException("[kb_databack]规格名称超过32位字数限制，请检查修改");
            }
        }

        //历史脏数据处理（08/10前的规格，存在多对一的情况（二维火对口碑），删除二维火一个规格后，造成没有对应关系）TODO
        if(StringUtils.isNotEmpty(tpId)){
            Result<String> thdSpecResult = kouBeiDishCookService.querySpec(merchantId, specName);
            if(!thdSpecResult.isSuccess()){
                bizLog.warn("[kb_databack]kouBeiDishCookService.querySpec(merchantId,specName) failed. entityId: "+ JSON.toJSONString(entityId) + "specName:" + JSON.toJSONString(specName) + ", thdSpecResult: "+ JSON.toJSONString(thdSpecResult));
                throw new BizException("[kb_databack]查询口碑菜品规格失败:\""+ specName + "\"， "+ thdSpecResult.getMessage());
            }else if(StringUtils.isBlank(thdSpecResult.getModel())){
                tpId = null;
            }
        }

        //新增/修改规格（口碑）
        KbDishDictionaryRequest request = new KbDishDictionaryRequest();
        request.setName(specName);
        request.setMerchantId(merchantId);
        request.setStatus(KouBeiDishStatusEnum.open);
        request.setDictionaryId(tpId);
        request.setCreateUser("ADMIN");
        request.setUpdateUser("ADMIN");
        request.setExtInfo(JSON.toJSONString(new HashMap<>()));
        Result<String> addOrUpdateResult = kouBeiDishCookService.dishDictionarySync(request, StringUtils.isNotBlank(tpId) ? KouBeiSyncTypeEnum.update : KouBeiSyncTypeEnum.add, KouBeiDictionaryBizTypeEnum.spec);
        if(!addOrUpdateResult.isSuccess() && (addOrUpdateResult.getMessage().contains("操作的数据不存在") || addOrUpdateResult.getMessage().contains("操作的字典数据不存在或已删除"))){
            tpId = null;
            request.setDictionaryId(null);
            addOrUpdateResult = kouBeiDishCookService.dishDictionarySync(request, KouBeiSyncTypeEnum.add, KouBeiDictionaryBizTypeEnum.spec);
        }else if(!addOrUpdateResult.isSuccess() && defaultSpecId.equals(specId) && StringUtils.isNotBlank(tpId)){
            return tpId;
        }
        if (!addOrUpdateResult.isSuccess()) {
            bizLog.warn("[kb_databack]kouBeiDishCookService.dishDictionarySync(request, syncType, bizType) failed. request: " + JSON.toJSONString(request) + ", syncType: " + JSON.toJSONString(StringUtils.isNotBlank(tpId) ? KouBeiSyncTypeEnum.update : KouBeiSyncTypeEnum.add) + ", bizType: " + JSON.toJSONString(KouBeiDictionaryBizTypeEnum.spec) + ", addOrUpdateResult: " + JSON.toJSONString(addOrUpdateResult));
        }
        tpId = StringUtils.isNotBlank(tpId) ? tpId : addOrUpdateResult.isSuccess() ? addOrUpdateResult.getModel() : null;//规格id(口碑)

        //新增/修改规格映射关系
        itemMapping = itemMapping==null ? new ItemMapping(entityId, shopId, platCode, (int)idType, specId, null) : itemMapping;
        itemMapping.setTpId(tpId);
        itemMapping.setSyncStatus(addOrUpdateResult.isSuccess() ? 1 : 0);
        itemMapping.setSyncResult(addOrUpdateResult.getMessage());
        if (itemMapping.getId() == null) {
            itemMappingService.saveItemMapping(itemMapping);
        } else {
            itemMappingService.updateItemMapping(itemMapping);
        }

        //同步是否成功
        if(!addOrUpdateResult.isSuccess()){
            if(StringUtils.isBlank(tpId)){
                codisService.del("specId"+ entityId + specId);//新增去鎖
            }
            throw new BizException("[kb_databack]同步规格失败:\""+ request.getName() + "\"， "+ addOrUpdateResult.getMessage());
        }
        return tpId;
    }

    /**
     * 获取菜类id(口碑)
     * @param idType 类型 （CommonConstant.ITEM_CATEGORY：普通菜类  CommonConstant.ITEM_COOK_CATEGORY：菜谱菜类）
     * @param entityId entityId
     * @param kindMenuId 菜类id
     * @param isForceUpdate 是否强制更新（若已有映射关系）
     * @param lastVer 版本号（若有, 更新时需检查版本号。用于校验mq消息体的版本号）
     * @return 口碑菜品组id
     */
    public String checkKindMenuId(byte idType, String merchantId, String shopId, String entityId, String kindMenuId, boolean isForceUpdate, Integer lastVer){
        String tpId = null;
        String platCode = String.valueOf(CommonConstant.KOUBEI_PLATFORM);

        //校验菜类id
        if(StringUtils.isBlank(kindMenuId) || Objects.equals(kindMenuId, "0")){
            return null;
        }

        //新增防重-自旋锁
        ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopId, platCode, (int)idType, kindMenuId, null);
        List<ItemMapping> itemMappings;
        ItemMapping itemMapping = null;
        Long cacheFlag;
        int count = 0;
        while(true) {
            count ++;
            if(count > 100) {
                break;
            }
            //查询菜类映射关系(localId->tpId)
            itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
            itemMapping = CollectionUtils.isEmpty(itemMappings) ? null : itemMappings.get(0);
            tpId = itemMapping!=null && StringUtils.isNotBlank(itemMapping.getTpId()) ? itemMapping.getTpId() : null;
            if (StringUtils.isNotBlank(tpId)) {
                //是否强制更新菜类
                if (StringUtils.isNotBlank(tpId) && !isForceUpdate) {
                    return tpId;
                }
                break;
            }else {//需新增时，抢锁
                cacheFlag = codisService.setnx("KindMenuId"+ idType +entityId + kindMenuId, 10, "true");
                if (cacheFlag != null && cacheFlag > 0) {
                    itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
                    itemMapping = CollectionUtils.isEmpty(itemMappings) ? null : itemMappings.get(0);
                    tpId = itemMapping!=null && StringUtils.isNotBlank(itemMapping.getTpId()) ? itemMapping.getTpId() : null;
                    break;
                }
            }
            try {
                Thread.sleep(CommonConstant.SLEEP_TIME);
            } catch (InterruptedException e) {
                throw new BizException("[kb_databack]线程中断异常");
            }
        }

        //查询菜类
        Result<KindMenu> kindMenuResult = getMenuService.findKindMenu(entityId, kindMenuId);
        if(!kindMenuResult.isSuccess() || kindMenuResult.getModel()==null){
            bizLog.error("[kb_databack]getMenuService.findKindMenu(entityId, kindMenuId) failed. entityId: "+ JSON.toJSONString(entityId) +", kindMenuId: "+ JSON.toJSONString(kindMenuId) +", kindMenuResult: "+ JSON.toJSONString(kindMenuResult));
            throw new BizException("[kb_databack]菜品分类已经删除，请添加分类后重新关联商品");
        }else if (StringUtils.isBlank(kindMenuResult.getModel().getName())){
            throw new BizException("[kb_databack]菜类名称为空，请检查修改");
        }else if (kindMenuResult.getModel().getName().length()>32){
            throw new BizException("[kb_databack]菜类名称超过32位字数限制，请检查修改");
        }
		//加料菜过滤
		if (kindMenuResult.getModel().getIsInclude() == KindMenu.TYPE_ADDITION) {
			return tpId;
		}
        KindMenu kindMenu = kindMenuResult.getModel();

        //版本号校验
        if(lastVer!=null){
            if (lastVer < kindMenu.getLastVer()){//消息版本落后，不处理
                return tpId;//
            }else if (lastVer > kindMenu.getLastVer()){//消息版本超前，等待重试（抛异常）
                throw new BizException("[kb_databack]同步菜类时mq消息版本超前");
            }
        }

        //新增/修改菜类（口碑）
//        String parentCatetoryId = StringUtils.isNotBlank(kindMenu.getParentId()) && !kindMenu.getParentId().equals("0") && !kindMenu.getParentId().equals(kindMenu.getId()) ? this.checkKindMenuId(idType, merchantId, shopId, entityId, kindMenu.getParentId(), true, null) : null;
//        int level = StringUtils.isNotBlank(kindMenu.getParentId()) && !kindMenu.getParentId().equals("0") && !kindMenu.getParentId().equals(kindMenu.getId()) ? this.getKindMenuLevel(idType, shopId, entityId, kindMenu.getParentId())+1 : 1;
        Map<String, String> extInfoMap = new HashMap<>();
        String cateSort = kindMenu.getSortCode();
        cateSort = ArithUtil.addZeroForNum(cateSort, 8);  //  右补0处理
        extInfoMap.put("cateSort", cateSort);  //
        extInfoMap.put("cateType", idType==CommonConstant.ITEM_CATEGORY ? "dish" : "cook");
//        extInfoMap.put("parentCatetoryId", parentCatetoryId==null ? "" : parentCatetoryId);
        extInfoMap.put("level", String.valueOf(1));
        KbDishDictionaryRequest request = new KbDishDictionaryRequest();
        request.setName(kindMenu.getName());
        request.setMerchantId(merchantId);
        request.setStatus(KouBeiDishStatusEnum.open);
        request.setExtInfo(JSON.toJSONString(extInfoMap));
        request.setDictionaryId(tpId);
        request.setCreateUser("ADMIN");
        request.setUpdateUser("ADMIN");
        Result<String> addOrUpdateResult = kouBeiDishCookService.dishDictionarySync(request, StringUtils.isNotBlank(tpId) ? KouBeiSyncTypeEnum.update : KouBeiSyncTypeEnum.add, KouBeiDictionaryBizTypeEnum.catetory);
        if(!addOrUpdateResult.isSuccess() && (addOrUpdateResult.getMessage().contains("操作的数据不存在") || addOrUpdateResult.getMessage().contains("操作的字典数据不存在或已删除"))){
            tpId = null;
            request.setDictionaryId(null);
            addOrUpdateResult = kouBeiDishCookService.dishDictionarySync(request, KouBeiSyncTypeEnum.add, KouBeiDictionaryBizTypeEnum.catetory);
        }
        if (!addOrUpdateResult.isSuccess()) {
            bizLog.warn("[kb_databack]kouBeiDishCookService.dishDictionarySync(request, syncType, bizType) failed. request: " + JSON.toJSONString(request) + ", syncType: " + JSON.toJSONString(StringUtils.isNotBlank(tpId) ? KouBeiSyncTypeEnum.update : KouBeiSyncTypeEnum.add) + ", bizType: " + JSON.toJSONString(KouBeiDictionaryBizTypeEnum.catetory) + ", addOrUpdateResult: " + JSON.toJSONString(addOrUpdateResult));
        }
        tpId = StringUtils.isNotBlank(tpId) ? tpId : addOrUpdateResult.isSuccess() ? addOrUpdateResult.getModel() : null;//菜类id(口碑)

        //新增/修改菜类映射关系
        itemMapping = itemMapping==null ? new ItemMapping(entityId, shopId, platCode, (int)idType, kindMenuId, null) : itemMapping;
        itemMapping.setTpId(tpId);
        itemMapping.getItemMappingExt().setLevel(1);
        itemMapping.setSyncStatus(addOrUpdateResult.isSuccess() ? 1 : 0);
        itemMapping.setSyncResult(addOrUpdateResult.getMessage());
        if (itemMapping.getId() == null) { //新增
            itemMappingService.saveItemMapping(itemMapping);
        } else { //修改
            itemMappingService.updateItemMapping(itemMapping);
        }

        //同步是否成功
        if(!addOrUpdateResult.isSuccess()){
            if(StringUtils.isBlank(tpId)){
                codisService.del("KindMenuId"+ idType + entityId + kindMenuId);//新增去鎖
            }
            throw new BizException("[kb_databack]同步菜类失败:\""+ request.getName() + "\"， "+ addOrUpdateResult.getMessage());
        }
        return tpId;
    }

    /**
     * 获取单位id(口碑)
     * @param merchantId merchantId
     * @param entityId entityId
     * @param unitId 单位id
     * @param isForceUpdate 是否强制更新（若已有映射关系）
     * @param lastVer 版本号（若有, 更新时需检查版本号。用于校验mq消息体的版本号）
     * @return 口碑菜品组id
     */
    public String checkUnitId(String merchantId, String shopId, String entityId, String unitId, boolean isForceUpdate, Integer lastVer){
        String tpId = null;
        String platCode = String.valueOf(CommonConstant.KOUBEI_PLATFORM);
        byte idType = CommonConstant.ITEM_UNIT;

        //校验单位id
        if(StringUtils.isBlank(unitId)){
            return null;
        }

        //新增防重-自旋锁
        ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopId, platCode, (int)idType, unitId, null);
        List<ItemMapping> itemMappings;
        ItemMapping itemMapping = null;
        Long cacheFlag;
        int count = 0;
        while(true) {
            count ++;
            if(count > 100) {
                break;
            }
            //查询菜类映射关系(localId->tpId)
            itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
            itemMapping = CollectionUtils.isEmpty(itemMappings) ? null : itemMappings.get(0);
            tpId = itemMapping!=null && StringUtils.isNotBlank(itemMapping.getTpId()) ? itemMapping.getTpId() : null;
            if (StringUtils.isNotBlank(tpId)) {
                //是否强制更新
                if (StringUtils.isNotBlank(tpId) && !isForceUpdate) {
                    return tpId;
                }
                break;
            }else {//需新增时，抢锁
                cacheFlag = codisService.setnx("unitId"+ entityId + unitId, 10, "true");
                if (cacheFlag != null && cacheFlag > 0) {
                    itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
                    itemMapping = CollectionUtils.isEmpty(itemMappings) ? null : itemMappings.get(0);
                    tpId = itemMapping!=null && StringUtils.isNotBlank(itemMapping.getTpId()) ? itemMapping.getTpId() : null;
                    break;
                }
            }
            try {
                Thread.sleep(CommonConstant.SLEEP_TIME);
            } catch (InterruptedException e) {
                throw new BizException("[kb_databack]线程中断异常");
            }
        }

        //查询单位
        Result<List<UnitExtDto>> unitExtDtosResult = getMenuService.queryUnitExtV2(entityId, ShopConstants.INDUSTRY_RESTAURANT);
        if(!unitExtDtosResult.isSuccess() || CollectionUtils.isEmpty(unitExtDtosResult.getModel()) || unitExtDtosResult.getModel().stream().noneMatch(vo -> Objects.equals(vo.getUnitId(), unitId))){
            bizLog.error("[kb_databack]getMenuService.queryUnitExtV2(entityId, industry) failed. entityId: "+ JSON.toJSONString(entityId) +", industry: "+ JSON.toJSONString(ShopConstants.INDUSTRY_RESTAURANT) +", unitExtDtosResult: "+ JSON.toJSONString(unitExtDtosResult));
            throw new BizException("[kb_databack]查询菜品单位失败，或单位已删除");
        }
        UnitExtDto unitExtDto = unitExtDtosResult.getModel().stream().filter(vo -> Objects.equals(vo.getUnitId(), unitId)).findFirst().orElse(null);;
        if (StringUtils.isBlank(unitExtDto.getUnitDesc())){
            throw new BizException("[kb_databack]单位名称为空，请检查修改");
        }else if (unitExtDto.getUnitDesc().length()>32){
            throw new BizException("[kb_databack]单位名称超过32位字数限制，请检查修改");
        }

        //校验版本号
        if(lastVer!=null){
            if (lastVer < unitExtDto.getLastVer()){//消息版本落后，不处理
                return tpId;
            }else if (lastVer > unitExtDto.getLastVer()){//消息版本超前，等待重试（抛异常）
                throw new BizException("[kb_databack]同步单位时mq消息版本超前");
            }
        }

        //新增/修改单位（口碑）
        KbDishDictionaryRequest request = new KbDishDictionaryRequest();
        request.setName(unitExtDto.getUnitDesc());
        request.setMerchantId(merchantId);
        request.setStatus(KouBeiDishStatusEnum.open);
        request.setDictionaryId(tpId);
        request.setCreateUser("ADMIN");
        request.setUpdateUser("ADMIN");
        request.setExtInfo(JSON.toJSONString(new HashMap<>()));
        Result<String> addOrUpdateResult = kouBeiDishCookService.dishDictionarySync(request, StringUtils.isNotBlank(tpId) ? KouBeiSyncTypeEnum.update : KouBeiSyncTypeEnum.add, KouBeiDictionaryBizTypeEnum.unit);
        if(!addOrUpdateResult.isSuccess() && (addOrUpdateResult.getMessage().contains("操作的数据不存在") || addOrUpdateResult.getMessage().contains("操作的字典数据不存在或已删除"))){
            tpId = null;
            request.setDictionaryId(null);
            addOrUpdateResult = kouBeiDishCookService.dishDictionarySync(request, KouBeiSyncTypeEnum.add, KouBeiDictionaryBizTypeEnum.unit);
        }
        if (!addOrUpdateResult.isSuccess()) {
            bizLog.warn("[kb_databack]kouBeiDishCookService.dishDictionarySync(request, syncType, bizType) failed. request: " + JSON.toJSONString(request) + ", syncType: " + JSON.toJSONString(StringUtils.isNotBlank(tpId) ? KouBeiSyncTypeEnum.update : KouBeiSyncTypeEnum.add) + ", bizType: " + JSON.toJSONString(KouBeiDictionaryBizTypeEnum.unit) + ", addOrUpdateResult: " + JSON.toJSONString(addOrUpdateResult));
        }
        tpId = StringUtils.isNotBlank(tpId) ? tpId : addOrUpdateResult.isSuccess() ? addOrUpdateResult.getModel() : null;//单位id(口碑)

        //新增/修改单位映射关系
        itemMapping = itemMapping==null ? new ItemMapping(entityId, shopId, platCode, (int)idType, unitId, null) : itemMapping;
        itemMapping.setTpId(tpId);
        itemMapping.setSyncStatus(addOrUpdateResult.isSuccess() ? 1 : 0);
        itemMapping.setSyncResult(addOrUpdateResult.getMessage());
        if (itemMapping.getId() == null) {
            itemMappingService.saveItemMapping(itemMapping);
        } else {
            itemMappingService.updateItemMapping(itemMapping);
        }

        //同步是否成功
        if(!addOrUpdateResult.isSuccess()){
            if(StringUtils.isBlank(tpId)){
                codisService.del("unitId"+ entityId + unitId);//新增去鎖
            }
            throw new BizException("[kb_databack]同步单位失败:\""+ request.getName() + "\"， "+ addOrUpdateResult.getMessage());
        }
        return tpId;
    }

    /**
     * 获取菜品组id(口碑)
     * @param merchantId merchantId
     * @param entityId entityId
     * @param suitMenuDetailId 菜品组id
     * @param isForceUpdate 是否强制更新（若已有映射关系）
     * @param lastVer 版本号（若有, 更新时需检查版本号。用于校验mq消息体的版本号）
     * @return 口碑菜品组id
     */
    public String checkGroupId(String merchantId, String shopId, String entityId, String suitMenuDetailId, boolean isForceUpdate, Integer lastVer){
        String tpId = null;
        String platCode = String.valueOf(CommonConstant.KOUBEI_PLATFORM);
        byte idType = CommonConstant.ITEM_GROUP;
        List<KbdishGroupDetail> kbdishGroupDetails = new ArrayList<>();

        //新增防重-自旋锁
        ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopId, platCode, (int)idType, suitMenuDetailId, null);
        List<ItemMapping> itemMappings;
        ItemMapping itemMapping = null;
        Long cacheFlag;
        int count = 0;
        while(true) {
            count ++;
            if(count > 100) {
                break;
            }
            //查询菜品组映射关系(localId->tpId)
            itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
            itemMapping = CollectionUtils.isEmpty(itemMappings) ? null : itemMappings.get(0);
            tpId = itemMapping!=null && StringUtils.isNotBlank(itemMapping.getTpId()) ? itemMapping.getTpId() : null;
            if (StringUtils.isNotBlank(tpId)) {
                //是否强制更新菜类
                if (StringUtils.isNotBlank(tpId) && !isForceUpdate) {
                    return tpId;
                }
                break;
            }else {//需新增时，抢锁
                cacheFlag = codisService.setnx("suitMenuDetailId"+ entityId + suitMenuDetailId, 10, "true");
                if (cacheFlag != null && cacheFlag > 0) {
                    itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
                    itemMapping = CollectionUtils.isEmpty(itemMappings) ? null : itemMappings.get(0);
                    tpId = itemMapping!=null && StringUtils.isNotBlank(itemMapping.getTpId()) ? itemMapping.getTpId() : null;
                    break;
                }
            }
            try {
                Thread.sleep(CommonConstant.SLEEP_TIME);
            } catch (InterruptedException e) {
                throw new BizException("[kb_databack]线程中断异常");
            }
        }

        //查询菜品组信息（suitMenuDetailId->groupId）
        Result<SuitMenuDetail> suitMenuDetailResult = getSuitMenuService.findSuitMenuDetail(entityId, suitMenuDetailId);
        if (!suitMenuDetailResult.isSuccess() || suitMenuDetailResult.getModel()==null){
            bizLog.error("[kb_databack]getSuitMenuService.findSuitMenuDetail(entityId, suitMenuDetailId) failed. entityId: "+ JSON.toJSONString(entityId)+ ", suitMenuDetailId: "+ JSON.toJSONString(suitMenuDetailId)+ ", suitMenuDetailResult: "+ JSON.toJSONString(suitMenuDetailResult));
            throw new BizException("[kb_databack]查询套餐分组信息失败，或套餐分组已删除");
        }else if(suitMenuDetailResult.getModel().getIsRequired() == 1){//必选菜品组不用同步
            if(StringUtils.isBlank(tpId)){
                codisService.del("suitMenuDetailId"+ entityId + suitMenuDetailId);//新增去鎖
            }
            return null;
        } else if (StringUtils.isBlank(suitMenuDetailResult.getModel().getName())){
            throw new BizException("[kb_databack]套餐分组名称为空，请检查修改");
        } else if (suitMenuDetailResult.getModel().getName().length()>32){
            throw new BizException("[kb_databack]套餐分组名称超过32位字数限制，请检查修改");
        }

        //版本号校验
        if(lastVer!=null){
            if (lastVer < suitMenuDetailResult.getModel().getLastVer()){//消息版本落后，不处理
                return tpId;//
            }else if (lastVer > suitMenuDetailResult.getModel().getLastVer()){//消息版本超前，等待重试（抛异常）
                throw new BizException("[kb_databack]同步套餐分组时mq消息版本超前");
            }
        }

        //查询菜品组-可选菜信息
        int suitMenuDetailNum = suitMenuDetailResult.getModel().getNum()<=0 ? 99 : (int)suitMenuDetailResult.getModel().getNum();
        int detailCountNum = 0;
        List<String> suitMenuDetailIds = new ArrayList<>();
        suitMenuDetailIds.add(suitMenuDetailId);
        Result<List<SuitMenuChange>> suitMenuChangesResult = getSuitMenuService.queryChangeListBySuitMenuDetailIdList(entityId, suitMenuDetailIds);
        if (!suitMenuChangesResult.isSuccess()){
            bizLog.error("[kb_databack]getSuitMenuService.queryChangeListBySuitMenuDetailIdList(entityId, suitMenuDetailIds) failed. entityId: "+ JSON.toJSONString(entityId)+ ", suitMenuDetailIds: "+ JSON.toJSONString(suitMenuDetailIds)+ ", suitMenuChangesResult: "+ JSON.toJSONString(suitMenuChangesResult));
            throw new BizException("[kb_databack]查询套餐分组信息失败，或套餐分组已删除");
        }else if(CollectionUtils.isNotEmpty(suitMenuChangesResult.getModel())) {
            Set<String> dishIds = new HashSet<>();
            for (SuitMenuChange suitMenuChange : suitMenuChangesResult.getModel()) {
                int detailCount = suitMenuChange.getSuitMenuChangeExtra()==null || suitMenuChange.getSuitMenuChangeExtra().getLimit_num()==null || suitMenuChange.getSuitMenuChangeExtra().getLimit_num()<=0 || suitMenuChange.getSuitMenuChangeExtra().getLimit_num()>suitMenuDetailNum? suitMenuDetailNum: suitMenuChange.getSuitMenuChangeExtra().getLimit_num();
                detailCountNum = detailCountNum + detailCount;
                KbdishGroupDetail kbdishGroupDetail = new KbdishGroupDetail();
                kbdishGroupDetail.setAddPrice(String.valueOf(suitMenuChange.getPrice()));
                kbdishGroupDetail.setDetailCount(String.valueOf(detailCount));
                kbdishGroupDetail.setDetailDishId(this.checkDishId(merchantId, shopId, entityId, suitMenuChange.getMenuId(), false, null, null));
                kbdishGroupDetail.setDetailIsDefault(suitMenuChange.getIsRequired() == 1 ? "Y" : "N");
                kbdishGroupDetail.setDetailSkuId(this.checkSkuId(merchantId, shopId, entityId, suitMenuChange.getMenuId(), suitMenuChange.getSpecDetailId(), false, null, null));
                kbdishGroupDetail.setGroupId(tpId);
                kbdishGroupDetail.setDetailSort(String.valueOf(suitMenuChange.getSortCode()));
                if(StringUtils.isBlank(kbdishGroupDetail.getDetailSkuId()) || StringUtils.isBlank(kbdishGroupDetail.getDetailDishId())){
                    bizLog.info("[kb_databack][ignored_suitMenuChange] getSuitMenuService.queryChangeListBySuitMenuDetailIdList(entityId, suitMenuDetailIds) . entityId: "+ JSON.toJSONString(entityId)+ ", suitMenuDetailId: "+ JSON.toJSONString(suitMenuDetailIds) +",suitMenuChangesResult: "+ JSON.toJSONString(suitMenuChangesResult)+ ", suitMenuChange: "+ JSON.toJSONString(suitMenuChange));
                    continue;
                }
                kbdishGroupDetails.add(kbdishGroupDetail);

                //套餐分组下应不含相同子菜
                if(dishIds.contains(kbdishGroupDetail.getDetailSkuId())){
                    throw new BizException("[kb_databack]套餐分组含相同子菜，请检查修改");
                }
                dishIds.add(kbdishGroupDetail.getDetailSkuId());
            }
        }
        if(CollectionUtils.isEmpty(kbdishGroupDetails)){
            bizLog.info("[kb_databack][ignored_suitMenuDetail] getSuitMenuService.queryChangeListBySuitMenuDetailIdList(entityId, suitMenuDetailIds) . entityId: "+ JSON.toJSONString(entityId)+ ", suitMenuDetailId: "+ JSON.toJSONString(suitMenuDetailIds) +",suitMenuChangesResult: "+ JSON.toJSONString(suitMenuChangesResult));
            if(StringUtils.isBlank(tpId)){
                codisService.del("suitMenuDetailId"+ entityId + suitMenuDetailId);//新增去鎖
            }
            return null;
        }

        //新增/修改菜品组(口碑)
        KbdishGroupRequest request = new KbdishGroupRequest();
        KbdishGroup kbdishGroup = new KbdishGroup();
        kbdishGroup.setGroupId(tpId);
        kbdishGroup.setMerchantId(merchantId);
        kbdishGroup.setGroupName(suitMenuDetailResult.getModel().getName());
        kbdishGroup.setUnitCountLimit(String.valueOf(suitMenuDetailNum));//数量限制
        kbdishGroup.setDetailList(kbdishGroupDetails);
        kbdishGroup.setStatus(KouBeiDishStatusEnum.open);
        kbdishGroup.setGroupVersion(String.valueOf(System.currentTimeMillis()));
        kbdishGroup.setCreateUser("ADMIN");
        kbdishGroup.setUpdateUser("ADMIN");
        request.setKbdishGroup(kbdishGroup);
        Result<KbdishGroupResponse> addOrUpdateResult = kouBeiDishCookService.dishGroupSync(request, StringUtils.isNotBlank(kbdishGroup.getGroupId()) ? KouBeiSyncTypeEnum.update : KouBeiSyncTypeEnum.add);
        if(!addOrUpdateResult.isSuccess()) {
            bizLog.warn("[kb_databack]kouBeiDishCookService.dishGroupSync(request, syncType) failed. request: " + JSON.toJSONString(request) + ", syncType: " + JSON.toJSONString(StringUtils.isNotBlank(kbdishGroup.getGroupId()) ? KouBeiSyncTypeEnum.update : KouBeiSyncTypeEnum.add) + ", addOrUpdateResult: " + JSON.toJSONString(addOrUpdateResult));
        }
        tpId = StringUtils.isNotBlank(tpId) ? tpId : !addOrUpdateResult.isSuccess() || addOrUpdateResult.getModel()==null || addOrUpdateResult.getModel().getKbdishGroup()==null ? null : addOrUpdateResult.getModel().getKbdishGroup().getGroupId();

        //新增/修改菜品组映射关系
        itemMapping = itemMapping==null ? new ItemMapping(entityId, shopId, platCode, (int)idType, suitMenuDetailId, null) : itemMapping;
        itemMapping.setTpId(tpId);
        itemMapping.setSyncStatus(addOrUpdateResult.isSuccess() ? 1 : 0);
        itemMapping.setSyncResult(!addOrUpdateResult.isSuccess() && suitMenuDetailNum>detailCountNum ? "套餐内子菜总和要大于分组数量":  addOrUpdateResult.getMessage());
        if (itemMapping.getId() == null) { //新增
            itemMappingService.saveItemMapping(itemMapping);
        } else { //修改
            itemMappingService.updateItemMapping(itemMapping);
        }

        //同步是否成功
        if(!addOrUpdateResult.isSuccess()){
            if(StringUtils.isBlank(tpId)){
                codisService.del("suitMenuDetailId"+ entityId + suitMenuDetailId);//新增去鎖
            }
            throw new BizException("[kb_databack]同步套餐分组失败:\""+ kbdishGroup.getGroupName() + "\"， "+  (suitMenuDetailNum>detailCountNum ? "套餐内子菜总和要大于分组数量" :addOrUpdateResult.getMessage()));
        }
        return tpId;
    }

    /**
     * 获取菜品id(口碑)
     * @param merchantId merchantId
     * @param entityId entityId
     * @param menuId 菜品id
     * @param isForceUpdate 是否强制更新（若已有映射关系）
     * @param lastVer 版本号（若有, 更新时需检查版本号。用于校验mq消息体的版本号）
     * @param kbDish 通用参数（若有,使用通用参数）
     * @return 口碑菜品id
     */
    public String checkDishId(String merchantId, String shopId, String entityId, String menuId, boolean isForceUpdate, Integer lastVer, KbDish kbDish){
        String dishId = null;
        String platCode = String.valueOf(CommonConstant.KOUBEI_PLATFORM);
        byte idType = CommonConstant.ITEM;
        String defaultSpecId = entityId + CommonConstant.KOUBEI_DEFAULT_SPEC_ID;
        boolean isNeedSave = true;
        String thisDishName = "";

        //新增防重-自旋锁
        ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopId, platCode, (int)idType, menuId, null);
        List<ItemMapping> itemMappings;
        ItemMapping itemMapping = null;
        ItemMapping itemMapping8 = null;
        Long cacheFlag;
        int count = 0;
        while (true){
            count ++;
            if(count > 100) {
                break;
            }
            //查询菜品映射关系
            itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
            itemMapping = CollectionUtils.isEmpty(itemMappings) ? null : itemMappings.get(0);
            dishId = itemMapping!=null && StringUtils.isNotBlank(itemMapping.getTpId()) ? itemMapping.getTpId() : null;
            if (StringUtils.isNotBlank(dishId)) {
                //是否强制更新菜类
                if (StringUtils.isNotBlank(dishId) && !isForceUpdate) {
                    return dishId;
                }
                //更新时抢锁
                cacheFlag = codisService.setnx("dish"+ entityId + menuId, 2, "true");//更新加锁2秒
                if (cacheFlag != null && cacheFlag > 0) {
                    break;
                }
                //否则sleep()
            }else {
                //需新增dish时，抢锁
                cacheFlag = codisService.setnx("dish"+ entityId + menuId, 10, "true");
                if (cacheFlag != null && cacheFlag > 0) {
                    itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
                    itemMapping = CollectionUtils.isEmpty(itemMappings) ? null : itemMappings.get(0);
                    dishId = itemMapping!=null && StringUtils.isNotBlank(itemMapping.getTpId()) ? itemMapping.getTpId() : null;
                    break;
                }
            }
            try {
                Thread.sleep(CommonConstant.SLEEP_TIME);
            } catch (InterruptedException e) {
                throw new BizException("[kb_databack]线程中断异常");
            }
        }

        //捕获业务异常并记录
        try {
            //查询菜品
            Result<Menu> menuResult = getMenuService.findMenuAndProp(entityId, menuId);
            if(!menuResult.isSuccess() || menuResult.getModel()==null){
                isNeedSave = false;
                bizLog.error("[kb_databack]getMenuService.findMenuAndProp(entityId, menuId) is fail! entityId: " + JSON.toJSONString(entityId) + "menuId: " + JSON.toJSONString(menuId) + " menuResult: " + JSON.toJSONString(menuResult));
                throw new BizException("[kb_databack]套餐内子菜不能单独删除");
            }
            Menu menu = menuResult.getModel();
            thisDishName = menu.getName();
            if(StringUtils.isNotBlank(menu.getCode()) && menu.getCode().length()>32){
                throw new BizException("[kb_databack]菜品编码超过32位字数限制，请检查修改");
            }else if(StringUtils.isBlank(menu.getName())){
                throw new BizException("[kb_databack]菜品名称为空，请检查修改");
            }else if(StringUtils.isNotBlank(menu.getSpell()) && menu.getSpell().length()>32){
                throw new BizException("[kb_databack]菜品名称超过32位字数限制，请检查修改");
            }else if(menu.getPrice()>5000 || menu.getMemberPrice()>5000){
                throw new BizException("[kb_databack]菜品价格大于5000，请检查修改");
            }
            thisDishName = menu.getName();

            //版本号校验
            if(lastVer!=null){
                if (lastVer < menuResult.getModel().getLastVer()){//消息版本落后，不处理
                    return dishId;
                }else if (lastVer > menuResult.getModel().getLastVer()){//消息版本超前，等待重试（抛异常）
                    throw new BizException("[kb_databack]同步数据处理中，请重试");
                }
            }

            final String finalDishId = dishId;
            String goodsId = itemMapping != null && StringUtils.isNotBlank(itemMapping.getItemMappingExt().getGoodsId()) ? itemMapping.getItemMappingExt().getGoodsId() : null;
            String imgUrl = StringUtils.isBlank(menu.getImagePath()) ? null : menu.getImagePath();//二维火图片url
            String imgUrlCode = StringUtils.isBlank(imgUrl) ? null : (itemMapping == null || StringUtils.isBlank(itemMapping.getItemMappingExt().getImgUrlCode()) || !imgUrl.equals(itemMapping.getItemMappingExt().getImgUrl())) ? this.checkImgUrlCode(entityId, imgUrl) : itemMapping.getItemMappingExt().getImgUrlCode();//口碑图片码



            //新增/修改菜品（口碑）
            KbDishRequest request = new KbDishRequest();
            kbDish = kbDish!=null && kbDish.getDishName()!=null ? kbDish : this.addKbDishParams(kbDish, menu, merchantId, shopId, entityId, menuId);//添加菜品/sku的共通参数
            if (kbDish==null) {
                return null;
            }
            List<KbDishSku> dishSkuList = kbDish.getDishSkuList();
            kbDish.getDishPracticeList().forEach(kbDishPractice -> kbDishPractice.setDishId(finalDishId));//赋值dishId
            kbDish.setDishId(finalDishId);
            kbDish.setGoodsId(goodsId);
            kbDish.setDishImg(imgUrlCode);
            {//赋值sku相关信息
                if ("packages".equals(kbDish.getTypeBig())) {
                    //查询sku映射
                    ItemMappingQuery itemMappingQuery8 = new ItemMappingQuery(entityId, shopId, platCode, (int) CommonConstant.ITEM_SKU, menuId, null);
                    itemMappingQuery8.setCommonId(defaultSpecId);
                    List<ItemMapping> itemMappings8 = itemMappingService.getItemMappingListByQuery(itemMappingQuery8);
                    itemMapping8 = CollectionUtils.isEmpty(itemMappings8) ? null : itemMappings8.get(0);

                    KbDishSku kbDishSku = kbDish.getDishSkuList().get(0);
                    final String finalSkuId = itemMapping8 == null || StringUtils.isBlank(itemMapping8.getTpId()) ? null : itemMapping8.getTpId();
                    kbDishSku.getDishPackagesDetailList().forEach(kbDishPackagesDetail -> kbDishPackagesDetail.setPackagesSkuId(finalSkuId));
                    kbDishSku.setSkuId(finalSkuId);
                    kbDishSku.setDishId(finalDishId);
                    kbDishSku.setSpecCode01(this.checkSpecId(merchantId, shopId, defaultSpecId, entityId, false, null));
                    kbDishSku.setSellPrice(String.valueOf(menu.getPrice()));
                    kbDishSku.setMemberPrice(String.valueOf(menu.getMemberPrice()));//会员价
                    kbDishSku.setSkuSort(Integer.toString(menu.getSortCode()));
                } else {
                    kbDish.setDishSkuList(null);//请求完需要加回去
                }
            }
            request.setKbDish(kbDish);
            {//重复性请求校验
                String dishVersion = kbDish.getDishVersion();//这个字段一直在变
                kbDish.setDishVersion(null);
                if (!this.checkMd5("dish" + entityId + menuId, request, StringUtils.isNotBlank(dishId) ? KouBeiSyncTypeEnum.update : KouBeiSyncTypeEnum.add, KouBeiDishBizTypeEnum.dish)) {//重复性校验
                    isNeedSave = false;
                    throw new BizException("[kb_databack]重复的菜品同步信息，请重试");
                }
                kbDish.setDishVersion(dishVersion);
            }
            Result<KbDishResponse> addOrUpdateResult = kouBeiDishCookService.dishSync(request, StringUtils.isNotBlank(dishId) ? KouBeiSyncTypeEnum.update : KouBeiSyncTypeEnum.add, KouBeiDishBizTypeEnum.dish);
            if(!addOrUpdateResult.isSuccess() && addOrUpdateResult.getMessage().contains("商户下的菜品已经存在,不能重复创建")){
                boolean isSameName = false;

                //同名菜品校验
                String kbDishId = addOrUpdateResult.getModel().getKbDish().getDishId();
                ItemMappingQuery itemMappingQuerySameName = new ItemMappingQuery(entityId, shopId, platCode, (int)CommonConstant.ITEM, null, kbDishId);
                List<ItemMapping> itemMappingsSameName = itemMappingService.getItemMappingListByQuery(itemMappingQuerySameName);
                if(CollectionUtils.isNotEmpty(itemMappingsSameName)){
                    isSameName = true;
                }

                //删除口碑的同名菜品（非二维火同步）
                if(!isSameName) {
                    KbDishRequest requestSameName = new KbDishRequest();
                    requestSameName.setKbDish(addOrUpdateResult.getModel().getKbDish());
                    Result<KbDishResponse> deleteResult = kouBeiDishCookService.dishSync(requestSameName, KouBeiSyncTypeEnum.del, KouBeiDishBizTypeEnum.dish);
                    if (!deleteResult.isSuccess()) {
                        bizLog.warn("[kb_databack] kouBeiDishCookService.dishSync(request, syncType, bizType) failed. request:{}, syncType:{}, bizType{}, deleteResult:{}", JSON.toJSONString(request), KouBeiSyncTypeEnum.del, KouBeiDishBizTypeEnum.dish, JSON.toJSON(deleteResult));
                        throw new BizException("[kb_databack]删除口碑同名菜品失败:" + kbDish.getDishName() + ", " + deleteResult.getMessage());
                    }

                    //重新同步
                    addOrUpdateResult = kouBeiDishCookService.dishSync(request, StringUtils.isNotBlank(dishId) ? KouBeiSyncTypeEnum.update : KouBeiSyncTypeEnum.add, KouBeiDishBizTypeEnum.dish);
                }
            }else if (!addOrUpdateResult.isSuccess() && (addOrUpdateResult.getMessage().contains("操作的数据不存在") || addOrUpdateResult.getMessage().contains("商户不能操作当前数据") || addOrUpdateResult.getMessage().contains("参数有误参数有问题"))) {
                //删除映射数据
                this.deleteItemMappingAndItemMenuMapping(entityId, shopId, menuId);

                //重新同步
                dishId=null;
                request.getKbDish().setDishId(null);
                addOrUpdateResult = kouBeiDishCookService.dishSync(request, StringUtils.isNotBlank(dishId) ? KouBeiSyncTypeEnum.update : KouBeiSyncTypeEnum.add, KouBeiDishBizTypeEnum.dish);
            }

            if (!addOrUpdateResult.isSuccess()) {
                bizLog.warn("[kb_databack]kouBeiDishCookService.dishSync(request, syncType, bizType) failed. request: " + JSON.toJSONString(request) + ", syncType: " + JSON.toJSONString(StringUtils.isNotBlank(dishId) ? KouBeiSyncTypeEnum.update : KouBeiSyncTypeEnum.add) + ", bizType: " + JSON.toJSONString(KouBeiDishBizTypeEnum.dish) + ", addOrUpdateResult: " + JSON.toJSONString(addOrUpdateResult));
            }
            dishId = StringUtils.isNotBlank(dishId) ? dishId : !addOrUpdateResult.isSuccess() || addOrUpdateResult.getModel() == null || addOrUpdateResult.getModel().getKbDish() == null ? null : addOrUpdateResult.getModel().getKbDish().getDishId();
            kbDish.setDishSkuList(dishSkuList);//加回

            //新增/修改菜品映射关系
            itemMapping = itemMapping == null ? new ItemMapping(entityId, shopId, platCode, (int) idType, menuId, null) : itemMapping;
            itemMapping.setTpId(dishId);
            itemMapping.getItemMappingExt().setGoodsId(addOrUpdateResult.isSuccess() && addOrUpdateResult.getModel() != null && addOrUpdateResult.getModel().getKbDish() != null ? addOrUpdateResult.getModel().getKbDish().getGoodsId() : null);
            itemMapping.getItemMappingExt().setImgUrl(imgUrl);
            itemMapping.getItemMappingExt().setImgUrlCode(imgUrlCode);
            itemMapping.setSyncStatus(addOrUpdateResult.isSuccess() ? 1 : 0);
            itemMapping.setSyncResult(addOrUpdateResult.getMessage());
            if (itemMapping.getId() == null) {
                itemMappingService.saveItemMapping(itemMapping);
            } else {
                itemMappingService.updateItemMapping(itemMapping);
            }

            //新增/修改sku映射关系(套餐，只有一个默认sku)
            if (addOrUpdateResult.isSuccess() && "packages".equals(kbDish.getTypeBig())) {
                itemMapping8 = itemMapping8 == null ? new ItemMapping(entityId, shopId, platCode, (int) CommonConstant.ITEM_SKU, menuId, null) : itemMapping8;
                itemMapping8.setTpId(addOrUpdateResult.getModel().getKbDish().getDishSkuList().get(0).getSkuId());
                itemMapping8.setCommonId(defaultSpecId);
                itemMapping8.setSyncStatus(addOrUpdateResult.isSuccess() ? 1 : 0);
                itemMapping8.setSyncResult(addOrUpdateResult.getMessage());
                if (itemMapping8.getId() == null) {
                    itemMappingService.saveItemMapping(itemMapping8);
                } else {
                    itemMappingService.updateItemMapping(itemMapping8);
                }
            }

			//同步完菜品加料之后新增/修改菜品加料映射关系
			Map<String, String> map = new HashMap<>();
			ItemMappingQuery itemMappingQuery9 = new ItemMappingQuery(entityId, shopId, platCode, (int) CommonConstant.ADDITION, null, null);
			List<ItemMapping> itemMappings9 = itemMappingService.getItemMappingListByQuery(itemMappingQuery9);
			if (CollectionUtils.isNotEmpty(itemMappings9)) {
				for (ItemMapping itemMapping9 : itemMappings9) {
					map.put(itemMapping9.getTpId(), itemMapping9.getLocalId());
				}
			}
			if (addOrUpdateResult.isSuccess()) {
				List<KbdishMaterialBinding> kbdishMaterialBindingList = kbDish.getMaterialBindingList();
				List<String> tpIdList = kbdishMaterialBindingList.stream().map(kbdishMaterialBinding -> kbdishMaterialBinding.getMaterialId()).collect(Collectors.toList());
				ItemMappingQuery itemMappingQuery10 = new ItemMappingQuery(entityId, shopId, platCode, (int) CommonConstant.ITEM_ADDITION, menuId, null);
				List<ItemMapping> itemMappingList10 = itemMappingService.getItemMappingListByQuery(itemMappingQuery10);
				Iterator<ItemMapping> it = itemMappingList10.iterator();
				while (it.hasNext()) {
					ItemMapping itemMapping10 = it.next();
					if (tpIdList.contains(itemMapping10.getTpId())) {
						it.remove();
					}
				}
				if (CollectionUtils.isNotEmpty(itemMappingList10)) {
					for (ItemMapping itemMapping1 : itemMappingList10) {
						itemMappingService.deleteItemMappingById(entityId, itemMapping1.getId());
					}
				}
				for(KbdishMaterialBinding kbdishMaterialBinding:kbdishMaterialBindingList){
					itemMappingQuery10.setTpId(kbdishMaterialBinding.getMaterialId());
					List<ItemMapping> itemMappings10 = itemMappingService.getItemMappingListByQuery(itemMappingQuery10);
					ItemMapping itemMapping10 = CollectionUtils.isEmpty(itemMappings10) ? new ItemMapping(entityId, shopId, platCode, (int) CommonConstant.ITEM_ADDITION, menuId, kbdishMaterialBinding.getMaterialId()) : itemMappings10.get(0);
					itemMapping10.setCommonId(map.get(kbdishMaterialBinding.getMaterialId()));
					itemMapping10.setSyncStatus(addOrUpdateResult.isSuccess() ? 1 : 0);
					itemMapping10.setSyncResult(addOrUpdateResult.getMessage());
					itemMapping10.setItemMappingExt(new ItemMappingExt());
					if (itemMapping10.getId() == null) {
						itemMappingService.saveItemMapping(itemMapping10);
					}/* else {
						itemMappingService.updateItemMapping(itemMapping10);
					}*/
				}

			}

            //同步是否成功
            if (!addOrUpdateResult.isSuccess()) {
                isNeedSave = false;
                throw new BizException("[kb_databack]同步菜品失败:" + kbDish.getDishName() + ", " + addOrUpdateResult.getMessage());
            }

            return dishId;
        }catch (BizException e){
            //记录非该菜品本身同步造成的错误信息
            if(isNeedSave) {//
                itemMapping = itemMapping == null ? new ItemMapping(entityId, shopId, platCode, (int) idType, menuId, null) : itemMapping;
                itemMapping.setTpId(dishId);
                itemMapping.setSyncStatus(0);
                itemMapping.setSyncResult(e.getMessage().replaceAll("\\[kb_databack\\]", ""));
                if (itemMapping.getId() == null) {
                    itemMappingService.saveItemMapping(itemMapping);
                } else {
                    itemMappingService.updateItemMapping(itemMapping);
                }
                throw new BizException("[kb_databack]同步菜品失败："+ thisDishName + ", " +e.getMessage());
            }
            throw e;
        }finally {
            codisService.del("dish" + entityId + menuId);//新增去鎖
        }
    }

    /**
     * 获取skuId(口碑)
     * @param merchantId merchantId
     * @param entityId entityId
     * @param menuId 菜品id
     * @param specDetailId 规格详情id
     * @param isForceUpdate 是否强制更新（若已有映射关系）
     * @param lastVer 版本号（若有, 更新时需检查版本号。用于校验mq消息体的版本号）
     * @param kbDish 通用参数（若有,使用通用参数）
     * @return 口碑菜品id
     */
    public String checkSkuId(String merchantId, String shopId, String entityId, String menuId, String specDetailId, boolean isForceUpdate, Integer lastVer, KbDish kbDish){
        String skuId = null;
        String platCode = String.valueOf(CommonConstant.KOUBEI_PLATFORM);
        byte idType = CommonConstant.ITEM_SKU;
        String defaultSpecId = entityId + CommonConstant.KOUBEI_DEFAULT_SPEC_ID;
        String specName = CommonConstant.KOUBEI_DEFAULT_SPEC_NAME;

        //查询规格Id
        String specId;
        if(StringUtils.isNotBlank(specDetailId)) {
            Result<SpecDetail> specDetailResult = getSpecDetailService.querySpecDetail(specDetailId, entityId);
            if (!specDetailResult.isSuccess() || specDetailResult.getModel() == null) {
                bizLog.error("[kb_databack]getSpecDetailService.querySpecDetail(specDetailId, entityId) failed. specDetailId: " + JSON.toJSONString(specDetailId) + ", entityId: " + JSON.toJSONString(entityId) + "menuSpecDetailsResult :" + JSON.toJSONString(specDetailResult));
                throw new BizException("[kb_databack]菜品规格已经删除，请添加规格后重新关联商品");
            }
            specId = specDetailResult.getModel().getSpecId();
            specName = specDetailResult.getModel().getName();
        }else {
            specId = defaultSpecId;
        }

        //新增防重-自旋锁
        ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopId, platCode, (int)idType, menuId, null);
        itemMappingQuery.setCommonId(specId);
        List<ItemMapping> itemMappings;
        ItemMapping itemMapping = null;
        Long cacheFlag;
        int count = 0;
        while (true) {
            count ++;
            if(count > 100) {
                break;
            }
            //查询菜品sku映射关系
            itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
            itemMapping = CollectionUtils.isEmpty(itemMappings) ? null : itemMappings.get(0);
            skuId = itemMapping!=null && StringUtils.isNotBlank(itemMapping.getTpId()) ? itemMapping.getTpId() : null;
            if (StringUtils.isNotBlank(skuId)) {
                //是否强制更新
                if (StringUtils.isNotBlank(skuId) && !isForceUpdate) {
                    return skuId;
                }
                //更新sku时抢锁
                cacheFlag = codisService.setnx("sku"+ entityId + menuId + specId, 2, "true");//更新加锁2秒
                if (cacheFlag != null && cacheFlag > 0) {
                    break;
                }
                //否则sleep()
            }else {
                //新增sku时，抢锁
                cacheFlag = codisService.setnx("sku"+ entityId + menuId + specId, 10, "true");
                if (cacheFlag != null && cacheFlag > 0) {
                    itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
                    itemMapping = CollectionUtils.isEmpty(itemMappings) ? null : itemMappings.get(0);
                    skuId = itemMapping!=null && StringUtils.isNotBlank(itemMapping.getTpId()) ? itemMapping.getTpId() : null;
                    break;
                }
            }
            try {
                Thread.sleep(CommonConstant.SLEEP_TIME);
            } catch (InterruptedException e) {
                throw new BizException("[kb_databack]线程中断异常");
            }
        }

        //捕获业务异常并记录
        try {
            //查询菜品
            Result<Menu> menuResult = getMenuService.findMenuAndProp(entityId, menuId);
            if (!menuResult.isSuccess() || menuResult.getModel() == null) {
                bizLog.error("[kb_databack]getMenuService.findMenuAndProp(entityId, menuId) is fail! entityId: " + JSON.toJSONString(entityId) + "menuId: " + JSON.toJSONString(menuId) + " menuResult: " + JSON.toJSONString(menuResult));
                throw new BizException("[kb_databack]套餐内子菜不能单独删除");
            }
            Menu menu = menuResult.getModel();

            //查询菜品映射
            this.checkDishId(merchantId, shopId, entityId, menuId,false,null, null);//确认菜品已同步
            ItemMappingQuery itemMappingQuery7 = new ItemMappingQuery(entityId, shopId, platCode, (int) CommonConstant.ITEM, menuId, null);
            List<ItemMapping> itemMappings7 = itemMappingService.getItemMappingListByQuery(itemMappingQuery7);
            ItemMapping itemMapping7 = CollectionUtils.isEmpty(itemMappings7) ? null : itemMappings7.get(0);
            if (itemMapping7==null) {
                throw new BizException("[kb_databack]查询菜品映射关系失败！");
            }else if(StringUtils.isBlank(itemMapping7.getTpId())){
                throw new BizException("[kb_databack]"+ itemMapping7.getSyncResult());
            }
            final String finalDishId = itemMapping7.getTpId();
            final String finalSkuId = skuId;
            String goodsId = itemMapping7.getItemMappingExt().getGoodsId();
            String imgUrl = StringUtils.isBlank(menu.getImagePath()) ? null : menu.getImagePath();//二维火图片url
            String imgUrlCode = StringUtils.isBlank(imgUrl) ? null : (StringUtils.isBlank(itemMapping7.getItemMappingExt().getImgUrlCode()) || !imgUrl.equals(itemMapping7.getItemMappingExt().getImgUrl())) ? this.checkImgUrlCode(entityId, imgUrl) : itemMapping7.getItemMappingExt().getImgUrlCode();//口碑图片码

            //新增/修改sku（口碑）
            KbDishRequest request = new KbDishRequest();
            kbDish = kbDish!=null && kbDish.getDishName()!=null ? kbDish : this.addKbDishParams(kbDish, menu, merchantId, shopId, entityId, menuId);//添加菜品/sku的共通参数
            if (kbDish == null) {
                return null;
            }
            kbDish.getDishPracticeList().forEach(kbDishPractice -> kbDishPractice.setDishId(finalDishId));//赋值dishId
            kbDish.setDishId(finalDishId);
            kbDish.setGoodsId(goodsId);
            kbDish.setDishImg(imgUrlCode);
            {//赋值sku相关信息
                KbDishSku kbDishSku = kbDish.getDishSkuList().get(0);
                kbDishSku.getDishPackagesDetailList().forEach(kbDishPackagesDetail -> kbDishPackagesDetail.setPackagesSkuId(finalSkuId));
                kbDishSku.setSkuId(finalSkuId);
                kbDishSku.setDishId(finalDishId);
                kbDishSku.setSpecCode01(this.checkSpecId(merchantId, shopId, specId, entityId, false, null));
                if (Objects.equals(specId, defaultSpecId)) {//默认sku
                    kbDishSku.setSellPrice(String.valueOf(menu.getPrice()));
                    kbDishSku.setMemberPrice(String.valueOf(menu.getMemberPrice()));//会员价
                    kbDishSku.setSkuSort(Integer.toString(menu.getSortCode()));
                } else {//有sku
                    //查询菜品_规格详情 MenuSpecDetail
                    Result<List<MenuSpecDetail>> menuSpecDetailsResult = getSpecDetailService.queryMenuSpecDetail(menuId, entityId);
                    if (!menuSpecDetailsResult.isSuccess()|| CollectionUtils.isEmpty(menuSpecDetailsResult.getModel())) {
                        bizLog.error("[kb_databack]getSpecDetailService.queryMenuSpecDetail(menuId, entityId) failed. menuId: " + JSON.toJSONString(menuId) + ", entityId: " + JSON.toJSONString(entityId) + "menuSpecDetailsResult :" + JSON.toJSONString(menuSpecDetailsResult));
                        throw new BizException("[kb_databack]菜品规格(" + specName + ")已经删除，请添加规格后重新关联商品");
                    }

                    MenuSpecDetail menuSpecDetail = null;
                    Set<String> specDetailNames = new HashSet<>();
                    for (MenuSpecDetail vo : menuSpecDetailsResult.getModel()){
                        if(Objects.equals(vo.getSpecDetailId(), specDetailId)){
                            menuSpecDetail = vo;
                        }else {
                            specDetailNames.add(vo.getSpecDetailName());
                        }
                    }

                    if (menuSpecDetail == null) {
                        bizLog.error("[kb_databack]getSpecDetailService.queryMenuSpecDetail(menuId, entityId) failed. menuId: " + JSON.toJSONString(menuId) + ", entityId: " + JSON.toJSONString(entityId) + "menuSpecDetailsResult :" + JSON.toJSONString(menuSpecDetailsResult));
                        throw new BizException("[kb_databack]菜品规格(" + specName + ")已经删除，请添加规格后重新关联商品");
                    }else if(specDetailNames.contains(menuSpecDetail.getSpecDetailName())){
                        throw new BizException("[kb_databack]菜品规格名称不能重复，请检查修改");
                    }
                    kbDishSku.setSellPrice(String.valueOf(menuSpecDetail.getMenuPrice()));
                    kbDishSku.setMemberPrice(String.valueOf(menuSpecDetail.getMenuPrice()));//会员价
                    kbDishSku.setSkuSort(Integer.toString(menuSpecDetail.getSortCode()));
                }
            }
            request.setKbDish(kbDish);
            Result<KbDishResponse> addOrUpdateResult = kouBeiDishCookService.dishSync(request, StringUtils.isNotEmpty(skuId) ? KouBeiSyncTypeEnum.update : KouBeiSyncTypeEnum.add, KouBeiDishBizTypeEnum.sku);
            if (!addOrUpdateResult.isSuccess()) {
                bizLog.warn("[kb_databack]kouBeiDishCookService.dishSync(request, syncType, bizType) failed. request: " + JSON.toJSONString(request) + ", syncType: " + JSON.toJSONString(StringUtils.isNotBlank(skuId) ? KouBeiSyncTypeEnum.update : KouBeiSyncTypeEnum.add) + ", bizType: " + JSON.toJSONString(KouBeiDishBizTypeEnum.sku) + ", addOrUpdateResult: " + JSON.toJSONString(addOrUpdateResult));
            }
            skuId = StringUtils.isNotEmpty(skuId) ? skuId : addOrUpdateResult.isSuccess() ? addOrUpdateResult.getModel().getKbDish().getDishSkuList().get(0).getSkuId() : null;

            //新增/修改sku映射关系
            itemMapping = itemMapping == null ? new ItemMapping(entityId, shopId, platCode, (int) idType, menuId, null) : itemMapping;
            itemMapping.setCommonId(specId);
            itemMapping.setTpId(skuId);
            itemMapping.setSyncStatus(addOrUpdateResult.isSuccess() ? 1 : 0);
            itemMapping.setSyncResult(addOrUpdateResult.getMessage());
            if (itemMapping.getId() == null) {
                itemMappingService.saveItemMapping(itemMapping);
            } else {
                itemMappingService.updateItemMapping(itemMapping);
            }

            //同步是否成功
            if (!addOrUpdateResult.isSuccess()) {
                throw new BizException("[kb_databack]同步sku失败:" + kbDish.getDishName() + "(" + specName + ")" + ", " + addOrUpdateResult.getMessage());
            }

            return skuId;
        }catch (BizException e){
            if(!e.getMessage().contains("同步sku失败")) {
                //新增/修改sku映射关系
                itemMapping = itemMapping == null ? new ItemMapping(entityId, shopId, platCode, (int) idType, menuId, null) : itemMapping;
                itemMapping.setCommonId(specId);
                itemMapping.setTpId(skuId);
                itemMapping.setSyncStatus(0);
                itemMapping.setSyncResult(e.getMessage().replaceAll("\\[kb_databack\\]", ""));
                if (itemMapping.getId() == null) {
                    itemMappingService.saveItemMapping(itemMapping);
                } else {
                    itemMappingService.updateItemMapping(itemMapping);
                }
            }
            throw e;
        }finally {
            codisService.del("sku" + entityId + menuId + specId);
        }
    }

	/**
	 * 获取加料Id(口碑)
	 *
	 * @param merchantId    merchantId
	 * @param affiliateId   加料Id
	 * @param entityId      entityId
	 * @param isForceUpdate 是否强制更新（若已有映射关系）
	 * @param lastVer       版本号（若有, 更新时需检查版本号。用于校验mq消息体的版本号）
	 * @return 口碑加料id
	 */
	public String checkAffiliateId(String merchantId, String shopId, String affiliateId, String entityId, boolean isForceUpdate, Integer lastVer) {
		String tpId = null;
		String platCode = String.valueOf(CommonConstant.KOUBEI_PLATFORM);
		byte idType = CommonConstant.ADDITION;

		//校验加料id
		if (StringUtils.isBlank(affiliateId)) {
			return null;
		}

		//新增防重-自旋锁
		ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopId, platCode, (int) idType, affiliateId, null);
		List<ItemMapping> itemMappings;
		ItemMapping itemMapping = null;
		Long cacheFlag;
		int count = 0;
		while (true) {
			count++;
			if (count > 100) {
				break;
			}
			//查询加料映射关系(localId->tpId)
			itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
			itemMapping = CollectionUtils.isEmpty(itemMappings) ? null : itemMappings.get(0);
			tpId = itemMapping != null && StringUtils.isNotBlank(itemMapping.getTpId()) ? itemMapping.getTpId() : null;
			if (StringUtils.isNotBlank(tpId)) {
				//是否强制更新
				if (StringUtils.isNotBlank(tpId) && !isForceUpdate) {
					return tpId;
				}
				break;
			} else {//需新增时，抢锁
				cacheFlag = codisService.setnx("additionId" + entityId + affiliateId, 10, "true");
				if (cacheFlag != null && cacheFlag > 0) {
					itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
					itemMapping = CollectionUtils.isEmpty(itemMappings) ? null : itemMappings.get(0);
					tpId = itemMapping != null && StringUtils.isNotBlank(itemMapping.getTpId()) ? itemMapping.getTpId() : null;
					break;
				}
			}
			try {
				Thread.sleep(CommonConstant.SLEEP_TIME);
			} catch (InterruptedException e) {
				throw new BizException("[kb_databack]线程中断异常");
			}
		}

		//查询加料
		Result<List<Menu>> menuResult = getAdditionService.listAdditionAll(entityId);
		if (!menuResult.isSuccess() || CollectionUtils.isEmpty(menuResult.getModel()) || menuResult.getModel().stream().noneMatch(vo -> Objects.equals(vo.getId(), affiliateId))) {
			bizLog.error("[kb_databack]getAdditionService.listAdditionAll(entityId) failed. entityId: " + JSON.toJSONString(entityId) + ", menuResult: " + JSON.toJSONString(menuResult));
			throw new BizException("[kb_databack]查询加料失败，或加料已删除");
		}
		Menu menu = menuResult.getModel().stream().filter(vo -> Objects.equals(vo.getId(), affiliateId)).findFirst().orElse(null);

		if(menu.getPrice()>1000){
			throw new BizException("[kb_databack]加料价格超过1000，请检查修改");
		}

		//校验版本号
		if (lastVer != null) {
			if (lastVer < menu.getLastVer()) {//消息版本落后，不处理
				return tpId;
			} else if (lastVer > menu.getLastVer()) {//消息版本超前，等待重试（抛异常）
				throw new BizException("[kb_databack]同步单位时mq消息版本超前");
			}
		}

		//新增/修改加料（口碑）
		KbDishAddition request = new KbDishAddition();
		request.setAddPrice(String.valueOf(menu.getPrice()));
		request.setMaterialName(menu.getName());
		request.setMaterialType("SHOP");
		request.setPublicId(shopId);
		request.setMerchantId(merchantId);
		if (StringUtils.isNotBlank(tpId)) {
			request.setMaterialId(tpId);
		}
		request.setCreateUser("ADMIN");
		request.setUpdateUser("ADMIN");
		request.setExtInfo(JSON.toJSONString(new HashMap<>()));
		Result<String> addOrUpdateResult = kouBeiDishCookService.dishAdditionSync(request, StringUtils.isNotBlank(tpId) ? KouBeiSyncTypeEnum.update : KouBeiSyncTypeEnum.add);
		if (!addOrUpdateResult.isSuccess()) {
			bizLog.warn("[kb_databack]kouBeiDishCookService.dishAdditionSync(request, syncType, bizType) failed. request: " + JSON.toJSONString(request) + ", syncType: " + JSON.toJSONString(StringUtils.isNotBlank(tpId) ? KouBeiSyncTypeEnum.update : KouBeiSyncTypeEnum.add) + ", bizType: " + "addition" + ", addOrUpdateResult: " + JSON.toJSONString(addOrUpdateResult));
		}
		tpId = StringUtils.isNotBlank(tpId) ? tpId : addOrUpdateResult.isSuccess() ? addOrUpdateResult.getModel() : null;//加料id(口碑)

		//新增/修改加料映射关系
		itemMapping = itemMapping == null ? new ItemMapping(entityId, shopId, platCode, (int) idType, affiliateId, null) : itemMapping;
		itemMapping.setTpId(tpId);
		itemMapping.setSyncStatus(addOrUpdateResult.isSuccess() ? 1 : 0);
		itemMapping.setSyncResult(addOrUpdateResult.getMessage());
		if (itemMapping.getId() == null) {
			itemMappingService.saveItemMapping(itemMapping);
		} else {
			itemMappingService.updateItemMapping(itemMapping);
		}


		if (itemMapping.getSyncStatus() == 0 && itemMapping.getSyncResult().equals(KoubeiErrorCodeEnum.INVALID_MATERIAL_NAME.getMessage())) {
			int num = 0;
			List<String> nameList = menuResult.getModel().stream().map(menu1 -> menu1.getName()).collect(Collectors.toList());
			for (String name : nameList) {
				if (name.equals(menu.getName())) {
					num++;
				}
			}
			if (num == 1) {
				bizLog.info("review addition is really repeat. entityId:{}, affiliateId:{}", entityId, affiliateId);
				KbDishAdditionResponse kbDishAdditionResponse = kouBeiDishCookService.queryAdditionListByMaterialId(merchantId, null, 200, 1).getModel();
				if (null != kbDishAdditionResponse) {
					List<KbDishAddition> kbDishAdditionList = kbDishAdditionResponse.getKbDishAddition();
					Map<String, String> map = new HashMap<>();
					if (CollectionUtils.isNotEmpty(kbDishAdditionList)) {
						for (KbDishAddition kbDishAddition : kbDishAdditionList) {
							if (kbDishAddition.getPublicId().equals(shopId)) {
								map.put(kbDishAddition.getMaterialName(), kbDishAddition.getMaterialId());
							}
						}
					}
					if (MapUtils.isNotEmpty(map)) {
						for (Map.Entry entry : map.entrySet()) {
							if (String.valueOf(entry.getKey()).equals(menu.getName())) {
								itemMapping.setTpId(String.valueOf(entry.getValue()));
								if (itemMapping.getTpId() != null) {
									itemMapping.setSyncStatus(1);
									itemMapping.setSyncResult("");
									int result = itemMappingService.updateItemMapping(itemMapping);
									if (result > 0) {
										bizLog.info("review addition success. entityId:{}, affiliateId:{}", entityId, affiliateId);
										tpId = itemMapping.getTpId();
										return tpId;
									}
								}
							}
						}
					}
				}
			}
		}

		//同步是否成功
		if (!addOrUpdateResult.isSuccess()) {
			if (StringUtils.isBlank(tpId)) {
				codisService.del("additionId" + entityId + affiliateId);//新增去鎖
			}
			throw new BizException("[kb_databack]同步加料失败:" + request.getMaterialName() + "， " + addOrUpdateResult.getMessage());

		}
		return tpId;
	}

    /**
     * 同步skuId(口碑，该菜品下所有sku)
     * @param merchantId merchantId
     * @param entityId entityId
     * @param menuId 菜品id
     * @param isForceUpdate 是否强制更新（若已有映射关系）
     * @param lastVer 版本号（若有, 更新时需检查版本号。用于校验mq消息体的版本号）
     */
    public void checkBatchSkuId(String merchantId, String shopId, String entityId, String menuId, boolean isForceUpdate, Integer lastVer, KbDish kbDish){
        String defaultSpecId = entityId + CommonConstant.KOUBEI_DEFAULT_SPEC_ID;
        List<String> specDetailIds = new ArrayList<>();
        List<String> specIds = new ArrayList<>();
        //查询所有规格
        Result<List<MenuSpecDetail>>  menuSpecDetailsResult = getSpecDetailService.queryMenuSpecDetail(menuId, entityId);
        if(!menuSpecDetailsResult.isSuccess()){
            bizLog.error("[kb_databack]getSpecDetailService.queryMenuSpecDetail(menuId, entityId) failed. menuId: "+ JSON.toJSONString(entityId)+", entityId: "+ JSON.toJSONString(menuId) + "menuSpecDetailsResult :"  + JSON.toJSONString(menuSpecDetailsResult));
            throw new BizException("[kb_databack]查询菜品规格失败！");
        } else if(CollectionUtils.isEmpty(menuSpecDetailsResult.getModel())){//同步默认sku
            this.checkSkuId(merchantId, shopId, entityId, menuId, null, true, null, kbDish);
        } else{//同步sku，并删除默认sku
            kouBeiDeleteUtil.deleteSkuId(merchantId, shopId, entityId, menuId, defaultSpecId);
            for(MenuSpecDetail menuSpecDetail : menuSpecDetailsResult.getModel()){
                this.checkSkuId(merchantId, shopId, entityId, menuId, menuSpecDetail.getSpecDetailId(), true, null, kbDish);
                specDetailIds.add(menuSpecDetail.getSpecDetailId());
            }
        }

        //获取规格的specIds
        if(CollectionUtils.isNotEmpty(specDetailIds)) {
            SpecDetailQuery specDetailQuery = new SpecDetailQuery(entityId);
            specDetailQuery.setSpecDetailIdList(specDetailIds);
            Result<List<SpecDetail>> specDetailsResult = getSpecDetailService.getSpecDetailsByQuery(specDetailQuery);
            if(!specDetailsResult.isSuccess()){
                return;
            }else if(CollectionUtils.isNotEmpty(specDetailsResult.getModel())){
                specDetailsResult.getModel().forEach(specDetail -> specIds.add(specDetail.getSpecId()));
            }
        }else {
            specIds.add(defaultSpecId);
        }

        //删除多余的sku记录
        ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopId, "107", 8, menuId, null);
        itemMappingQuery.setSyncStatus(0);
        List<ItemMapping> itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
        if(CollectionUtils.isNotEmpty(itemMappings)){
            itemMappings.forEach(itemMapping -> {
                if(!specIds.contains(itemMapping.getCommonId())){
                    itemMapping.setIsValid(0);
                    itemMappingService.updateItemMapping(itemMapping);
                }
            });
        }



    }
    /**
     * 校验菜谱-菜品绑定情况（未绑定的进行绑定，套餐时需要判断）
     * @param merchantId merchantId
     * @param entityId entityId
     * @param menuId 菜品id
     */
    public void checkDishCookMapping(String merchantId, String shopId, String entityId, String menuId){
        String platCode = String.valueOf(CommonConstant.KOUBEI_PLATFORM);
        try {
            //查询菜谱
            CookBO cookBO = cookInService.selectByType(entityId, EnumCookType.KOUBEI.getCode());
            if (cookBO==null){
                return;
            }

            //查询菜谱明细
            CookDetailQuery cookDetailQuery = new CookDetailQuery(entityId);
            cookDetailQuery.setCookId(cookBO.getId());
            cookDetailQuery.setMenuId(menuId);
            List<CookDetailBO> cookDetailBOs = cookDetailInService.selectByQuery(cookDetailQuery);
            if(CollectionUtils.isEmpty(cookDetailBOs)){
                return;
            }

            //查询菜谱明细映射
            ItemMenuMappingQuery itemMenuMappingQuery = new ItemMenuMappingQuery(entityId, shopId, platCode, menuId, null, String.valueOf(cookBO.getId()), null);
            List<ItemMenuMapping> itemMenuMappings = itemMenuMappingService.getItemMenuMappingListByQuery(itemMenuMappingQuery);
            itemMenuMappings.removeIf(itemMenuMapping -> itemMenuMapping.getSyncStatus()==0);
            if(CollectionUtils.isNotEmpty(itemMenuMappings)){
                return;
            }

            //同步菜谱明细
            for(CookDetailBO cookDetailBO : cookDetailBOs){
                koubeiCookDishHandler.addCookDetailBODetail(cookDetailBO, entityId, merchantId, cookDetailBO.getCookId(), shopId);
            }

        }catch (Exception e){
            bizLog.error("[kb_databack]kouBeiCheckUtil.checkDishCookMapping(merchantId, menuId); failed. merchantId: " + JSON.toJSONString(merchantId) + ", entityId: " + JSON.toJSONString(entityId) + ", menuId: " + JSON.toJSONString(menuId), e);
        }
    }

    /**
     * 获取菜谱id(口碑)
     * @param merchantId merchantId
     * @param entityId entityId
     * @param cookId 菜谱Id
     * @param isForceUpdate 是否强制更新（若已有映射关系）
     * @param lastVer 版本号（若有, 更新时需检查版本号。用于校验mq消息体的版本号）
     * @return 口碑菜品组id
     */
    public String checkCookId(String merchantId, String shopId, String entityId, String cookId, boolean isForceUpdate, Integer lastVer){
        String tpId = null;
        String platCode = String.valueOf(CommonConstant.KOUBEI_PLATFORM);
        byte idType = CommonConstant.ITEM_COOK;

        //新增防重-自旋锁
        ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopId, platCode, (int)idType, cookId, null);
        List<ItemMapping> itemMappings;
        ItemMapping itemMapping = null;
        Long cacheFlag;
        int count = 0;
        while(true) {
            count ++;
            if(count > 100) {
                break;
            }
            //查询菜谱映射关系(localId->tpId)
            itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
            itemMapping = CollectionUtils.isEmpty(itemMappings) ? null : itemMappings.get(0);
            tpId = itemMapping!=null && StringUtils.isNotBlank(itemMapping.getTpId()) ? itemMapping.getTpId() : null;
            if (StringUtils.isNotBlank(tpId)) {
                //是否强制更新
                if (StringUtils.isNotBlank(tpId) && !isForceUpdate) {
                    return tpId;
                }
                break;
            }else {//需新增时，抢锁
                cacheFlag = codisService.setnx("cookId"+ entityId + cookId, 10, "true");
                if (cacheFlag != null && cacheFlag > 0) {
                    itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
                    itemMapping = CollectionUtils.isEmpty(itemMappings) ? null : itemMappings.get(0);
                    tpId = itemMapping!=null && StringUtils.isNotBlank(itemMapping.getTpId()) ? itemMapping.getTpId() : null;
                    break;
                }
            }
            try {
                Thread.sleep(CommonConstant.SLEEP_TIME);
            } catch (InterruptedException e) {
                throw new BizException("[kb_databack]线程中断异常");
            }
        }

        //查询菜谱
        CookBO cookBO = cookInService.selectByType(entityId,EnumCookType.KOUBEI.getCode());
        if(cookBO==null){
            bizLog.error("[kb_databack]cookInService.selectByEntityId(entityId) failed. entityId: "+ JSON.toJSONString(entityId) +", cookBO: "+ JSON.toJSONString(cookBO));
            throw new BizException("[kb_databack]查询菜谱失败，或菜谱已删除");
        }

        //校验版本号
        if(lastVer!=null){
            if (lastVer < cookBO.getLastVer()){//消息版本落后，不处理
                return tpId;
            }else if (lastVer > cookBO.getLastVer()){//消息版本超前，等待重试（抛异常）
                throw new BizException("[kb_databack]同步单位时mq消息版本超前");
            }
        }

        //新增/修改菜谱（口碑）
        KbDishCookRequest request = new KbDishCookRequest();
        KbDishCook kbDishCook = new KbDishCook();
        kbDishCook.setCookId(tpId);
        kbDishCook.setCookName(cookBO.getName());
        kbDishCook.setStatus(cookBO.getStatus()==0 ? KouBeiDishStatusEnum.stop : KouBeiDishStatusEnum.open);
        kbDishCook.setPeriodType(KouBeiPeriodTypeEnum.forever);
        kbDishCook.setCookChannel(KouBeiCookChannelEnum.kbb2c);  //  默认都是扫码
        kbDishCook.setCookVersion(cookBO.getLastVer() == null ? "1" : String.valueOf(cookBO.getLastVer()));
        kbDishCook.setMerchantId(merchantId);
        kbDishCook.setCreateUser("1");
        kbDishCook.setUpdateUser("1");
        kbDishCook.setSourceFrom(CommonConstant.SOURCE_FROM);
        List<String> shopIdList = new ArrayList<>();
        shopIdList.add(shopId);
        kbDishCook.setShopList(shopIdList);
        List<KbDishCookDetail> kbCookDetailList = new ArrayList<>();
        kbDishCook.setKbCookDetailList(kbCookDetailList);
        kbDishCook.setStartTime("00:00");
        kbDishCook.setEndTime("23:59");
        kbDishCook.setArea("无;");
        request.setKbDishCook(kbDishCook);
        Result<KbDishCookResponse> addOrUpdateResult = kouBeiDishCookService.dishCookSync(request, StringUtils.isNoneBlank(tpId) ? KouBeiSyncTypeEnum.update : KouBeiSyncTypeEnum.add, KouBeiCookBizTypeEnum.cook);
        if (!addOrUpdateResult.isSuccess()) {
            bizLog.warn("[kb_databack]kouBeiDishCookService.dishDictionarySync(request, syncType, bizType) failed. request: " + JSON.toJSONString(request) + ", syncType: " + JSON.toJSONString(StringUtils.isNotBlank(tpId) ? KouBeiSyncTypeEnum.update : KouBeiSyncTypeEnum.add) + ", bizType: " + JSON.toJSONString(KouBeiDictionaryBizTypeEnum.unit) + ", addOrUpdateResult: " + JSON.toJSONString(addOrUpdateResult));
        }
        tpId = StringUtils.isNotBlank(tpId) ? tpId : addOrUpdateResult.getModel()!=null && addOrUpdateResult.getModel().getKbDishCook()!=null ? addOrUpdateResult.getModel().getKbDishCook().getCookId() : null;//单位id(口碑)

        //新增/修改菜谱映射关系
        itemMapping = itemMapping==null ? new ItemMapping(entityId, shopId, platCode, (int)idType, cookId, null) : itemMapping;
        itemMapping.setTpId(tpId);
        itemMapping.setSyncStatus(addOrUpdateResult.isSuccess() ? 1 : 0);
        itemMapping.setSyncResult(addOrUpdateResult.getMessage());
        if (itemMapping.getId() == null) {
            itemMappingService.saveItemMapping(itemMapping);
        } else {
            itemMappingService.updateItemMapping(itemMapping);
        }

        //同步是否成功
        if(!addOrUpdateResult.isSuccess()){
            if(StringUtils.isBlank(tpId)){
                codisService.del("cookId"+ entityId + cookId);//新增去鎖
            }
            throw new BizException("[kb_databack]同步菜谱失败:\""+ request.getKbDishCook().getCookName() +"\"， "+ addOrUpdateResult.getMessage());
        }
        return tpId;
    }

    /**
     * 获取图片码(口碑)
     * @param entityId entityId
     * @param imgUrl 二维火图片url
     * @return 口碑图片码
     */
    private String checkImgUrlCode(String entityId, String imgUrl) {
        try {
            //获取token
            Result<String> koubeiAuthTokenDTOResult = koubeiCommonService.getKoubeiTokenByEntityId(entityId);
            if (!koubeiAuthTokenDTOResult.isSuccess() || StringUtils.isEmpty(koubeiAuthTokenDTOResult.getModel())) {
                bizLog.error("[kb_databack]koubeiBaseService.getKdeoubeiTokenByEntityId(entityId) failed. entityId: " + JSON.toJSONString(entityId));
                throw new BizException("[kb_databack]根据entityId获取Token失败！");
            }

            //上传图片（重试3次）
            int count = 0;
            while (count++ < 3) {
                Result<String> imgResult = koubeiCommonService.uploadImg(imgUrl, koubeiAuthTokenDTOResult.getModel());
                if (!imgResult.isSuccess()) {
                    bizLog.error("[kb_databack]koubeiBaseService.uploadImg(imgUrl, token) failed. imgUrl: " + JSON.toJSONString(imgUrl) + ", token: " + JSON.toJSONString(koubeiAuthTokenDTOResult.getModel()) + ",imgResult:" + JSON.toJSONString(imgResult));
                } else {
                    return imgResult.getModel();
                }
            }
        }catch (Exception e){
            bizLog.error("[kb_databack] checkImgUrlCode(entityId, imgUrl) failed. entityId: " + JSON.toJSONString(entityId) + ", imgUrl: " + JSON.toJSONString(imgUrl), e);
        }

        return null;
    }






    /**
     * 添加菜品/sku的共通参数
     * @param menu 菜品
     * @param merchantId merchantId
     * @param entityId entityId
     * @param menuId 菜品id
     */
    private KbDish addKbDishParams(KbDish kbDish, Menu menu, String merchantId, String shopId, String entityId, String menuId){
        kbDish = kbDish==null ? new KbDish() : kbDish;
        List<KbDishPackagesDetail> kbDishPackagesDetailList = new ArrayList<>();
        List<KbDishSku> kbDishSkuList = new ArrayList<>();
        KbDishSku kbDishSku = new KbDishSku();//赋值sku相关信息
        boolean isChoose = false;

        //查询菜品
        if(menu==null){
            Result<Menu> menuResult = getMenuService.findMenuAndProp(entityId, menuId);
            if(!menuResult.isSuccess() || menuResult.getModel()==null){
                bizLog.error("[kb_databack]getMenuService.findMenuAndProp(entityId, menuId) is fail! entityId: " + JSON.toJSONString(entityId) + "menuId: " + JSON.toJSONString(menuId) + " menuResult: " + JSON.toJSONString(menuResult));
                throw new BizException("[kb_databack]套餐内子菜不能单独删除");
            }
            menu = menuResult.getModel();
        }

        //菜品是否为套餐
        if(menu.getIsInclude()==1){
            //查询套餐组
            Result<List<SuitMenuDetail>> suitMenuDetailsResult = getSuitMenuService.queryDetailsBySuitMenuId(menuId, entityId);
            if(!suitMenuDetailsResult.isSuccess()){
                bizLog.error("[kb_databack]getSuitMenuService.queryDetailsBySuitMenuId(menuId, entityId) failed. menuId: "+ JSON.toJSONString(menuId)+", entityId: "+ JSON.toJSONString(entityId) + "suitMenuDetailsResult :"  + JSON.toJSONString(suitMenuDetailsResult));
                throw new BizException("[kb_databack]查询套餐分组失败，或套餐分组已删除");
            }else if(CollectionUtils.isNotEmpty(suitMenuDetailsResult.getModel())){
                for(SuitMenuDetail suitMenuDetail : suitMenuDetailsResult.getModel()){
                    //按类型处理
                    if(suitMenuDetail.getIsRequired() == 1){//套餐-必选菜品组-子菜
                        //查询菜品组-子菜
                        List<String> suitMenuDetailIds = new ArrayList<>();
                        suitMenuDetailIds.add(suitMenuDetail.getId());
                        Result<List<SuitMenuChange>> suitMenuChangesResult = getSuitMenuService.queryChangeListBySuitMenuDetailIdList(entityId, suitMenuDetailIds);
                        if(!suitMenuChangesResult.isSuccess()){
                            bizLog.error("[kb_databack]getSuitMenuService.queryChangeListBySuitMenuId(suitMenuId, entityId) failed. suitMenuId: "+ JSON.toJSONString(suitMenuDetail.getId())+", entityId: "+ JSON.toJSONString(entityId) + "suitmenuChanggLists :"  + JSON.toJSONString(suitMenuChangesResult));
                            throw new BizException("[kb_databack]查询（必选）套餐分组子菜失败，或（必选）套餐分组子菜已删除");
                        }else if(CollectionUtils.isNotEmpty(suitMenuChangesResult.getModel())){
                            Set<String> dishIds = new HashSet<>();
                            for(SuitMenuChange suitMenuChange : suitMenuChangesResult.getModel()) {
                                KbDishPackagesDetail kbDishPackagesDetail = new KbDishPackagesDetail();
                                kbDishPackagesDetail.setDetailSkuId(this.checkSkuId(merchantId, shopId, entityId, suitMenuChange.getMenuId(),suitMenuChange.getSpecDetailId(),false,null, null));
                                kbDishPackagesDetail.setDetailSellPrice(String.valueOf(suitMenuChange.getPrice()));
                                kbDishPackagesDetail.setDetailMemberPrice(Double.toString(suitMenuChange.getPrice()));
                                kbDishPackagesDetail.setDetailCount(suitMenuChange.getNum()<=0 ? "99": String.valueOf((int)suitMenuChange.getNum()));
                                kbDishPackagesDetail.setDetailSort(String.valueOf(suitMenuChange.getSortCode()));
                                kbDishPackagesDetail.setDetailType(CommonConstant.DISH);
                                kbDishPackagesDetailList.add(kbDishPackagesDetail);

                                //套餐分组下应不含相同子菜
                                if(dishIds.contains(kbDishPackagesDetail.getDetailSkuId())){
                                    throw new BizException("[kb_databack]套餐分组含相同子菜，请检查修改");
                                }
                                dishIds.add(kbDishPackagesDetail.getDetailSkuId());
                            }
                        }
                    }else {//套餐-可选菜品组
                        KbDishPackagesDetail kbDishPackagesDetail_ = new KbDishPackagesDetail();
                        kbDishPackagesDetail_.setDetailSellPrice(String.valueOf(suitMenuDetail.getMenuPrice()));
                        kbDishPackagesDetail_.setDetailMemberPrice(Double.toString(suitMenuDetail.getMenuPrice()));
                        kbDishPackagesDetail_.setDetailCount(suitMenuDetail.getNum()<=0 ? "99" : String.valueOf((int)suitMenuDetail.getNum()));
                        kbDishPackagesDetail_.setDetailSort(String.valueOf(suitMenuDetail.getSortCode()));
                        kbDishPackagesDetail_.setDetailType(CommonConstant.GROUP);
                        kbDishPackagesDetail_.setGroupId(this.checkGroupId(merchantId, shopId, entityId, suitMenuDetail.getId(),false,null));
                        if(StringUtils.isEmpty(kbDishPackagesDetail_.getGroupId())) {
                            continue;
                        }
                        isChoose = true;
                        kbDishPackagesDetailList.add(kbDishPackagesDetail_);
                    }
                }
            }

            //空套餐不同步
            if(CollectionUtils.isEmpty(kbDishPackagesDetailList)){
                bizLog.info("[kb_databack][ignored_suitMenuDetail] getSuitMenuService.queryDetailsBySuitMenuId(menuId, entityId). entityId: "+ JSON.toJSONString(menuId)+ ", entityId: "+ JSON.toJSONString(entityId) +", suitMenuDetailsResult: "+ JSON.toJSONString(suitMenuDetailsResult));
                //先刪除菜譜-菜品
                kouBeiDeleteUtil.deleteCookDish(shopId, merchantId, menu.getEntityId(), menu.getId());
                //删除菜品
                kouBeiDeleteUtil.deleteDishId(merchantId, shopId, menu);
                //删除映射关系
                this.deleteItemMappingAndItemMenuMapping(entityId, shopId, menuId);

                throw new BizException("[kb_databack]套餐子菜不能为空");
            }
        }

        //查询菜品菜类
        Result<KindMenu> kindMenuResult = getMenuService.findKindMenu(entityId, menu.getKindMenuId());
        if(!kindMenuResult.isSuccess() || kindMenuResult.getModel()==null){
            bizLog.error("[kb_databack]getMenuService.findKindMenu(entityId, kindMenuId) failed. entityId: "+ JSON.toJSONString(entityId)+", kindMenuId: "+ JSON.toJSONString(menu.getKindMenuId()) + ", kindMenuResult: "+ JSON.toJSONString(kindMenuResult));
            throw new BizException("[kb_databack]菜品分类已经删除，请添加分类后重新关联商品");
        }

        //查询菜品做法加价
        List<KbDishPractice> kbDishPracticeList = new ArrayList<>();
        Result<List<MenuMake>> menuMakeListResult = getMakeService.queryMenuMakeList(entityId,menuId);
        if(!menuMakeListResult.isSuccess()){
            bizLog.error("[kb_databack]getMakeService.queryMenuMakeList(entityId, menuId) failed. entityId: "+ JSON.toJSONString(entityId)+", menuId: "+ JSON.toJSONString(menuId) + ", menuMakeListResult: "+ JSON.toJSONString(menuMakeListResult));
            throw new BizException("[kb_databack]查询菜品做法失败，或做法已删除");
        }else if (CollectionUtils.isNotEmpty(menuMakeListResult.getModel())){
            Set<String> menuMakeNames = new HashSet<>();
            for(MenuMake menuMake : menuMakeListResult.getModel()){
                if(StringUtils.isBlank(menuMake.getName())){
                    throw new BizException("[kb_databack]菜品做法名称为空，请检查修改");
                }else if (menuMake.getName().length()>32){
                    throw new BizException("[kb_databack]菜品做法名称超过32位字数限制，请检查修改");
                }else if(menuMakeNames.contains(menuMake.getName())){
                    throw new BizException("[kb_databack]菜品做法名称不能重复，请检查修改");
                }
                menuMakeNames.add(menuMake.getName());

                KbDishPractice kbDishPractice = new KbDishPractice();
                if(menuMake.getMakePriceMode() == 3){
                    kbDishPractice.setIncreaseMode(CommonConstant.MULTIPLY);
                }else {
                    kbDishPractice.setIncreaseMode(CommonConstant.ADD);
                }
                kbDishPractice.setPracticeName(menuMake.getName());
                kbDishPractice.setIncreasePrice(String.valueOf(menuMake.getMakePrice()));
                kbDishPracticeList.add(kbDishPractice);
            }
        }

		//查询加料映射
		Map<String, String> map = new HashMap<>();
		Map<String,String> map2 = new HashMap<>();

		Result<List<AdditionKindMenuVo>> listResult = getAdditionService.getBindKindDefaultAdditionList(entityId, menuId);
		List<String> existAdditionIdList = new ArrayList<>();
		for (AdditionKindMenuVo additionKindMenuVo : listResult.getModel()) {
			List<AdditionMenuVo> list = additionKindMenuVo.getAdditionMenuList();
			if (CollectionUtils.isNotEmpty(list)) {
				for (AdditionMenuVo additionMenuVo : list) {
					map2.put(additionMenuVo.getMenuId(),additionMenuVo.getMenuName());
					existAdditionIdList.add(additionMenuVo.getMenuId());
				}
			}
		}
		List<String> additionIdList = new ArrayList<>();
		for (String additionId : existAdditionIdList) {
			ItemMappingQuery itemMappingQuery9 = new ItemMappingQuery(entityId, shopId, String.valueOf(CommonConstant.KOUBEI_PLATFORM), (int) CommonConstant.ADDITION, additionId, null);
			List<ItemMapping> itemMappings9 = itemMappingService.getItemMappingListByQuery(itemMappingQuery9);
			if (CollectionUtils.isNotEmpty(itemMappings9)) {
				for (ItemMapping itemMapping : itemMappings9) {
					map.put(itemMapping.getLocalId(), itemMapping.getTpId());//存放的是二维火的加料id和口碑的加料id
				}
			} else {
				additionIdList.add(additionId);
			}
		}
		if (CollectionUtils.isNotEmpty(additionIdList)) {
			for (String additionId : additionIdList) {
				String tpId = checkAffiliateId(merchantId, shopId, additionId, entityId, true, null);
				if (null == tpId) {
					throw new BizException("[kb_databack]查询加料映射关系失败！");
				} else {
					map.put(additionId, tpId);
				}
			}
		}

		//查询菜品加料
		List<KbdishMaterialBinding> kbdishMaterialBindingList = new ArrayList<>();
		MenuAdditionQuery menuAdditionQuery = new MenuAdditionQuery(entityId);
		menuAdditionQuery.setMenuId(menuId);
		menuAdditionQuery.setIsValid(1);
		Result<List<MenuAddition>> menuAdditionListResult = getAdditionService.queryMenuAddition(menuAdditionQuery);

		if (!menuAdditionListResult.isSuccess()) {
			bizLog.error("[kb_databack]getAdditionService.queryMenuAddition(menuAdditionQuery) failed. entityId: " + JSON.toJSONString(entityId) + ", menuId: " + JSON.toJSONString(menuId) + ", menuAdditionListResult: " + JSON.toJSONString(menuAdditionListResult));
			throw new BizException("[kb_databack]查询菜品加料失败，或加料已删除");
		} else if (CollectionUtils.isNotEmpty(menuAdditionListResult.getModel())) {
			if (menuAdditionListResult.getModel().size() > 50) {
				throw new BizException("[kb_databack]菜品加料不能超过50个，请修改");
			}
			for (MenuAddition menuAddition : menuAdditionListResult.getModel()) {
				KbdishMaterialBinding kbdishMaterialBinding = new KbdishMaterialBinding();
				if(StringUtils.isNoneEmpty(map.get(menuAddition.getAdditionId()))){
					kbdishMaterialBinding.setMaterialId(map.get(menuAddition.getAdditionId()));
				}else{
					ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopId, String.valueOf(CommonConstant.KOUBEI_PLATFORM), (int) CommonConstant.ADDITION, menuAddition.getAdditionId(), null);
					List<ItemMapping> itemMappingList = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
					if (itemMappingList.get(0).getSyncResult().equals(KoubeiErrorCodeEnum.INVALID_MATERIAL_NAME.getMessage())) {
						throw new BizException("[kb_databack]菜品加料名称不能重复，请检查修改");
					} else if(itemMappingList.get(0).getSyncResult().equals(KoubeiErrorCodeEnum.DISH_NAME_EXIST_FORBIDDEN_WORD.getMessage())){
                        throw new BizException("[kb_databack]同步加料失败:" + map2.get(menuAddition.getAdditionId()) + "， " + KoubeiErrorCodeEnum.DISH_NAME_EXIST_FORBIDDEN_WORD.getMessage());
					}
				}
				kbdishMaterialBindingList.add(kbdishMaterialBinding);
			}
		}

        //查询单位Id
        String unitName = menu.getAccount();
        String unitId = null;
        if(StringUtils.isNotEmpty(unitName)){
            Result<String> unitResult  = getMenuService.getUnitIdByUnitDesc(entityId,unitName);
            if(!unitResult.isSuccess() || unitResult.getModel()==null){
                bizLog.error("[kb_databack]getMenuService.getUnitIdByUnitDesc(entityId, unitName) is fail! entityId: " + JSON.toJSONString(entityId) + "unitName: " + JSON.toJSONString(unitName) + " unitResult: " + JSON.toJSONString(unitResult));
                throw new BizException("[kb_databack]菜品单位已经删除，请添加新单位后重新关联商品");
            }
            unitId = unitResult.getModel();
        }
        //查询单位映射关系
        String tpUnitId = this.checkUnitId(merchantId, shopId, entityId, unitId,false,null);
        //查询菜类映射关系
        String categoryId = this.checkKindMenuId(CommonConstant.ITEM_CATEGORY, merchantId, shopId, entityId, menu.getKindMenuId(),false, null);

        //赋值菜品/sku共通参数
        kbDish.setMiniAddNum(String.valueOf(menu.getStepLength()));
        kbDish.setNotCountThreshold(menu.getIsRatio()==1 ? "N" : "Y");
        kbDish.setCreateUser("ADMIN");
        kbDish.setUpdateUser("ADMIN");
        kbDish.setNbRememberCode(menu.getCode());
        kbDish.setEnRememberCode(menu.getSpell());
        kbDish.setDishName(menu.getName());
        kbDish.setDishCuisine(menu.getKindMenuName());//菜系传菜类名称
        kbDish.setCatetoryBigId(categoryId);
        kbDish.setCatetorySmallId(null);
        kbDish.setTypeBig(menu.getIsInclude()==1 ? "packages" : "single");//套餐|单品
        kbDish.setTypeSmall("packages".equals(kbDish.getTypeBig()) ? (isChoose ? CommonConstant.CHOOSE : CommonConstant.FIXED) : null);//套餐才有typeSmall
        kbDish.setUnitId(tpUnitId);
        kbDish.setCurPriceFlag(CommonConstant.NOT_CUR_PRICE_FLAG);
        kbDish.setMinServing(menu.getStartNum() == 0 ? "1" : String.valueOf(menu.getStartNum()));
        if("packages".equals(kbDish.getTypeBig()) && Integer.valueOf(kbDish.getMinServing())>1){
            throw new BizException("[kb_databack]口碑套餐起点份数最多为1份，请修改起点份数");
        }
        kbDish.setStatus(menu.getIsSelf()==1 ? KouBeiDishStatusEnum.open : KouBeiDishStatusEnum.stop);
        kbDish.setRemarks(StringUtils.isBlank(menu.getMemo()) ? null : menu.getMemo().length()<=1024 ? menu.getMemo() : menu.getMemo().substring(0, 1024));
        kbDish.setDishVersion(Long.toString(System.currentTimeMillis()));
        kbDish.setMerchantId(merchantId);
        kbDish.setDishPracticeList(kbDishPracticeList);
        kbDish.setMaterialBindingList(kbdishMaterialBindingList);
        kbDish.setShopId(shopId);
        kbDishSku.setStatus(KouBeiDishStatusEnum.open.getCode());
        kbDishSku.setBoxPrice(String.valueOf(menu.getPackingBox() != null ? menu.getPackingBox().getPrice() * menu.getPackingBoxNum() : 0));
        kbDishSku.setDishPackagesDetailList(kbDishPackagesDetailList);
        kbDishSkuList.add(kbDishSku);
        kbDish.setDishSkuList(kbDishSkuList);
        return kbDish;
    }

    /**
     * 删除菜品、sku、菜谱明细映射关系
     * （用于处理 口碑菜品被删除|空套餐）
     * @param entityId entityId
     * @param shopId shopId
     * @param menuId 菜品id
     */
    public void deleteItemMappingAndItemMenuMapping(String entityId, String shopId, String menuId){
        try {
            String platCode = String.valueOf(CommonConstant.KOUBEI_PLATFORM);
            //删除菜品、sku 映射
            List<Integer> idTypes = new ArrayList<>();
            idTypes.add((int)CommonConstant.ITEM);
            idTypes.add((int)CommonConstant.ITEM_SKU);
            ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopId, platCode, null, menuId, null);
            itemMappingQuery.setIdTypeList(idTypes);
            List<ItemMapping> itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
            if(CollectionUtils.isNotEmpty(itemMappings)){
                for(ItemMapping itemMapping : itemMappings){
                    itemMapping.setIsValid(0);
                    itemMappingService.updateItemMapping(itemMapping);
                }
            }

            //删除菜谱明细 映射
            List<ItemMenuMapping> itemMenuMappings = new ArrayList<>();
            List<CookBO> cookBOs = cookInService.selectByEntityId(entityId);
            if(CollectionUtils.isNotEmpty(cookBOs)) {
                for(CookBO cookBO : cookBOs) {
                    ItemMenuMappingQuery itemMenuMappingQuery = new ItemMenuMappingQuery(entityId, shopId, platCode, menuId, null, String.valueOf(cookBO.getId()), null);
                    List<ItemMenuMapping> itemMenuMappingList = itemMenuMappingService.getItemMenuMappingListByQuery(itemMenuMappingQuery);
                    itemMenuMappings.addAll(itemMenuMappingList);
                }
            }
            if(CollectionUtils.isNotEmpty(itemMenuMappings)){
                for (ItemMenuMapping itemMenuMapping : itemMenuMappings){
                    itemMenuMapping.setIsValid(0);
                    itemMenuMappingService.updateItemMenuMapping(itemMenuMapping);
                }
            }
        }catch (Exception e){
            bizLog.error("[kb_databack]deleteItemMappingAndItemMenuMapping(entityId, shopId, menuId, isEmptyDish) failed. entityId:{}, shopId:{}, menuId:{}", entityId, shopId, menuId, e);
        }
    }

    /**
     * 检查并删除同名菜品
     * （用于口碑同名菜存在，但没有映射关系的情况）
     * @param entityId entityId
     * @param shopId shopId
     * @param menuId menuId
     * @param kbDish 口碑菜品参数
     * @return 是否确实有同名菜
     */
    private boolean checkSameNameDish(String entityId, String shopId, String menuId, KbDish kbDish, String kbDishId){
        try {
            //查询同步菜品是否有映射关系
            String platCode = String.valueOf(CommonConstant.KOUBEI_PLATFORM);
            ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopId, platCode, (int)CommonConstant.ITEM, null, kbDishId);
            List<ItemMapping> itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
            bizLog.warn("[test] itemMappingService.getItemMappingListByQuery(itemMappingQuery) failed. itemMappingQuery:{}, itemMappings:{}", JSON.toJSONString(itemMappingQuery), JSON.toJSON(itemMappings));
            if(CollectionUtils.isNotEmpty(itemMappings)){
                return true;
            }

            //删除口碑的同名菜品
            KbDishRequest request = new KbDishRequest();
            request.setKbDish(kbDish);
            Result<KbDishResponse> deleteResult = kouBeiDishCookService.dishSync(request, KouBeiSyncTypeEnum.del, KouBeiDishBizTypeEnum.dish);
            if(!deleteResult.isSuccess()){
                bizLog.warn("[kb_databack] kouBeiDishCookService.dishSync(request, syncType, bizType) failed. request:{}, syncType:{}, bizType{}, deleteResult:{}", JSON.toJSONString(request), KouBeiSyncTypeEnum.del, KouBeiDishBizTypeEnum.dish, JSON.toJSON(deleteResult));
                throw new BizException("[kb_databack]删除菜品（口碑）失败:"+ deleteResult.getMessage());
            }
            return false;
        }catch (Exception e){
            bizLog.error("[kb_databack]checkSameNameDish(entityId, shopId, kbDish) failed. entityId:{}, shopId:{}, kbDish:{}}", entityId, shopId, JSON.toJSONString(kbDish), e);
        }
        return true;
    }
    /**
     * 获取菜类level
     * @param entityId entityId
     * @param kindMenuId 菜类id
     * @return level
     */
    private int getKindMenuLevel(byte idType, String shopId, String entityId, String kindMenuId){
        ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopId, String.valueOf(CommonConstant.KOUBEI_PLATFORM), (int)idType, kindMenuId, null);
        List<ItemMapping> itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
        if(CollectionUtils.isEmpty(itemMappings) || itemMappings.get(0).getItemMappingExt()==null){
            throw new BizException("[kb_databack]查询菜类映射关系失败");
        }else if(itemMappings.get(0).getItemMappingExt().getLevel()==null){
            return 1;
        }
        return itemMappings.get(0).getItemMappingExt().getLevel();
    }

    /**
     * 获取店铺映射关系
     * @param entityId entityId
     * @return merchantId
     */
    public ShopBindVo getShopBindVo(String entityId){
        return shopBindCacheComponent.loadData(entityId);
    }

    /**
     * 更新店铺绑定关系
     * @param shopBindVo shopBindVo
     */
    public void updateShopBindVo(ShopBindVo shopBindVo){
        try {
            Result updateResult = shopBindService.update(shopBindVo);
            if(!updateResult.isSuccess()){
                bizLog.error("[kb_databack]shopBindService.update(shopBindVo) failed. shopBindVo: "+ JSON.toJSONString(shopBindVo)+ ", updateResult: "+ JSON.toJSONString(updateResult));
            }
        }catch (Exception e){
            bizLog.error("[kb_databack]updateShopBindVo(shopBindVo) failed. shopBindVo: "+ JSON.toJSONString(shopBindVo), e);
        }
    }

    /**
     * 请求参数Md5校验
     * @param prefix 缓存前缀
     * @param objs 待加密参数s
     */
    private boolean checkMd5(String prefix, Object... objs){
        StringBuilder stringBuilder = new StringBuilder();
        for (Object obj : objs){
            stringBuilder.append(JSON.toJSON(obj));
        }
        String sign = MD5Util.MD5Encode(stringBuilder.toString());
        Long cacheFlag = codisService.setnx(prefix + sign, 5, "true");
        return cacheFlag != null && cacheFlag > 0;
    }
}
