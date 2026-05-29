package com.api.RestAPI;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:restapi-test;MODE=MariaDB;NON_KEYWORDS=END;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.jpa.show-sql=false",
		"spring.rabbitmq.host=localhost",
		"spring.rabbitmq.port=5672",
		"spring.rabbitmq.username=guest",
		"spring.rabbitmq.password=guest",
		"spring.activemq.broker-url=vm://test?broker.persistent=false",
		"spring.activemq.user=",
		"spring.activemq.password=",
		"app.queue.name=test-appointments",
		"security.api-key=test-api-key",
		"security.user.password=test-password",
		"providers.swiftsend.url=http://localhost",
		"providers.swiftsend.api-key=test",
		"providers.swiftsend.student-group=test",
		"providers.legacylink.url=http://localhost",
		"providers.legacylink.authorization=test",
		"providers.legacylink.sender=test",
		"providers.legacylink.student-group=test",
		"providers.asyncflow.url=http://localhost",
		"providers.asyncflow.api-key=test",
		"providers.asyncflow.priority=normal",
		"providers.asyncflow.student-group=test",
		"providers.securepost.auth-url=http://localhost/auth",
		"providers.securepost.message-url=http://localhost/messages",
		"providers.securepost.client-id=test",
		"providers.securepost.client-secret=test",
		"providers.securepost.student-group=test",
		"spring.rabbitmq.ssl.enabled=false"
})
class RestApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
