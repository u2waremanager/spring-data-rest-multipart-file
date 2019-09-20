package org.springframework.data.rest.webmvc.multipart;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Multipart {

	private @JsonIgnore Object id;
	private @JsonIgnore Object source;
	private @JsonIgnore boolean version = false;
	
	private String contentType;
	private String filename;
	private Long size;
	private Long lastModified;
	
	public Object getId() {
		return id;
	}
	public void setId(Object id) {
		this.id = id;
	}
	public Object getSource() {
		return source;
	}
	public void setSource(Object source) {
		this.source = source;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public Long getSize() {
		return size;
	}
	public void setSize(Long size) {
		this.size = size;
	}
	public Long getLastModified() {
		return lastModified;
	}
	public void setLastModified(Long lastModified) {
		this.lastModified = lastModified;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public boolean isVersion() {
		return version;
	}
	public void setVersion(boolean version) {
		this.version = version;
	}
	@Override
	public String toString() {
		return "Multipart [id=" + id + ", filename=" + filename + "]";
	}
}