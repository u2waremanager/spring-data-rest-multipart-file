package org.springframework.data.rest.webmvc.multipart.environment;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "spring.data.rest.multipart.filesystem")
public class FileSystemProperties {

	private String location = "target/storage";

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	
	public boolean isUseable() {
		return StringUtils.hasText(location);
	}
}
