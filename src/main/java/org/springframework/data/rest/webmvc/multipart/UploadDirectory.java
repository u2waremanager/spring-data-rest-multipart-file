package org.springframework.data.rest.webmvc.multipart;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public class UploadDirectory extends AbstractResource implements MultipartFile {

	public final static String TEXT_DIRECTORY_VALUE = "text/directory";
	
	private String directory;
	private String rename;

	public UploadDirectory(String directory) {
		this.directory = directory;
		this.rename = directory;
	}
	public UploadDirectory(String directory, Object rename) {
		this.directory = directory;
		this.rename = rename.toString();
	}
	
	
	@Override
	public String getName() {
		throw new RuntimeException();
	}

	@Override
	public String getOriginalFilename() {
		return rename;
	}

	@Override
	public String getContentType() {
		return TEXT_DIRECTORY_VALUE;
	}

	@Override
	public boolean isEmpty() {
		return false;
	} 

	@Override
	public long getSize() {
		return 0;
	}

	@Override
	public byte[] getBytes() throws IOException {
		throw new RuntimeException();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		throw new RuntimeException();
	}

	@Override
	public Resource getResource() {
		return this;
	}
	@Override
	public void transferTo(File dest) throws IOException, IllegalStateException {
		throw new RuntimeException();
	}

	@Override
	public String getFilename() {
		return directory;
	}
	@Override
	public String getDescription() {
		throw new RuntimeException();
	}
}
