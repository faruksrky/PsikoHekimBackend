package com_psikohekim.psikohekim_appt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication (exclude = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
public class PsikohekimApptApplication {

	public static void main(String[] args) {
		SpringApplication.run(PsikohekimApptApplication.class, args);
	}

}
