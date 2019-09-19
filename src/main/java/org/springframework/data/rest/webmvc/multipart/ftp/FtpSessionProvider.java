package org.springframework.data.rest.webmvc.multipart.ftp;

import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.ftp.session.FtpSession;

public class FtpSessionProvider extends DefaultFtpSessionFactory{
	
	public interface FtpSessionCallback<T> {
		T doWith(FtpSession session) throws Exception;
	}
	
	public <T> T doWithFtpSession(FtpSessionCallback<T> callback) throws Exception{
		
		FtpSession session = null;
		try {
			session = super.getSession();
			return callback.doWith(session);
		
		}catch(Exception e) {
			e.printStackTrace();
			throw e;
		}finally {
			if(session != null)
				session.close();
		}
	}
	

}