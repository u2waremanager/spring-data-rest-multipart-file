package org.springframework.data.rest.webmvc.multipart.local;


import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.rest.webmvc.multipart.Multipart;
import org.springframework.data.rest.webmvc.multipart.UploadDirectory;
import org.springframework.data.rest.webmvc.multipart.MultipartService;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

public class FileSystemService implements MultipartService, InitializingBean, DisposableBean{

	protected Log log = LogFactory.getLog(getClass());

	private Path root = Paths.get("target/storage");
	
	public void setLocation(String location) {
		this.root = Paths.get(location);
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if (!Files.exists(root)) {
			Files.createDirectories(root);
		}
		log.info("Location: "+root);
	}
	@Override
	public void destroy() throws Exception {
		
	}
	
	private void store(Path file, Multipart m) throws Exception{
		FileSystemUtils.setAttribute(file, "filename", m.getFilename());
		FileSystemUtils.setAttribute(file, "contentType", m.getContentType());
		FileSystemUtils.setAttribute(file, "version", "false");
	}

	private Multipart load(Path file) throws Exception{
		Multipart m = new Multipart();
		try {
			m.setFilename(FileSystemUtils.getAttribute(file, "filename"));
			m.setContentType(FileSystemUtils.getAttribute(file, "contentType"));
			m.setVersion(Boolean.parseBoolean(FileSystemUtils.getAttribute(file, "version")));
		}catch(Exception e) {
			m.setFilename(StringUtils.getFilename(file.getFileName().toString()));
			m.setContentType(Files.isDirectory(file)? UploadDirectory.TEXT_DIRECTORY_VALUE : MediaType.APPLICATION_OCTET_STREAM_VALUE);
			m.setVersion(false);
		}
		m.setSize(Files.size(file));
		m.setLastModified(Files.getLastModifiedTime(file).toMillis());
		return m;
	}
	
	public Optional<Multipart> upload(String path, MultipartFile file){
	
		try {
			Path parent = root.resolve(path);
			if(! Files.exists(parent)) {
				throw new Exception(path+" is not found.");
			}

			String filename = file.getOriginalFilename();
			Path target = parent.resolve(filename);
			Files.copy(file.getInputStream(), target);
			
			////////////////////////////////////////////////
			Multipart m = new Multipart();
			m.setId(root.toUri().relativize(target.toUri()));
			m.setFilename(file.getResource().getFilename());
			m.setContentType(file.getContentType());
			m.setSize(file.getSize());
			m.setLastModified(System.currentTimeMillis());
			store(target, m);

			log.info("File has been successfully upload to: "+target.toUri());
			return Optional.of(m);
			
		}catch(Exception e) {
			return Optional.empty();
		}
	}

	public Optional<Multipart> mkdir(String path, MultipartFile directory) {
		try {
			Path parent = root.resolve(path);
			if(! Files.exists(parent) ) {
				throw new Exception(path+" is not found.");
			}
			
			////////////////////////////////////////////////
			String filename = directory.getOriginalFilename();
			Path target = parent.resolve(filename);
			if(! Files.exists(target)) {
				log.info("File has been successfully mkdir to: "+target.toUri());
				Files.createDirectories(target);
			}

			////////////////////////////////////////////////
			Multipart m = new Multipart();
			m.setId(root.toUri().relativize(target.toUri()));
			m.setFilename(directory.getResource().getFilename());
			m.setContentType(directory.getContentType());
			m.setSize(directory.getSize());
			m.setLastModified(System.currentTimeMillis());
			store(target, m);
			
			return Optional.of(m);
			
		}catch(Exception e) {
			return Optional.empty();
		}
	}
	
	@Override
	public Optional<Multipart> attrs(String path)  {
		try {
			Path target = root.resolve(path);
			if(! Files.exists(target)) {
				throw new Exception(path+" is not found.");
			}
			
			////////////////////////////////////////////////
			Multipart m = load(target);
			m.setId(root.toUri().relativize(target.toUri()));
			m.setSource(target);
			
			return Optional.of(m);
		}catch(Exception e) {
			return Optional.empty();
		}
	}

	@Override
	public boolean exists(String path) {
		try {
			Path target = root.resolve(path);
			return Files.exists(target);
		}catch(Exception e) {
			throw new RuntimeException();
		}
	}

	@Override
	public Optional<Multipart> download(String path){
		return attrs(path);
	}
	
	@Override
	public Optional<Multipart> delete(String path)  {
		try {
			Path target = root.resolve(path);
			if(! Files.exists(target)) {
				throw new Exception(path+" is not found.");
			}
			
			org.springframework.util.FileSystemUtils.deleteRecursively(target);
			
            Multipart m = new Multipart();
			return Optional.of(m);
			
		}catch(Exception e) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<Multipart> rename(String path, String name) {
		throw new RuntimeException("NotImplementedException");
	}
	@Override
	public Optional<Multipart> version(String path, InputStream src) {
		throw new RuntimeException("NotImplementedException");
	}

	@Override
	public Page<Multipart> versions(String path, Pageable pageable) {
		throw new RuntimeException("NotImplementedException");
	}

	@Override
	public Page<Multipart> search(String path, Pageable pageable, MultiValueMap<String, Object> params) {
		throw new RuntimeException("NotImplementedException");
	}

	@Override
	public Page<Multipart> childs(String path, Pageable pageable) {
		try {
			Path target = root.resolve(path);
			if(! Files.exists(target)) {
				throw new ResourceNotFoundException(path+" is not found.");
			}
			
			int stx = pageable.getPageNumber() * pageable.getPageSize();
			int end = stx + pageable.getPageSize();
			AtomicInteger count = new AtomicInteger(0);
			
			List<Multipart> list = Files.list(target).filter((p)->{
				int idx = count.getAndAdd(1);
				return idx >= stx && idx < end;
				
			}).map((p)->{
				try {
					Multipart m = load(p);
		        	m.setId(root.toUri().relativize(p.toUri()));
					return m;
				}catch(Exception ex) {
					return null;
				}
			}).collect(Collectors.toList());
			
			return new PageImpl<>(list, pageable, count.get());
			
		}catch(ResourceNotFoundException e) {
			throw e;
		}catch(Exception e) {
			return new PageImpl<Multipart>(new ArrayList<Multipart>());
		}
	}

	
	@Override
	public Resource<Multipart> toResource(Multipart entity, String httpUrl) {
		String self = UriComponentsBuilder.fromHttpUrl(httpUrl).path("/").path(entity.getId().toString()).build().toUriString();
		return new Resource<Multipart>(entity, new Link(self));
	}
	
	
}
