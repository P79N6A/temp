package com.dfire.soa.item.partner.service;

import com.dfire.soa.item.partner.ItemPartnerApplicationTests;
import com.dfire.soa.item.partner.bo.ItemMenuMapping;
import com.dfire.soa.item.partner.service.internal.IItemMenuMappingService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * Created by GanShu on 2018/9/12 0012.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ItemPartnerApplicationTests.class)
@EnableTransactionManagement
@Transactional(rollbackFor = Exception.class)
public class ItemMenuMappingTest {

    @Resource
    private IItemMenuMappingService itemMenuMappingService;

    @Test
    public void queryTest() {
        ItemMenuMapping itemMenuMapping = itemMenuMappingService.getItemMenuMappingById("99933225", 356521883274969088L);
        System.out.println(itemMenuMapping);
    }



}
