package com_psikohekim.psikohekim_appt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com_psikohekim.psikohekim_appt.service") // FeignClient'lerin bulunduğu paket
public class PsikohekimApptApplication {

	public static void main(String[] args) {
		SpringApplication.run(PsikohekimApptApplication.class, args);
	}

}
