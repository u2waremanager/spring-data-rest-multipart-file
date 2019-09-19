package org.springframework.data.rest.webmvc.multipart.local;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.text.Normalizer.Form;

import org.springframework.util.ClassUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.Platform;
import com.sun.jna.platform.mac.XAttrUtil;

public class FileSystemUtils {

	//private static Log logger = LogFactory.getLog(FileSystemUtils.class);
	
	private FileSystemUtils() {}
	
	private static ObjectMapper objectMapper = new ObjectMapper();

	public static <T> void setAttribute(Path path, T value) throws IOException{
		String attrName = "user:"+ClassUtils.getShortName(value.getClass());

		if(Platform.isMac()){
			String xpath = Normalizer.normalize(path.toFile().getAbsolutePath(), Form.NFD);
			XAttrUtil.setXAttr(xpath, attrName, objectMapper.writeValueAsString(value));
		}else{
			Files.setAttribute(path, attrName, objectMapper.writeValueAsBytes(value));
		}
	}

	public static <T> T getAttribute(Path path, Class<T> type) throws IOException{
		String attrName = "user:"+ClassUtils.getShortName(type);

		if(Platform.isMac()){
			String xpath = Normalizer.normalize(path.toFile().getAbsolutePath(), Form.NFD);
			String content = XAttrUtil.getXAttr(xpath, attrName);
			return objectMapper.readValue(content, type);
		}else{
			byte[] content = (byte[])Files.getAttribute(path, attrName);
			return  objectMapper.readValue(content, type);
		}
	}
	
	
	public static void setAttribute(Path path, String name, String value) throws IOException{
		String attrName = "user:"+name;

		if(Platform.isMac()){
			String xpath = Normalizer.normalize(path.toFile().getAbsolutePath(), Form.NFD);
			XAttrUtil.setXAttr(xpath, attrName, value);
		}else{
			Files.setAttribute(path, attrName, value.getBytes());
		}
	}

	public static String getAttribute(Path path, String name) throws IOException {
		String attrName = "user:"+name;

		if(Platform.isMac()){
			String xpath = Normalizer.normalize(path.toFile().getAbsolutePath(), Form.NFD);
			String content = XAttrUtil.getXAttr(xpath, attrName);
			return content;
		}else{
			byte[] content = (byte[])Files.getAttribute(path, attrName);
			return new String(content);
		}
	}
	
}
