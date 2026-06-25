package com.trabalho.verificadocs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class VerificaDocsApplication {

	public static void main(String[] args) {
		SpringApplication.run(VerificaDocsApplication.class, args);
	}

}
