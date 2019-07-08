package com.dfire.soa.item.partner.service;

import com.dfire.soa.item.partner.ItemPartnerApplicationTests;
import com.dfire.soa.item.partner.bo.PartnerMapping;
import com.dfire.soa.item.partner.bo.query.PartnerMappingQuery;
import com.dfire.soa.item.partner.enums.EnumMappingType;
import com.twodfire.share.result.Result;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by GanShu on 2018/9/3 0003.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ItemPartnerApplicationTests.class)
//@EnableTransactionManagement(proxyTargetClass = true)
//@Transactional(rollbackFor = Exception.class)
public class PartnerMappingTest {

    @Resource
    private IPartnerMappingClientService partnerMappingClientService;


    @Test
    public void saveOrUpdateTest() {

        PartnerMapping partnerMapping = new PartnerMapping();
        partnerMapping.setId(428921388418564096L);
        partnerMapping.setEntityId("99929817");
        partnerMapping.setShopId("2018111400077000000065842489");
        partnerMapping.setLocalId("20181224002361000000770000014423");
        partnerMapping.setOutId("2088000441375244");
        partnerMapping.setMpType(EnumMappingType.MAPP_PAY_USER_ID.getCode());

        Result<PartnerMapping>  result= partnerMappingClientService.saveOrUpdatePartnerMapping(partnerMapping);
        System.out.println(result);
    }


    @Test
    public void queryTest() {
        PartnerMappingQuery query = new PartnerMappingQuery();
        int pageSize = 500;
        int pageIndex = 1;

        query.setPageIndex(pageIndex);
        query.setPageSize(pageSize);
        query.setEntityId("99929817");
        query.setOrderBy("entity_id");
        query.setMpType(EnumMappingType.MAPP_PAY_USER_ID.getCode());
        Result<List<PartnerMapping>> result = partnerMappingClientService.getPartnerMappingListByQuery(query);
        System.out.println(result);
    }

    
    @Test
    public void deleteTest() {
        Result<Integer> result= partnerMappingClientService.deletePartnerMappingById("99929817", 428921388418564096L);
        System.out.println(result);
    }
    



}
