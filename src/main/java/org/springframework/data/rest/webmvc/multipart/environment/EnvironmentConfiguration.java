package org.springframework.data.rest.webmvc.multipart.environment;

import org.springframework.context.annotation.Bean;
import org.springframework.data.rest.webmvc.RepositoryRestController;

@RepositoryRestController
public class EnvironmentConfiguration {

	@Bean
	public FileSystemProperties fileSystemProperties() {
		return new FileSystemProperties();
	}	
}
