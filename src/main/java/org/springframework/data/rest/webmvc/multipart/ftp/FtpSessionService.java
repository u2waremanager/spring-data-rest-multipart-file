package org.springframework.data.rest.webmvc.multipart.ftp;

import static org.springframework.web.util.UriComponentsBuilder.fromPath;
import static org.springframework.web.util.UriComponentsBuilder.fromUri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.FileSystemResource;
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

import com.fasterxml.jackson.databind.ObjectMapper;

public class FtpSessionService extends FtpSessionProvider implements MultipartService , InitializingBean, DisposableBean{

	protected Log log = LogFactory.getLog(getClass());
	
	protected ObjectMapper objectMapper = new ObjectMapper();
	protected URI root = fromPath("/").build().toUri();;
	protected Map<String, Map<String,Object>> metadata;
	protected boolean useMetadata = true;
	
	public void setLocation(String location) {
		this.root = fromPath("/").path(location).path("/").build().toUri();;
	}
	public void setUseMetadata(boolean useMetadata) {
		this.useMetadata = useMetadata;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void afterPropertiesSet() throws Exception {
		
		doWithFtpSession(session->{
			if(! session.exists(root.toString())) {
				List<String> pathSegments = fromUri(root).build().getPathSegments();
				UriComponentsBuilder builder = fromPath("/");
				for(String pathSegment : pathSegments) {
					builder.pathSegment(pathSegment);
					if(! session.exists(builder.toUriString()) ) {
						session.mkdir(builder.toUriString());
					}
				}
			}
			return null;
		});
		log.info("Location: "+root);

		if(! useMetadata) return;
		File f = new File(System.getProperty("user.home"), "metadata.json");
		try {
			metadata = objectMapper.readValue(f, Map.class);
		}catch(Exception e) {
			e.printStackTrace();
			metadata = new HashMap<>();
		}
		log.info("afterPropertiesSet "+f.getAbsolutePath());
	}
	
	@Override
	public void destroy() throws Exception {
		if(! useMetadata) return;
		File f = new File(System.getProperty("user.home"), "metadata.json");
		objectMapper.writeValue(f, metadata);
		log.info("destroy "+f.getAbsolutePath());
	}

	@SuppressWarnings("unchecked")
	private void store(URI key, Multipart value) throws Exception{
		if(metadata == null) return;
		metadata.put(key.toString(), objectMapper.convertValue(value, Map.class));
		log.info("metadata store: "+key);
	}
	
	private Multipart load(URI key, FTPFile file) {
		if(metadata != null && metadata.containsKey(key.toString())) {
			log.info("metadata load: "+key);
			return objectMapper.convertValue(metadata.get(key.toString()), Multipart.class);
		} else {
			Multipart m = new Multipart();
			m.setFilename(StringUtils.getFilename(file.getName()));
			m.setContentType(file.isDirectory() ? UploadDirectory.TEXT_DIRECTORY_VALUE : MediaType.APPLICATION_OCTET_STREAM_VALUE);
			m.setSize(file.getSize());
			m.setLastModified(file.getTimestamp().getTimeInMillis());
			return m;
		}
	}
	
	
	@Override
	public Optional<Multipart> upload(String path, MultipartFile file){

		try {
			return doWithFtpSession(session->{

				URI parent = fromUri(root).path(path).build().toUri();
				if(! session.exists(parent.toString())) {
					throw new Exception(path+" is not found.");
				}
				
				////////////////////////////////////////////////
				String filename = file.getOriginalFilename();
				URI target  = fromUri(parent).path("/").path(filename).build().toUri();
				session.write(file.getInputStream(), target.toString());

				////////////////////////////////////////////////
				Multipart m = new Multipart();
				m.setId(root.relativize(target));
				m.setFilename(file.getResource().getFilename());
				m.setContentType(file.getContentType());
				m.setVersion(false);
				m.setSize(file.getSize());
				m.setLastModified(System.currentTimeMillis());
				store(target, m);
				
				log.info("File has been successfully upload to: "+target);
				return Optional.of(m);
			});			
		}catch(Exception e) {
			return Optional.empty();
		}
	}

	public Optional<Multipart> mkdir(String path, MultipartFile directory) {
		try {
			return doWithFtpSession(session->{
				
				URI parent = fromUri(root).path(path).build().toUri();
				if(! session.exists(parent.toString())) {
					throw new Exception(path+" is not found.");
				}

				///////////////////////////////////////////////
				String filename = directory.getOriginalFilename();
				URI target  = fromUri(parent).path("/").path(filename).build().toUri();
				
				List<String> pathSegments = fromUri(target).build().getPathSegments();
				UriComponentsBuilder builder = fromPath("/");
				for(String pathSegment : pathSegments) {
					builder.pathSegment(pathSegment);
					if(! session.exists(builder.toUriString()) ) {
						log.info("File has been successfully mkdir to: "+builder.toUriString());
						session.mkdir(builder.toUriString());
					}
				}
				
				///////////////////////////////////////////////
				Multipart m = new Multipart();
				m.setId(root.relativize(target));
				m.setFilename(directory.getResource().getFilename());
				m.setContentType(directory.getContentType());
				m.setVersion(false);
				m.setSize(directory.getSize());
				m.setLastModified(System.currentTimeMillis());
				store(target, m);
				
				return Optional.of(m);
			});
			
		}catch(Exception e) {
			return Optional.empty();
		}
	}	

	@Override
	public Optional<Multipart> attrs(String path)  {
		try {
			return doWithFtpSession(session->{
				
				URI target = fromUri(root).path(path).build().toUri();
				if(! session.exists(target.toString())) {
					throw new Exception(path+" is not found.");
				}

				////////////////////////////////////////////////
				FTPFile ftpFile = session.getClientInstance().mlistFile(target.toString());
				Multipart m = load(target, ftpFile);
				m.setId(root.relativize(target));
				
				return Optional.of(m);
			});
			
		}catch(Exception e) {
			return Optional.empty();
		}
	}
	
	@Override
	public boolean exists(String path) {
		try {
			return doWithFtpSession(session->{
				URI target = fromUri(root).path(path).build().toUri();
				return session.exists(target.toString());
			});
			
		}catch(Exception e) {
			throw new RuntimeException();
		}
	}

	
	@Override
	public Optional<Multipart> download(String path) {
		try {
			return doWithFtpSession(session->{
				
				URI target = fromUri(root).path(path).build().toUri();
				if(! session.exists(target.toString())) {
					throw new Exception(path+" is not found.");
				}

				FTPFile ftpFile = session.getClientInstance().mlistFile(target.toString());
				
				Multipart m = load(target, ftpFile);
				m.setId(root.relativize(target));

			    File ret = File.createTempFile("ftpsession_", "_tmp");
				session.read(target.toString(), new FileOutputStream(ret));
				m.setSource(new FileSystemResource(ret));
				
				return Optional.of(m);
			});
			
		}catch(Exception e) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<Multipart> delete(String path) {
		try {
			return doWithFtpSession(session->{
				
				URI target = fromUri(root).path(path).build().toUri();
				if(! session.exists(target.toString())) {
					throw new Exception(path+" is not found.");
				}
				session.remove(target.toString());
				metadata.remove(target.toString());
				
				Multipart m = new Multipart();
				return Optional.of(m);
			});
			
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
			return doWithFtpSession(session->{
				
				URI target = fromUri(root).path(path).build().toUri();
				if(! session.exists(target.toString())) {
					throw new ResourceNotFoundException(path+" is not found.");
				}
				
				int stx = pageable.getPageNumber() * pageable.getPageSize();
				int end = stx + pageable.getPageSize();
				AtomicInteger count = new AtomicInteger(0);

				
				List<Multipart> list = Arrays.stream(session.list(target.toString())).filter((p)->{
					int idx = count.getAndAdd(1);
					return idx >= stx && idx < end;
					
				}).map((ftpFile)->{

					URI file = fromUri(target).path("/").path(ftpFile.getName()).build().toUri();
					Multipart m = load(file, ftpFile);
					m.setId(root.relativize(file));
		        	return m;
		        	
				}).collect(Collectors.toList());
				
				return new PageImpl<>(list, pageable, count.get());
			});
			
		}catch(ResourceNotFoundException e) {
			throw e;
		}catch(Exception e) {
			return new PageImpl<Multipart>(new ArrayList<Multipart>());
		}
	}

	@Override
	public Resource<Multipart> toResource(Multipart entity, String httpUrl) {
		String self = UriComponentsBuilder.fromHttpUrl(httpUrl).path("/").path(""+entity.getId()).build().toUriString();
		return new Resource<Multipart>(entity, new Link(self));
	}
}
