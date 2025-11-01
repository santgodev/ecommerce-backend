package com.farmatodo.product_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Product Service Application Tests
 * Fixed: Added H2 database and required test properties
 */
@SpringBootTest
@TestPropertySource(properties = {
		"spring.cloud.config.enabled=false",
		"spring.config.import=",
		"api.key=test-api-key-12345"
})
class ProductServiceApplicationTests {

	@Test
	void contextLoads() {
		// Context loads successfully with H2 test database
	}

}
