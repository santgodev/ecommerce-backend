package com.farmatodo.order_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"spring.config.import=",
		"spring.cloud.config.enabled=false",
		"spring.datasource.url=jdbc:h2:mem:testdb",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"api.key=test-api-key",
		"spring.mail.host=localhost",
		"spring.mail.port=3025",
		"services.cartService.url=http://localhost:8084",
		"services.cartService.apiKey=test-cart-api-key",
		"services.client.url=http://localhost:8081",
		"services.client.apiKey=test-client-api-key",
		"services.product.url=http://localhost:8083",
		"services.product.apiKey=test-product-api-key",
		"services.token.url=http://localhost:8082",
		"services.token.apiKey=test-token-api-key"
})
class OrderServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
