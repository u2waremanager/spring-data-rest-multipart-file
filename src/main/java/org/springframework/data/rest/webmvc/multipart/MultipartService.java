package org.springframework.data.rest.webmvc.multipart;

import java.io.InputStream;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

public interface MultipartService {
	
	public Optional<Multipart> upload(String path, MultipartFile file);
	public Optional<Multipart> mkdir(String path,  MultipartFile directory) ;
	
	public Optional<Multipart> attrs(String path);    
	public Optional<Multipart> download(String path);
	public Optional<Multipart> delete(String path);
	public boolean exists(String path); 
	
	public Optional<Multipart> rename(String path, String name); 
	public Optional<Multipart> version(String path, InputStream src);
	public Page<Multipart> versions(String path, Pageable pageable);
	public Page<Multipart> search(String path, Pageable pageable, MultiValueMap<String,Object> params);
	public Page<Multipart> childs(String path, Pageable pageable);
	
}
