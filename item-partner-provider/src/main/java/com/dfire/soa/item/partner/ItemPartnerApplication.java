package com.dfire.soa.item.partner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication(scanBasePackages = {"com.dfire.soa.item.partner"})
@ImportResource({"classpath:spring-content.xml"})
public class ItemPartnerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ItemPartnerApplication.class, args);
	}
}
