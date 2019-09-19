package org.springframework.data.rest.webmvc.multipart;

import java.io.InputStream;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface MultipartRepository {

	public Optional<Multipart> create(String path, MultipartFile src)  ;
	public Optional<Multipart> create(String path, String directory) ;

	public Optional<Multipart> read(String path) ;
	
	public Optional<Multipart> update(String path, InputStream src) ;
	public Optional<Multipart> update(String path, String name) ;
	public Optional<Multipart> delete(String path) ;
	
	public Page<Multipart> history(String path, Pageable pageable) ;
	public Page<Multipart> childs(String path, Pageable pageable) ;
	public Page<Multipart> search(String path, Pageable pageable) ;
}