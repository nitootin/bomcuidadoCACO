package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "cuidar.normalizacao-dados.enabled=false")
class CuidarApplicationTests {

	@Test
	void contextLoads() {
	}

}
