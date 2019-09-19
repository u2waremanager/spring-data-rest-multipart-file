 package org.springframework.data.rest.webmvc.multipart;

import java.io.File;
import java.io.FileInputStream;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class MultipartServiceTests extends ApplicationTests{


	@Autowired @Qualifier("default")
	private MultipartService defaultService;

	
	@Autowired @Qualifier("oops")
	private MultipartService ftpService;

	
	@Test
	public void contextLoads() throws Exception {
		

//		defaultService.childs("", PageRequest.of(0, 10)).forEach(m->{
//			logger.info(m.getFilename());
//		});;
//		$.GET("/multipart/default/a.html").is2xx();
//		$.GET("/multipart/default/a.html").P("flag", "preview").is2xx();
//		$.GET("/multipart/default/a.html").P("flag", "download").is2xx();

		ftpService.childs("", PageRequest.of(0, 10)).forEach(m->{
			logger.info(m.getFilename()+" "+m.getContentType());
		});
		
		$.MULTIPART("/multipart/oops").F(createTextResource()).P("random", "false").is2xx();
		
		$.GET("/multipart/oops").P("flag", "childs").is2xx();


	}	
	
	private MockMultipartFile createTextResource() throws Exception {
		Long name = System.currentTimeMillis();
		return new MockMultipartFile("file", name+".txt", "text/plain", name.toString().getBytes());
	}
	
	private MockMultipartFile createMediaResource() throws Exception {
		Long name = System.currentTimeMillis();
		File sample = new File("./docs/sample.mp4");
		return new MockMultipartFile("file", name+".mp4", "text/plain", new FileInputStream(sample));
	}
	
}
