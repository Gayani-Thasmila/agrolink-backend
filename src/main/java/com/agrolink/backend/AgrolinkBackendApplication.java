package com.agrolink.backend;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AgrolinkBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgrolinkBackendApplication.class, args);
	}

}
