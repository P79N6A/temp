package com.dfire.soa.item.partner.service;

import com.dfire.rest.util.common.exception.OpenApiException;
import com.dfire.soa.item.partner.ItemPartnerApplicationTests;
import com.dfire.soa.item.partner.bo.CookBO;
import com.dfire.soa.item.partner.bo.CookDetailBO;
import com.dfire.soa.item.partner.koubei.handler.KoubeiCookDishHandler;
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
 * Created by GanShu on 2018/9/12 0012.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ItemPartnerApplicationTests.class)
@EnableTransactionManagement
@Transactional(rollbackFor = Exception.class)
public class KoubeiCookDishHandlerTest {

    @Resource
    private KoubeiCookDishHandler koubeiCookDishHandler;

//    @Test
//    public void cookDetailSyncTest() throws OpenApiException {
//
//        Map<String, String> map = new HashMap<>();
//        map.put("entity_id", "");
//        CookDetailBO cookDetailBO = koubeiCookDishHandler.getMultipleMenuElement("99933225", 356488186450509824L, "9993322565c2e1da0165c33c04aa0107");
//        CookBO cookBO  = koubeiCookDishHandler.getCookBO("99933225", 356488186450509824L, "2018082900077000000062153549");
//        koubeiCookDishHandler.updateCookDetailForSync(cookDetailBO, cookBO, "99933225", "2088312258383825", 356488186450509824L, "9993322565c2e1da0165c33c04aa0107", "2018082900077000000062153549");
//
//    }


}
