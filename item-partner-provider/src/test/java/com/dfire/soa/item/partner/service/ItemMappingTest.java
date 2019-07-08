package com.dfire.soa.item.partner.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dfire.soa.item.partner.ItemPartnerApplicationTests;
import com.dfire.soa.item.partner.bo.BrandSyncResultBo;
import com.dfire.soa.item.partner.bo.ItemMapping;
import com.dfire.soa.item.partner.bo.query.CommonIdModel;
import com.dfire.soa.item.partner.bo.query.ItemMappingQuery;
import com.dfire.soa.item.partner.constant.CommonConstant;
import com.dfire.soa.item.partner.service.internal.IItemMappingService;
import com.twodfire.share.result.Result;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by GanShu on 2018/9/3 0003.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ItemPartnerApplicationTests.class)
@EnableTransactionManagement
@Transactional(rollbackFor = Exception.class)
public class ItemMappingTest {

    @Resource
    private IItemMappingService itemMappingService;
    @Resource
    private IItemMappingClientService itemMappingClientService;

    @Test
    public void cacheLocalIdsTest(){
        List<String> localIds = new ArrayList<>();
        localIds.add("999332255e983785015e99cfc756008d");
        Result<List<ItemMapping>> listResult = itemMappingClientService.getItemMappingListByLocalIds("107", (byte)7, "99933225", "2018082900077000000062153549", localIds);
        listResult = itemMappingClientService.getItemMappingListByLocalIds("107", (byte)7, "99933225", "2018082900077000000062153549", localIds);
        System.out.print(listResult);


        localIds.add("999332255e983785015e99cfc853008f");
        Result<List<ItemMapping>> listResult2 = itemMappingClientService.getItemMappingListByLocalIds("107", (byte)7, "99933225", "2018082900077000000062153549", localIds);
        listResult2 = itemMappingClientService.getItemMappingListByLocalIds("107", (byte)7, "99933225", "2018082900077000000062153549", localIds);
        System.out.print(listResult2);

        localIds.add("999332255e983785015e99cfc9430091");
        Result<List<ItemMapping>> listResult3 = itemMappingClientService.getItemMappingListByLocalIds("107", (byte)7, "99933225", "2018082900077000000062153549", localIds);
        listResult3 = itemMappingClientService.getItemMappingListByLocalIds("107", (byte)7, "99933225", "2018082900077000000062153549", localIds);
        System.out.print(listResult3);

        localIds.add("9993322565c2e1da0165c33c04aa0107");
        Result<List<ItemMapping>> listResult4 = itemMappingClientService.getItemMappingListByLocalIds("107", (byte)7, "99933225", "2018082900077000000062153549", localIds);
        listResult4 = itemMappingClientService.getItemMappingListByLocalIds("107", (byte)7, "99933225", "2018082900077000000062153549", localIds);
        System.out.print(listResult4);
    }

    @Test
    public void cacheTpIdsTest(){
        List<String> tpIds = new ArrayList<>();
        tpIds.add("D20180904000352425300007");
        Result<List<ItemMapping>> listResult = itemMappingClientService.getItemMappingListByTpIds("107", (byte)7, "99933225", "2018082900077000000062153549", tpIds);
        listResult = itemMappingClientService.getItemMappingListByTpIds("107", (byte)7, "99933225", "2018082900077000000062153549", tpIds);
        System.out.print(listResult);


        tpIds.add("D20180904000352437100008");
        Result<List<ItemMapping>> listResult2 = itemMappingClientService.getItemMappingListByTpIds("107", (byte)7, "99933225", "2018082900077000000062153549", tpIds);
        listResult2 = itemMappingClientService.getItemMappingListByTpIds("107", (byte)7, "99933225", "2018082900077000000062153549", tpIds);
        System.out.print(listResult2);

        tpIds.add("D20180904000352353800006");
        Result<List<ItemMapping>> listResult3 = itemMappingClientService.getItemMappingListByTpIds("107", (byte)7, "99933225", "2018082900077000000062153549", tpIds);
        listResult3 = itemMappingClientService.getItemMappingListByTpIds("107", (byte)7, "99933225", "2018082900077000000062153549", tpIds);
        System.out.print(listResult3);

        tpIds.add("D20180912000442509700001");
        Result<List<ItemMapping>> listResult4 = itemMappingClientService.getItemMappingListByTpIds("107", (byte)7, "99933225", "2018082900077000000062153549", tpIds);
        listResult4 = itemMappingClientService.getItemMappingListByTpIds("107", (byte)7, "99933225", "2018082900077000000062153549", tpIds);
        System.out.print(listResult4);
    }

    @Test
    public void cacheCommonIdModelsTest(){
        List<CommonIdModel> commonIdModels = new ArrayList<>();
        commonIdModels.add(new CommonIdModel("9993322565c3deb90165c7af14c60566", "99933225h3a4c5a901l3ab57b3580g58"));
        Result<List<ItemMapping>> listResult = itemMappingClientService.getItemMappingListByCommonIdModels("107", (byte)8, "99933225", "2018082900077000000062153549", commonIdModels);
        listResult = itemMappingClientService.getItemMappingListByCommonIdModels("107", (byte)8, "99933225", "2018082900077000000062153549", commonIdModels);
        System.out.print(listResult);


        commonIdModels.add(new CommonIdModel("9993322565c807ba0165cc98523b05a7", "9993322565c807ba0165cbb98a720298"));
        Result<List<ItemMapping>> listResult2 = itemMappingClientService.getItemMappingListByCommonIdModels("107", (byte)8, "99933225", "2018082900077000000062153549", commonIdModels);
        listResult2 = itemMappingClientService.getItemMappingListByCommonIdModels("107", (byte)8, "99933225", "2018082900077000000062153549", commonIdModels);
        System.out.print(listResult2);

        commonIdModels.add(new CommonIdModel("9993322565c807ba0165d0a415010783", "99933225h3a4c5a901l3ab57b3580g58"));
        Result<List<ItemMapping>> listResult3 = itemMappingClientService.getItemMappingListByCommonIdModels("107", (byte)8, "99933225", "2018082900077000000062153549", commonIdModels);
        listResult3 = itemMappingClientService.getItemMappingListByCommonIdModels("107", (byte)8, "99933225", "2018082900077000000062153549", commonIdModels);
        System.out.print(listResult3);

        commonIdModels.add(new CommonIdModel("9993322565c807ba0165d0e1e47c084b", "99933225h3a4c5a901l3ab57b3580g58"));
        Result<List<ItemMapping>> listResult4 = itemMappingClientService.getItemMappingListByCommonIdModels("107", (byte)8, "99933225", "2018082900077000000062153549", commonIdModels);
        listResult4 = itemMappingClientService.getItemMappingListByCommonIdModels("107", (byte)8, "99933225", "2018082900077000000062153549", commonIdModels);
        System.out.print(listResult4);
    }


    @Test
    public void queryTest() {
        ItemMappingQuery query = new ItemMappingQuery();
        int pageSize = 500;
        int pageIndex = 1;
        query.setTpShopId("0");
        query.setIsValid(1);
        query.setPageIndex(pageIndex);
        query.setPageSize(pageSize);
//        query.setEntityId("99932287");
        query.setOrderBy("entity_id");
        List<ItemMapping> list = itemMappingService.getItemMappingListByQueryWithoutEntityId(query);
        System.out.println(list);
    }

    @Test
    public void queryTpTest() {
        ItemMapping itemMapping = itemMappingService.getTpId("107", (byte)8, "99933225", "9993322565c2e1da0165c33c04aa0107", "99933225h3a4c5a901l3ab57b3580g58", "2018082900077000000062153549");
        System.out.println(itemMapping);
    }

    @Test
    public void deleteTest() {
        int count = itemMappingService.batchDeleteByEntityId("99226658", "99226658", "107");
        System.out.println(count);
    }

    @Test
    public void queryInvalidTest() {
       /* ItemMapping itemMapping = itemMappingService.getTpIdWithInvalid("107", (byte)7 , "99226658", "9922665865e65bbb0165e6772caf00a1", "2018091500077000000062717758");
        System.out.println(itemMapping);*/
    }

    @Test
    public void clientTest() {
        Result<ItemMapping> itemMappingResult = itemMappingClientService.getLocalId("107", (byte)5, "99927909", "C20180911020036649000006", "2018051700077000000052080788");
        System.out.println(itemMappingResult);
    }

    @Test
    public void itemMappingQueryTest() {
        ItemMappingQuery itemMappingQuery9 = new ItemMappingQuery("99226625", "2018091300077000000062673366", String.valueOf(CommonConstant.KOUBEI_PLATFORM), Integer.valueOf(CommonConstant.ADDITION), null, null);
        List<ItemMapping> itemMappings9 = itemMappingService.getItemMappingListByQuery(itemMappingQuery9);
        Map<String, String> additionIdmap = itemMappings9.stream().collect(Collectors.toMap(ItemMapping::getLocalId, ItemMapping::getTpId));
        System.out.println(JSONObject.toJSON(itemMappings9));
        //System.out.println(itemMappingResult);
    }

    @Test
    public void batchQueryFailCountTest() {
        List<String> list = new ArrayList<>();
        list.add("999279090");
//        list.add("99928869");
//        list.add("99936052");
        List<BrandSyncResultBo> result = itemMappingService.batchQueryFailCount("107", list);
        System.out.println(JSON.toJSONString(result));
    }



}
