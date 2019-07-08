package com.dfire.soa.item.partner.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dfire.open.takeout.bo.kb.KbDishAdditionResponse;
import com.dfire.open.takeout.service.IKouBeiDishCookService;
import com.dfire.soa.item.partner.ItemPartnerApplicationTests;
import com.dfire.soa.item.partner.bo.BrandSyncResultBo;
import com.dfire.soa.item.partner.bo.ItemMapping;
import com.dfire.soa.item.partner.bo.SimpleSyncResultBo;
import com.dfire.soa.item.partner.bo.SyncResultBo;
import com.dfire.soa.item.partner.bo.query.ItemMappingQuery;
import com.dfire.soa.item.partner.constant.CommonConstant;
import com.dfire.soa.item.partner.koubei.KouBeiCheckUtil;
import com.dfire.soa.item.partner.koubei.KouBeiDeleteUtil;
import com.dfire.soa.item.partner.koubei.handler.KoubeiSuitMenuHandler;
import com.dfire.soa.item.partner.koubei.service.IKoubeiSyncService;
import com.dfire.soa.item.partner.rocketmq.IItemRmqService;
import com.dfire.soa.item.partner.service.internal.IItemMappingService;
import com.dfire.soa.msstate.bo.MenuBalance;
import com.dfire.soa.msstate.service.IMenuBalanceClientService;
import com.twodfire.redis.CodisService;
import com.twodfire.share.result.Result;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhishi on 2018/9/10 0010.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ItemPartnerApplicationTests.class)
@EnableTransactionManagement
@Transactional(rollbackFor = Exception.class)
public class SyncTest {
    @Resource
    private IKoubeiSyncService koubeiSyncService;
    @Resource
	private IKouBeiDishCookService kouBeiDishCookService;
    @Resource
    private IItemMappingService itemMappingService;
    @Resource
    private KouBeiCheckUtil kouBeiCheckUtil;
    @Resource
    private KouBeiDeleteUtil kouBeiDeleteUtil;
    @Resource
    private KoubeiSuitMenuHandler koubeiSuitMenuHandler;
    @Resource
    private IItemRmqService itemRmqService;
    @Resource
    private CodisService codisService;
//    @Resource
//    private CodisService codisService;

    @Test
    public void itemSync(){
        Result<SyncResultBo> result = koubeiSyncService.itemSync("99935916","107");
        System.out.print(JSON.toJSONString(result));
		System.out.println(result);
    }
    @Test
    public void getItemSyncResult(){
        Result<SyncResultBo> result = koubeiSyncService.getItemSyncResult("99226658", "107");
        System.out.print(JSON.toJSONString(result));
    }



    @Test
    public void batchItemSync(){
        Map<Integer, List<String>> map = new HashMap<>();
        map.put(7, new ArrayList<String>(){{add("9993322565c807ba0165cc98523b05a7"); add("9993322565c807ba0165d0d5c2c20846");}});
        Result<SyncResultBo> result = koubeiSyncService.batchItemSync("99933225", "107", map);
        System.out.print(JSON.toJSONString(result));
    }
    @Test
    public void getBatchItemSyncResult(){
        Result<SyncResultBo> result = koubeiSyncService.getBatchItemSyncResult("99226619", "107");
        System.out.print(JSON.toJSONString(result));
    }

    @Test
    public void testQueryAddition(){
		Result<KbDishAdditionResponse> result = kouBeiDishCookService.queryAdditionListByMaterialId("2088312258383825",null,99,1);
		System.out.println(JSONObject.toJSON(result));
    }


    @Test
    public void getSimpleSyncResult(){
        Result<Map<Integer, List<SimpleSyncResultBo>>>  result = koubeiSyncService.getSimpleSyncResult("99935916", "107", null, 3);
        System.out.print(JSON.toJSONString(result));
    }
    @Test
    public void getSimpleSyncResult2(){
        Map<Integer, List<String>> map = new HashMap<>();
        map.put(7, new ArrayList<String>(){{add("99226619661b56db01661e2f95320185");}});
        Result<Map<Integer, List<SimpleSyncResultBo>>> result =koubeiSyncService.getSimpleSyncResult("99226619","107", map,null);
//        Map<Integer, List<String>> map = new HashMap<>();
//        map.put(7, null);
//        Result<Map<Integer, List<SimpleSyncResultBo>>> result = koubeiSyncService.getSimpleSyncResult("99226627","107", map,null);
        System.out.print(JSON.toJSONString(result));
    }

    @Test
    public void testDish(){
       kouBeiCheckUtil.checkDishId("2088122968268363", "2018090400077000000062294178", "99935916", "99935916633e83cf01633f8b75230018", true, null, null);
    }

    @Test
    public void testSku(){
        kouBeiCheckUtil.checkBatchSkuId("2088802642981091", "2018091100077000000062619785", "99226590", "9922659065eaa2f10165eb35c078004f", true, null, null);
    }

    @Test
    public void testDel(){
        Result  result = koubeiSyncService.batchDelByMerchantIdShopId("99935916", "2088122968268363", "2018090400077000000062294178");
        System.out.print(JSON.toJSONString(result));
    }

    @Test
    public void testDelSku(){
        kouBeiDeleteUtil.deleteSkuId("2088802642981091","2018091100077000000062619785", "99226590", "9922659065eaa2f10165eb35c078004f", "9922659065eaa2f10165eb5894ce00a9");
    }

    @Test
    public void testMsg(){
        Map<String, String> syncDishResult = new HashMap<>();//老版本同步结果
        syncDishResult.put("success", "true");
        syncDishResult.put("entityId", "99928644");
        itemRmqService.rocketMqTransmit("DISH_SYNC", JSON.toJSONString(syncDishResult));//发送消息通知
    }

    @Test
    public void testQuery(){
        ItemMappingQuery itemMappingQuery = new ItemMappingQuery("99926516", "2018040800077000000048211049", "107", 7, "9992651657469ec001576409524800be", null);
        List<ItemMapping> itemMappings = itemMappingService.getItemMappingListByQuery(itemMappingQuery);
        System.out.println(JSON.toJSONString(itemMappings));


    }

    @Test
    public void testAsync(){
        koubeiSyncService.syncHistoryOfSameName();
    }

    @Test
    public void testCache(){
        String entityId = "99226590";
        String key1 = CommonConstant.KOUBEI_ITEM_SYNC_RESULT + "107" + entityId;//缓存同步结果的key

        SyncResultBo syncResultBo = new SyncResultBo(1);//新版同步结果 1-全量同步 2-批量同步
        syncResultBo.setSyncStatus(1);                            //1-同步中 2-同步完成 3-同步失败 4-未同步 5-已同步
        syncResultBo.setEntityId(entityId);
        syncResultBo.setType(1);
        syncResultBo.setBusinessId(null);
        syncResultBo.setSyncStatus(3);
        syncResultBo.setErrorMsg("test");
        codisService.setObject(key1, syncResultBo, 30*60);

    }

    @Test
    public void testkk(){

        Integer str = 10000;
        testkkk(str);

        System.out.print("success");
    }

    private void  testkkk(Integer str){
        str = str+1;

    }

    @Test
    public void testbatchSyncForAddition(){
    	String[] str = new String[]{"99935916"};
        koubeiSyncService.batchSyncForAddition(null);
    }

    @Resource
	private IMenuBalanceClientService menuBalanceClientService;

    @Test
	public void testsellout(){
    	List<String> list =new ArrayList<>();
    	list.add("99226601670be9b601670c6793a4059f");
		Result<List<MenuBalance>> menuBalanceListResult = menuBalanceClientService.getMenuBalanceList("99226601",list);
		System.out.println(JSONObject.toJSON(menuBalanceListResult.getModel()));

	}

	@Test
	public void cahceList() {
        String key = "pppppppppppppppppppp";
        codisService.rpush(key, 60, "1");
        codisService.rpush(key, 60, "2");
        codisService.rpush(key, 60, "3");
        codisService.lpop(key);
        codisService.lpop(key);
        codisService.lpop(key);
        codisService.lpop(key);
        List<String> list = codisService.lrange(key, 0, -1);
        List<String> list_ = codisService.lrange(key, 0, -1);
        System.out.println(JSON.toJSONString(list));
    }

    @Test
    public void brandItemSyncTest() {
        List<String> list = new ArrayList<>();
        list.add("4354356");
        koubeiSyncService.brandItemSync("23343543", list, "107");
    }

    @Test
    public void getBrandItemSyncResultTest() {
        Result<BrandSyncResultBo> result = koubeiSyncService.getBrandItemSyncResult("99600576", "107");
        System.out.println(JSON.toJSONString(result));
    }

}
