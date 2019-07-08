package com.dfire.soa.item.partner.koubei.service;

import com.dfire.soa.item.partner.bo.BrandSyncResultBo;
import com.dfire.soa.item.partner.bo.CookBO;
import com.dfire.soa.item.partner.bo.SimpleSyncResultBo;
import com.dfire.soa.item.partner.bo.SyncResultBo;
import com.twodfire.share.result.Result;

import java.util.List;
import java.util.Map;

/**
 * 口碑数据同步接口
 * Created by GanShu on 2018/8/29 0029.
 */
public interface IKoubeiSyncService {

    /**
     * 全量同步
     * @param entityId entityId
     * @param platCode [107-口碑;]
     * @return result
     */
    Result<SyncResultBo> itemSync(String entityId, String platCode);

    /**
     *
     * @param brandEntityId 连锁的entityId
     * @param entityIdList  连锁下门店的id列表
     * @param platCode
     * @return
     */
    void brandItemSync(String brandEntityId, List<String> entityIdList, String platCode);

    /**
     * 获得连锁门店商品同步结果
     * @param brandEntityId
     * @param platCode
     * @return
     */
    Result<BrandSyncResultBo> getBrandItemSyncResult(String brandEntityId, String platCode);

    /**
     * 全量同步结果
     * @param entityId entityId
     * @param platCode [107-口碑;]
     * @return result
     */
    Result<SyncResultBo> getItemSyncResult(String entityId, String platCode);

    /**
     * 批量同步
     * (按idType、id同步)
     * @param entityId entityId
     * @param platCode [107-口碑;]
     * @param map  key: idType [1-规格; 2-单位; 3-菜类; 4-套餐组; 7-菜品; ];
     *              value: list<String>
     * @return result
     */
    Result<SyncResultBo> batchItemSync(String entityId, String platCode, Map<Integer, List<String>> map);

    /**
     * 批量同步结果
     * @param entityId entityId
     * @param platCode [107-口碑;]
     * @return result
     */
    Result<SyncResultBo> getBatchItemSyncResult(String entityId, String platCode);

    /**
     * 获取店铺各项同步结果
     * @param entityId entityId
     * @param platCode [107-口碑;]
     * @param map (map为null,取entityId下所有结果)
     *              key: idType [1-规格; 2-单位; 3-菜类; 4-套餐组; 7-菜品; ];
     *              value: list<String>
     * @param syncStatus 同步状态 [null-所有; 1-同步中 2-同步完成 3-同步失败]
     * @return map key: idType [1-规格; 2-单位; 3-菜类; 4-套餐组; 7-菜品; ];
     *              value: list<SimpleSyncResultBo>
     */
    Result<Map<Integer, List<SimpleSyncResultBo>>> getSimpleSyncResult(String entityId, String platCode, Map<Integer, List<String>> map, Integer syncStatus);

    /**
     * 批量菜品估清
     * @param entityId entityId
     * @param platCode [107-口碑;]
     * @return 结果
     */
    Result selloutSync(String entityId, String platCode);

    /**
     * 批量删除口碑店铺菜品
     * @param entityId entityId
     * @param merchantId merchantId
     * @param shopId shopId
     * @return 结果
     */
    Result batchDelByMerchantIdShopId(String entityId, String merchantId, String shopId);

    /**
     * 批量删除菜品映射关系（不删除口碑数据）
     * @param entityId entityId
     * @param merchantId merchantId
     * @param shopId shopId
     * @return 结果
     */
    Result batchDelMapping(String entityId, String merchantId, String shopId);

    /**
     * 添加菜谱和菜谱明细
     * @param entityId entityId
     * @param merchantId merchantId
     * @param shopId shopId
     * @param cookBO 菜谱
     * @return 结果
     */
    Result addCookAndDetail(String entityId, String merchantId, String shopId, CookBO cookBO);

    /**
     * 批量删除口碑店铺菜品, 并同步
     * @param entityId entityId
     * @return 结果
     */
    Result clearAndSync(String entityId);

    /**
     * 同步菜品（强制）
     * @param entityId entityId
     * @param menuId menuId
     * @param isNeedDelete 是否先删除菜品
     * @return 结果
     */
    Result checkDish(String entityId, String menuId, boolean isNeedDelete);

    /**
     * 订正数据使用
     *
     * @return
     */
    Result syncHistory();

    /**
     * 订正数据使用
     *
     * @return
     */
    Result syncHistoryOfSameName();

    /**
     * 订正数据使用
     *
     * @return
     */
    Result queryKoubeiCook(int count);

    /**
     * 临时表加白名单
     *
     * @return
     */
    Result batchSyncForAddition(String[] entityIdList);
}
