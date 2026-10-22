package com.hemovia.api;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Sobe o contexto completo (precisa do PostgreSQL): roda na esteira, não no build da imagem
@Tag("integration")
@SpringBootTest
class ApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
