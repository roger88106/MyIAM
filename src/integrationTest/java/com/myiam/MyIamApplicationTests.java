package com.myiam;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * アプリケーションコンテキストが起動できることの検証。
 */
@SpringBootTest
@Import(TestcontainersConfig.class)
class MyIamApplicationTests {

	@Test
	void contextLoads() {
	}

}
