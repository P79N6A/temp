package com.dfire.soa.item.partner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@SpringBootApplication
@EnableAspectJAutoProxy
@EnableCaching
@EnableAutoConfiguration(exclude=SolrAutoConfiguration.class)
@ComponentScan(basePackages = {"com.dfire.soa.item.partner"})
public class ItemPartnerApplicationTests {

	@Test
	public void contextLoads() {
	}
	private static ConfigurableApplicationContext applicationContext;

	public static void main(String[] args) {
		applicationContext = SpringApplication.run(ItemPartnerApplicationTests.class, args);
	}

	public static void exit() {
		SpringApplication.exit(applicationContext);
	}

}
