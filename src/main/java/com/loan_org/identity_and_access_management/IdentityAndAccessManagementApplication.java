package com.loan_org.identity_and_access_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class IdentityAndAccessManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(IdentityAndAccessManagementApplication.class, args);
	}

}
