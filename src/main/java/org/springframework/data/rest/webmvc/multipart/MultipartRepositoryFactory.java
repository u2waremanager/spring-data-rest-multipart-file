package org.springframework.data.rest.webmvc.multipart;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.rest.webmvc.multipart.environment.FileSystemMultipartRepository;
import org.springframework.data.rest.webmvc.multipart.environment.FileSystemProperties;

@RepositoryRestController
public class MultipartRepositoryFactory implements FactoryBean<MultipartRepository>{

	protected @Autowired FileSystemProperties fileSystemProperties;
	
	@Override
	public MultipartRepository getObject() throws Exception {
		return new FileSystemMultipartRepository(fileSystemProperties);
	}

	@Override
	public Class<?> getObjectType() {
		return MultipartRepository.class;
	}
}
