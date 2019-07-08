package com.dfire.soa.item.partner.koubei.handler;

import com.alibaba.fastjson.JSON;
import com.dfire.soa.item.bo.Menu;
import com.dfire.soa.item.partner.bo.ItemMapping;
import com.dfire.soa.item.partner.bo.query.ItemMappingQuery;
import com.dfire.soa.item.partner.constant.CommonConstant;
import com.dfire.soa.item.partner.koubei.KouBeiCheckUtil;
import com.dfire.soa.item.partner.koubei.KouBeiDeleteUtil;
import com.dfire.soa.item.partner.service.internal.IItemMappingService;
import com.dfire.soa.item.service.IGetAdditionService;
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
 * @Author: xiaoji
 * @Date: create on 2018/11/6
 * @Describle:处理加料消息
 */
@Component
public class KoubeiAdditionHandler {
	@Resource
	private KouBeiCheckUtil kouBeiCheckUtil;
	@Resource
	private IGetAdditionService getAdditionService;
	@Resource
	private KouBeiDeleteUtil kouBeiDeleteUtil;
	@Resource
	private IItemMappingService itemMappingService;
	@Resource
	private KoubeiDishHandler koubeiDishHandler;

	private static final Logger bizLog = LoggerFactory.getLogger(CommonConstant.BIZ_LOG);

	/**
	 * 新增/修改加料处理
	 *
	 * @param merchantId merchantId
	 * @param menu       加料
	 * @return boolean
	 */
	public boolean insertOrUpdate(String merchantId, String shopId, Menu menu) {
		String tpId = kouBeiCheckUtil.checkAffiliateId(merchantId, shopId,  menu.getId(), menu.getEntityId(),true, menu.getLastVer());
		return StringUtils.isNotBlank(tpId);
	}

	/**
	 * 删除加料处理
	 *
	 * @param merchantId merchantId
	 * @param menu       加料
	 * @return boolean
	 */
	public boolean delete(String merchantId, String shopId, Menu menu) {
		String entityId = menu.getEntityId();
		String localId = menu.getId();

		//查询加料
		Result<List<Menu>> menuResult = getAdditionService.listAdditionAll(entityId);
		if (!menuResult.isSuccess()) {
			bizLog.error("[kb_databack][error]getAdditionService.listAdditionAll(entityId) failed. entityId: " + entityId + ", menuResult: " + JSON.toJSONString(menuResult));
			throw new BizException("[kb_databack][error]查询加料信息失败");
		} else if (CollectionUtils.isNotEmpty(menuResult.getModel()) && menuResult.getModel().stream().anyMatch(vo -> Objects.equals(vo.getId(), localId))) {
			return false;
		}

		//加料菜被当成菜品处理删除去掉错误数据,放一段时间去掉
		ItemMappingQuery itemMappingQuery = new ItemMappingQuery(entityId, shopId, "107", (int) CommonConstant.ITEM, menu.getId(), null);
		List<ItemMapping> itemMappingList = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
		if (CollectionUtils.isNotEmpty(itemMappingList)) {
			//删除默认sku
			String defaultSpecId = entityId + CommonConstant.KOUBEI_DEFAULT_SPEC_ID;
			kouBeiDeleteUtil.deleteSkuId(merchantId, shopId, entityId, menu.getId(), defaultSpecId);

			//删除菜品
			kouBeiDeleteUtil.deleteDishId(merchantId, shopId, menu);
		}
		//删除加料
		return kouBeiDeleteUtil.deleteAffiliateId(merchantId, shopId, menu);
	}
}
