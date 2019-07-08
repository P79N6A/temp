package com.dfire.soa.item.partner.service.impl;

import com.dfire.soa.item.partner.bo.CookBO;
import com.dfire.soa.item.partner.service.ICookService;
import com.dfire.soa.item.partner.service.internal.ICookInService;
import com.twodfire.share.result.Result;
import com.twodfire.share.result.ResultSupport;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @Author: xiaoji
 * @Date: create on 2018/8/27
 * @Describle:
 */
@Service("cookService")
public class CookServiceImpl implements ICookService {

	@Resource
	private ICookInService cookInService;

	@Override
	public Result<Boolean> insert(CookBO cookBO) {
		Result<Boolean> result = new ResultSupport<>();
		result.setModel(cookInService.insert(cookBO));
		return result;
	}

	@Override
	public Result<Boolean> updateById(String[] entityId) {
		Result<Boolean> result = new ResultSupport<>();
		Boolean flag = Boolean.FALSE;
		for(String eId:entityId){
			CookBO cookBO = cookInService.selectByType(eId,107);
			flag = cookInService.updateById(cookBO);
		}
		result.setModel(flag);
		return result;
	}

	@Override
	public Result<Boolean> deleteById(String entityId, Long id) {
		Result<Boolean> result = new ResultSupport<>();
		result.setModel(cookInService.deleteById(entityId, id));
		return result;
	}

	@Override
	public Result<CookBO> selectById(String entityId, Long id) {
		Result<CookBO> result = new ResultSupport<>();
		result.setModel(cookInService.queryById(entityId, id));
		return result;
	}

	@Override
	public Result<CookBO> selectByType(String entityId, int type) {
		Result<CookBO> result = new ResultSupport<>();
		result.setModel(cookInService.selectByType(entityId, type));
		return result;
	}

	@Override
	public Result<Integer> batchInsertByIdList(Map<String, Long> idList) {
		Result<Integer> result = new ResultSupport<>();
		result.setModel(cookInService.batchInsertByIdList(idList));
		return result;
	}

	@Override
	public Result batchDelete(Long time) {
		Result<Integer> result = new ResultSupport<>();
		result.setModel(cookInService.batchDelete(time));
		return result;
	}

	@Override
	public Result updateStatus() {
		Result<Integer> result = new ResultSupport<>();
		cookInService.updateStatus();
		return result;
	}

	@Override
	public Result<List<String>> getEntityIdList(long startTime, long endTime) {
		Result<List<String>> result = new ResultSupport<>();
		List<String> list = cookInService.getEntityIdList(startTime, endTime);
		result.setModel(list);
		return result;
	}
}
