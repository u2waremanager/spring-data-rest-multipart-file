package org.springframework.data.rest.webmvc.multipart;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public class UploadFile implements MultipartFile {

	private MultipartFile file;
	private String rename;

	public UploadFile(MultipartFile file) {
		this.file = file;
		this.rename = file.getOriginalFilename();
	}
	public UploadFile(MultipartFile file, Object rename) {
		this.file = file;
		this.rename = rename.toString();
	}
	
	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public String getOriginalFilename() {
		return rename;
	}

	@Override
	public String getContentType() {
		return file.getContentType();
	}

	@Override
	public boolean isEmpty() {
		return file.isEmpty();
	} 

	@Override
	public long getSize() {
		return file.getSize();
	}

	@Override
	public byte[] getBytes() throws IOException {
		return file.getBytes();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return file.getInputStream();
	}

	@Override
	public Resource getResource() {
		return file.getResource();
	}
	
	@Override
	public void transferTo(File dest) throws IOException, IllegalStateException {
		file.transferTo(dest);
	}
}
