 package org.springframework.data.rest.webmvc.multipart;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;

public class MultipartServiceTests extends ApplicationTests{


	@Autowired @Qualifier("default")
	private MultipartService defaultService;

	
	@Test
	public void contextLoads() throws Exception {

		defaultService.childs("", PageRequest.of(0, 10)).forEach(m->{
			logger.info(m.getFilename());
		});
		
		Long name = System.currentTimeMillis();
		MockMultipartFile f =  new MockMultipartFile("file", name+".html", "text/plain", name.toString().getBytes());
		
		$.MULTIPART("/multipart/default").F(f).P("random", "false").is2xx();//.and(docs.uploadFile("upload-step1"));
		
		$.GET("/multipart/default/"+name+".html").is2xx();
		$.GET("/multipart/default/"+name+".html").P("flag", "preview").is2xx();
		$.GET("/multipart/default/"+name+".html").P("flag", "download").is2xx();
	}	
}
