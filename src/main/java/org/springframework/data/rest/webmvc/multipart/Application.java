package org.springframework.data.rest.webmvc.multipart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.rest.webmvc.multipart.local.FileSystemService;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	
	@Bean("default")
	public MultipartService mutipartService1() {
		return new FileSystemService();
	}

	
}
