package com.dfire.soa.item.partner.service;

import com.alibaba.fastjson.JSONObject;
import com.dfire.soa.item.partner.ItemPartnerApplicationTests;
import com.dfire.soa.item.partner.bo.CookBO;
import com.dfire.soa.item.partner.enums.EnumCookSubType;
import com.dfire.soa.item.partner.enums.EnumCookType;
import com.dfire.soa.item.partner.service.internal.ICookInService;
import com.twodfire.share.result.Result;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: xiaoji
 * @Date: create on 2018/9/1
 * @Describle:
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ItemPartnerApplicationTests.class)
@EnableTransactionManagement
@Transactional(rollbackFor = Exception.class)
public class CookServiceImplTest {

	@Resource
	private ICookService cookService;

	@Resource
	private ICookInService cookInService;

	@Test
	public void testInsert() {
		CookBO cookBO = new CookBO();
		cookBO.setName("小蓟测试口碑菜单");
		cookBO.setStatus(1);
		cookBO.setType(EnumCookType.KOUBEI.getCode());
		cookBO.setSubType(EnumCookSubType.EAT_IN.getCode());
		cookBO.setEntityId("99935916");
		Assert.assertTrue(cookService.insert(cookBO).getModel());
	}

	@Test
	public void testUpdate() {
		CookBO cookBO = new CookBO();
		cookBO.setId(354662875752660992L);
		cookBO.setName("小蓟菜单");
		cookBO.setEntityId("99935916");
		cookBO.setLastVer(0);
		cookBO.setStatus(1);
		cookBO.setType(EnumCookType.KOUBEI.getCode());
		cookBO.setSubType(EnumCookSubType.EAT_IN.getCode());
		String[] str = new String[]{"99935916"};
		Assert.assertTrue(cookService.updateById(str).getModel());
	}

	@Test
	public void testSelectById() {
		Result<CookBO> cookBOResult = cookService.selectById("99935916", 354662875752660992L);
		System.out.println(JSONObject.toJSON(cookBOResult));
		Assert.assertTrue(cookBOResult.getModel() != null);
	}

	@Test
	public void testSelectByType() {
		Result<CookBO> cookBOResult = cookService.selectByType("99935916", EnumCookType.KOUBEI.getCode());
		System.out.println(JSONObject.toJSON(cookBOResult.getModel()));
		Assert.assertTrue(cookBOResult.getModel() != null);
	}

	@Test
	public void testBatchInsertByIdList() {
		Map<String, Long> map = new HashMap<>();
		map.put("99926544", 0L);
		map.put("99935916",0L);
		map.put("99933225",356488186450509824L);
		Long start = System.currentTimeMillis();
		System.out.println(JSONObject.toJSON(map));
		Integer count = cookInService.batchInsertByIdList(map);
		Long end = System.currentTimeMillis();
		System.out.println(end-start);
		Assert.assertTrue(count != 0);
	}
}
