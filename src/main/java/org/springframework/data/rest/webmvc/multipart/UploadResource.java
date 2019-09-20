package org.springframework.data.rest.webmvc.multipart;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

public class UploadResource implements MultipartFile {

	private Resource resource;
	private String originalFilename;
	private String contentType;
	
	public UploadResource(Resource resource) {
		this(resource, MediaType.APPLICATION_OCTET_STREAM_VALUE);
	}
	public UploadResource(Resource resource, String contentType) {
		this(resource, contentType, resource.getFilename());
	}
	public UploadResource(Resource resource, String contentType, String originalFilename) {
		this.resource = resource;
		this.contentType = contentType;
		this.originalFilename = originalFilename;
	}
	
	@Override
	public String getName() {
		throw new RuntimeException();
	}

	@Override
	public String getOriginalFilename() {
		return originalFilename;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public boolean isEmpty() {
		return false;
	} 

	@Override
	public long getSize() {
		try {
			return resource.contentLength();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] getBytes() throws IOException {
		throw new RuntimeException();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return resource.getInputStream();
	}

	@Override
	public Resource getResource() {
		return resource;
	}
	
	@Override
	public void transferTo(File dest) throws IOException, IllegalStateException {
		throw new RuntimeException();
	}

}
