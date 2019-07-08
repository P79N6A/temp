package com.dfire.soa.item.partner.service.impl;

import com.dfire.soa.item.partner.bo.CookDetailBO;
import com.dfire.soa.item.partner.bo.query.CookDetailQuery;
import com.dfire.soa.item.partner.constants.CommonConstants;
import com.dfire.soa.item.partner.service.ICookDetailService;
import com.dfire.soa.item.partner.service.internal.ICookDetailInService;
import com.dfire.soa.item.partner.service.internal.ICookInService;
import com.dfire.soa.item.vo.MenuVO;
import com.twodfire.share.result.Result;
import com.twodfire.share.result.ResultSupport;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: xiaoji
 * @Date: create on 2018/8/27
 * @Describle:
 */
@Service("cookDetailService")
public class CookDetailServiceImpl implements ICookDetailService {

	@Resource
	private ICookDetailInService cookDetailInService;

	@Resource
	private ICookInService cookInService;

	@Override
	public Result<Boolean> insert(CookDetailBO cookDetailBO) {
		Result<Boolean> result = new ResultSupport<>();
		result.setModel(cookDetailInService.insert(cookDetailBO));
		return result;
	}

	@Override
	public Result<Boolean> updateById(CookDetailBO cookDetailBO) {
		Result<Boolean> result = new ResultSupport<>();
		result.setModel(cookDetailInService.updateById(cookDetailBO));
		return result;
	}

	@Override
	public Result<Boolean> deleteById(String entityId, Long id) {
		Result<Boolean> result = new ResultSupport<>();
		result.setModel(cookDetailInService.deleteById(entityId, id));
		return result;
	}

	@Override
	public Result addCookDetailMenus(String entityId, Long cookId, List<String> menuIdList) {
		Result result = new ResultSupport<>();
		cookDetailInService.addCookDetailMenus(entityId, cookId, menuIdList);
		return result;
	}

	@Override
	public Result<Integer> batchDeleteByIdList(String entityId, List<Long> idList) {
		Result<Integer> result = new ResultSupport<>();
		result.setModel(cookDetailInService.batchDeleteByIdList(entityId, idList));
		return result;
	}

	@Override
	public Result<List<CookDetailBO>> selectByQuery(String entityId) {
		Result<List<CookDetailBO>> result = new ResultSupport<>();
		CookDetailQuery cookDetailQuery = new CookDetailQuery(entityId);
		cookDetailQuery.setIsValid(CommonConstants.IsValid.VALID);
		cookDetailQuery.setUsePage(true);
		cookDetailQuery.setOrderBy(CookDetailQuery.ORDER_CreateTime_Desc_Id_Desc);
		result.setModel(cookDetailInService.selectByQuery(cookDetailQuery));
		return result;
	}

	@Override
	public Result<List<CookDetailBO>> selectByCookId(String entityId, Long cookId) {
		Result<List<CookDetailBO>> result = new ResultSupport<>();
		CookDetailQuery cookDetailQuery = new CookDetailQuery(entityId);
		cookDetailQuery.setIsValid(CommonConstants.IsValid.VALID);
		cookDetailQuery.setCookId(cookId);
		cookDetailQuery.setUsePage(true);
		cookDetailQuery.setOrderBy(CookDetailQuery.ORDER_CreateTime_Desc_Id_Desc);
		result.setModel(cookDetailInService.selectByQuery(cookDetailQuery));
		return result;
	}

	@Override
	public Result<CookDetailBO> queryById(String entityId, Long id) {
		Result<CookDetailBO> result = new ResultSupport<>();
		CookDetailBO cookDetailBO = cookDetailInService.queryById(entityId, id);
		result.setModel(cookDetailBO);
		return result;
	}

	@Override
	public Result<List<String>> queryEntityIdWihtFewCookDetail() {
		Result<List<String>> result = new ResultSupport<>();
		List<String> list = cookDetailInService.queryEntityIdWihtFewCookDetail();
		result.setModel(list);
		return result;
	}

	@Override
	public Result<Integer> batchDeleteByMenuIdList(String[] entityIdList) {
		Result<Integer> result = new ResultSupport<>();
		result.setModel(cookInService.batchDeleteByMenuIdList(entityIdList));
		return result;
	}

	@Override
	public Result<Integer> queryCountByMenuIdAndCreateTime(String entityId, Long cookId, String menuId) {
		Result<Integer> result = new ResultSupport<>();
		result.setModel(cookInService.queryCountByMenuIdAndCreateTime(entityId, cookId, menuId));
		return result;
	}
}
