package org.springframework.data.rest.webmvc.multipart;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.View;

public class MultipartView implements View {

	protected Log logger = LogFactory.getLog(getClass());

	private Multipart multipart;
	private boolean isDownload;

	public MultipartView(Multipart multipart, boolean isDownload) {
		this.multipart = multipart;
		this.isDownload = isDownload;
	}

	@Override
	public String getContentType() {
		return multipart.getContentType();
	}

	@Override
	public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		InputStream in =  null;
		OutputStream out =  null;
		try {
			String filename = getFilename(request, multipart.getFilename());
			
			if (isDownload) {
				response.setContentType("application/octet-stream;charset=UTF-8");
				response.setHeader("Content-Disposition", "attachment; filename=" + filename);
				response.setHeader("Content-Transfer-Encoding", "binary");
			} else {
				//response.setContentType(getContentType());
				response.setHeader("Content-Disposition", "inline; filename="+ filename);
			}
			response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
			response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
			response.setHeader("Expires", "0"); // Proxies.

			response.setContentLength(multipart.getSize().intValue());

			in = getInputStream(request, multipart.getSource());
			out = response.getOutputStream();

			StreamUtils.copy(in, out);

		} catch (Exception e) {
			logger.debug("", e);
			response.sendError(HttpStatus.NOT_FOUND.value(), e.getMessage());
		} finally {
			try {
				if(in != null) in.close();
			}catch(Exception e) {
			}
			try {
				if(out != null) out.close();
			}catch(Exception e) {
			}
		}
	}

	private InputStream getInputStream(HttpServletRequest request, Object source) throws Exception {
		if(ObjectUtils.isEmpty(source)) {
			throw new Exception("source is not found.");
		}else if(ClassUtils.isAssignableValue(Path.class, source)) {
			return Files.newInputStream((Path)source);
		}else if(ClassUtils.isAssignableValue(Resource.class, source)) {
			return ((Resource)source).getInputStream();
		}else if(ClassUtils.isAssignableValue(InputStream.class, source)) {
			return (InputStream)source;
		}else {
			throw new Exception();
		}
	}
	
	
	@SuppressWarnings("deprecation")
	private String getFilename(HttpServletRequest request, String filename) throws Exception {
		String header = getBrowser(request);
		if (header.contains("MSIE")) {
			return URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20")+";";
        
        }else if (header.contains("Trident")) {
            return URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20")+";";
        
        }else if (header.contains("Chrome")) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < filename.length(); i++) {
                char c = filename.charAt(i);
                sb.append((c > '~') ? URLEncoder.encode("" + c, "UTF-8") : c);
            }
            return sb.toString();
            
		} else if (header.contains("Opera")) {
            return "\""+new String(filename.getBytes("UTF-8"), "ISO-8859-1")+"\"";
		} else if (header.contains("Safari")) {
			return URLDecoder.decode("\""+new String(filename.getBytes("UTF-8"), "ISO-8859-1")+"\"");
		} else if (header.contains("Firefox")) {
            return URLDecoder.decode("\""+new String(filename.getBytes("UTF-8"), "8859_1") + "\"");
		} else{
            return filename;
        }
		//throw new RuntimeException("");
	}

	private String getBrowser(HttpServletRequest request) throws Exception {

        String header = request.getHeader("User-Agent");
        if(StringUtils.isEmpty(header)){
            return "Firefox";
        }

        if (header.indexOf("MSIE") > -1) {
            return "MSIE";
        } else if (header.indexOf("Trident") > -1) {   // IE11 
            return "Trident";
        } else if (header.indexOf("Chrome") > -1) {
            return "Chrome";
        } else if (header.indexOf("Opera") > -1) {
            return "Opera";
        } else if (header.indexOf("Safari") > -1) {
            return "Safari";
        }
        return "Firefox";

	}
}