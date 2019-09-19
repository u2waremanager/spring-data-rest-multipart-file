package org.springframework.data.rest.webmvc.multipart;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.springframework.data.rest.webmvc.multipart.ftp.FtpSessionService;
import org.springframework.data.rest.webmvc.multipart.local.FileSystemUtils;

public class MultipartTests extends ApplicationTests{

	@Test
	public void contextLoads() throws Exception {
		
		String filename = System.currentTimeMillis()+".txt";
		
		Path path = Files.createFile(Paths.get("target/storage/"+filename));
		FileSystemUtils.setAttribute(path, "a", "b");

		FtpSessionService ftp = new FtpSessionService();
		ftp.setLocation("/");
		ftp.setHost("cdnupload.cdn.cloudn.co.kr");
		ftp.setUsername("sigongmedia_dw-hiclass");
		ftp.setPassword("dwhiclass1@");

		
		File f = new File("target/aaaa.txt");
		FileOutputStream ff = new FileOutputStream(f);
		
		ftp.doWithFtpSession(session->{
			session.write(new FileInputStream(path.toFile()), "/"+filename);
			session.read("/"+filename, ff);
			return null;
		});
		ff.close();
		
		logger.info(FileSystemUtils.getAttribute(f.toPath(), "a"));
		logger.info(FileSystemUtils.getAttribute(f.toPath(), "a"));
		logger.info(FileSystemUtils.getAttribute(f.toPath(), "a"));
	}
}
