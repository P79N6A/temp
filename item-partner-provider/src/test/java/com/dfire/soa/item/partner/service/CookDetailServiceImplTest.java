package com.dfire.soa.item.partner.service;

import com.alibaba.fastjson.JSONObject;
import com.dfire.soa.item.partner.ItemPartnerApplicationTests;
import com.dfire.soa.item.partner.bo.CookDetailBO;
import com.dfire.soa.item.partner.bo.SpecBO;
import com.dfire.soa.item.partner.bo.SpecExtBO;
import com.dfire.soa.item.partner.bo.query.CookDetailQuery;
import com.dfire.soa.item.partner.domain.CookDetailDO;
import com.dfire.soa.item.partner.manager.ICookDetailManager;
import com.dfire.soa.item.partner.service.internal.ICookDetailInService;
import com.twodfire.share.result.Result;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: xiaoji
 * @Date: create on 2018/9/3
 * @Describle:
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ItemPartnerApplicationTests.class)
@EnableTransactionManagement
@Transactional(rollbackFor = Exception.class)
public class CookDetailServiceImplTest {

	@Resource
	private ICookDetailService cookDetailService;

	@Resource
	private ICookDetailInService cookDetailInService;

	@Test
	public void testInsert() {
		CookDetailBO cookDetailBO = new CookDetailBO();
		cookDetailBO.setEntityId("99935916");
		cookDetailBO.setCookId(370281060312907776L);
		cookDetailBO.setMenuId("9993591665a5028e0165a92c43060f90");
		cookDetailBO.setUsePriceSwitch(1);
		cookDetailBO.setPrice(88);
		cookDetailBO.setMemberPrice(88);
		/*List<SpecBO> specBOList = new ArrayList<>();
		SpecBO specBO = new SpecBO();
		specBO.setSpecId("9993591663b402d40163b56e08bb01e3");
		specBO.setSpecPrice(0);
		SpecBO specBO2 = new SpecBO();
		specBO2.setSpecId("9993591663b402d40163b54b728a01b2");
		specBO2.setSpecPrice(5);
		specBOList.add(specBO);
		specBOList.add(specBO2);
		SpecExtBO specExtBO = new SpecExtBO();
		specExtBO.setSpecBOList(specBOList);
		cookDetailBO.setSpecExtBO(specExtBO);*/
		Assert.assertTrue(cookDetailService.insert(cookDetailBO).getModel());
	}

	@Test
	public void testUpdate() {
		CookDetailBO cookDetailBO = new CookDetailBO();
		cookDetailBO.setEntityId("99935916");
		cookDetailBO.setId(355425805486096384L);
		cookDetailBO.setMenuId("9993591663b402d40163b54b728a01b2");
		//cookDetailBO.setUsePriceSwitch(0);
		//cookDetailBO.setLastVer(3);
		Assert.assertTrue(cookDetailService.updateById(cookDetailBO).getModel());
	}

	@Test
	public void testSelect() {
		Result<List<CookDetailBO>> result = cookDetailService.selectByCookId("99935916", 355425232368009216L);
		System.out.println(JSONObject.toJSON(result));
		Assert.assertTrue(result.getModel() != null);
	}

	@Test
	public void testQuery() {
		Result<CookDetailBO> result = cookDetailService.queryById("99935916", 362175606579888128L);
		System.out.println(JSONObject.toJSON(result));
		Assert.assertTrue(result.getModel() != null);
	}

	@Test
	public void testDelete() {
		List<Long> longList = new ArrayList<>();
		longList.add(Long.valueOf("358257435061911552"));
		//longList.add(Long.valueOf("354662876516057088"));
		Result<Integer> result = cookDetailService.batchDeleteByIdList("99933225", longList);
		Assert.assertTrue(result.getModel() != null);
	}

	@Test
	public void testDeleteById() {
		Result<Boolean> result = cookDetailService.deleteById("99933225", 358257435061911552L);
		Assert.assertTrue(result.getModel());
	}

	@Test
	public void testAddCookDetailMenus() {
		List<String> list = new ArrayList<>();
		list.add("9993591665a5028e0165a92b3fbd0f7d");
		list.add("9993591665a5028e0165a92c43060f90");
		cookDetailService.addCookDetailMenus("99935916", 355425232368009216L, list);
	}

	@Test
	public void testDeleteByMenuId() {
		Boolean result = cookDetailInService.deleteByMenuId("99935916", "9993591665a5028e0165a92b3fbd0f7d");
		Assert.assertTrue(result);
	}

	@Test
	public void testSelectByQuery() {
		CookDetailQuery cookDetailQuery = new CookDetailQuery("99932363");
		cookDetailQuery.setCookId(357572999412547584L);
		//cookDetailQuery.setMenuId("99935916659fe7080165a3b8113f1bc7");
		cookDetailQuery.setIsValid(1);
		cookDetailQuery.setUsePage(true);
		List<CookDetailBO> cookDetailBOList = cookDetailInService.selectByQuery(cookDetailQuery);
		Assert.assertTrue(cookDetailBOList != null);
		System.out.println(JSONObject.toJSON(cookDetailBOList));
	}

	@Test
	public void test() {
		/*Result<List<CookDetailBO>> result = cookDetailService.selectByCookId("99933225",356488186450509824L);
		System.out.println(JSONObject.toJSON(result.getModel()));*/
		Result<CookDetailBO> result = cookDetailService.queryById("99935916", 357155764806221824L);
		System.out.println(JSONObject.toJSON(result.getModel()));
	}

	@Test
	public void testBatchDeleteByIdList() {
		/*List<Long> list = new ArrayList<>();
		list.add(368009270756769792L);
		list.add(368009270765158400L);
		list.add(368009270698049536L);*/
		/*list.add(357155764760084480L);
		list.add(362175606579888128L);
		list.add(362175606584082432L);
		list.add(362175606592471040L);
		list.add(362175606592503808L);
		list.add(362175606596698112L);
		list.add(362175606600859648L);
		list.add(362175606600892416L);
		list.add(362175606609281024L);*/
		CookDetailQuery cookDetailQuery = new CookDetailQuery("99935916");
		cookDetailQuery.setCookId(357155760389619712l);
		List<CookDetailDO> cookDetailDOList = cookDetailManager.selectByQuery(cookDetailQuery);
		List<Long> list = cookDetailDOList.stream().map(cookDetailDO -> cookDetailDO.getId()).collect(Collectors.toList());

		cookDetailService.batchDeleteByIdList("99935916", list);
	}

	@Resource
	private ICookDetailManager cookDetailManager;

	@Test
	public void test1(){
		List<String>  list = cookDetailManager.queryMenuIdsByCookId("99935916",357155760389619712l);
		System.out.println(JSONObject.toJSON(list));
	}

//	@Test
//	public void test2(){
//		List<String>  list = cookDetailManager.queryEntityIdWihtFewCookDetail();
//		System.out.println(JSONObject.toJSON(list));
//	}

	@Test
	public void testDeleteByMenuIdList(){
		CookDetailQuery cookDetailQuery = new CookDetailQuery("99935916");
		cookDetailQuery.setCookId(357155760389619712l);
		List<CookDetailDO> cookDetailDOList = cookDetailManager.selectByQuery(cookDetailQuery);
		List<String> list = cookDetailDOList.stream().map(cookDetailDO -> cookDetailDO.getMenuId()).collect(Collectors.toList());
		Integer count = cookDetailManager.deleteByMenuIdList("99935916",list);
		System.out.println(count);
	}

	@Test
	public void testBatchDeleteByMenuIdList(){
		String[] str = new String[]{"99935916"};
		cookDetailService.batchDeleteByMenuIdList(str);
	}
}
