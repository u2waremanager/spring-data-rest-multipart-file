package org.springframework.data.rest.webmvc.multipart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.rest.webmvc.multipart.ftp.FtpSessionService;
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

	@Bean("oops")
	public MultipartService mutipartService2() {
		FtpSessionService ftp = new FtpSessionService();
		ftp.setHost("cdnupload.cdn.cloudn.co.kr");
		ftp.setUsername("sigongmedia_dw-hiclass");
		ftp.setPassword("dwhiclass1@");
		ftp.setLocation("/test");
		ftp.setUseMetadata(false);
		return ftp;
	}

//	@Bean("dwoops")
//	public MultipartService mutipartService3() {
//		FtpSessionService ftp = new FtpSessionService();
//		ftp.setHost("cdnupload.cdn.cloudn.co.kr");
//		ftp.setUsername("sigongmedia_hiclass");
//		ftp.setPassword("hiclass1@");
//		return ftp;
//	}
	
}
