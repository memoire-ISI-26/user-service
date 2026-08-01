package com.financedomain.user;

import com.financedomain.user.service.AdminService;
import com.financedomain.user.service.ClientService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
		"server.port=8082",
		"user-service.uriport=8082",
		"user-service.urlregistry=http://localhost:8761/eureka",
		"user-service.showsql=true",
		"spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"eureka.client.enabled=false",
		"spring.cloud.config.enabled=false"
})
class UserServiceApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private AdminService adminService;

	@Autowired
	private ClientService clientService;

	@Test
	@DisplayName("Vérifie le chargement du contexte Spring Boot et des beans pour user-service")
	void contextLoads() {
		assertNotNull(applicationContext, "Le contexte Spring Boot du user-service doit s'initialiser correctement.");
		assertThat(adminService).isNotNull();
		assertThat(clientService).isNotNull();
	}

}
