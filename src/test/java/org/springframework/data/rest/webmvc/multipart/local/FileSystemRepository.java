package org.springframework.data.rest.webmvc.multipart.local;

import static org.slieb.throwables.FunctionWithThrowable.castFunctionWithThrowable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.rest.webmvc.multipart.Multipart;
import org.springframework.data.rest.webmvc.multipart.MultipartService;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

public abstract class FileSystemRepository implements MultipartService, InitializingBean{

	protected Log logger = LogFactory.getLog(getClass());
	
	private Path root;
	
	protected abstract String getLocation();

	@Override
	public void afterPropertiesSet() throws Exception {
		this.root = Paths.get(getLocation());
		logger.info("ROOT: " + getLocation());
		if (!Files.exists(this.root)) {
			Files.createDirectories(this.root);
			logger.info("ROOT: created");
		}
	}

	
	@Override
	public Optional<Multipart> upload(String path, MultipartFile file){
		
		try {
			Path parent = root.resolve(path);
			if(! Files.isDirectory(parent)) 
				throw new Exception(path+" is not found.");
			
			Path target = parent.resolve(UUID.randomUUID().toString());
			Files.copy(file.getInputStream(), target);
			FileSystemUtils.setAttribute(target, "filename", file.getOriginalFilename());
			FileSystemUtils.setAttribute(target, "contentType", file.getContentType());
			FileSystemUtils.setAttribute(target, "version", "false");
			

			logger.info("File has been successfully transferred to: "+target);
			
			return Optional.of(multipart(target));
		}catch(Exception e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}
	
	@Override
	public Optional<Multipart> mkdir(String path, MultipartFile directory) {
		try {
			Path parent = root.resolve(path);
			if(! Files.isDirectory(parent)) 
				throw new Exception(path+" is not found.");
	
			Path target = parent.resolve(UUID.randomUUID().toString());
			Files.createDirectories(target);
			FileSystemUtils.setAttribute(target, "filename", directory.getOriginalFilename());
			FileSystemUtils.setAttribute(target, "contentType", directory.getContentType());
			FileSystemUtils.setAttribute(target, "version", "false");
			
			return Optional.of(multipart(target));
		}catch(Exception e) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<Multipart> attrs(String path)  {
		try {
			Path target = root.resolve(path);
			if(! Files.exists(target) || StringUtils.isEmpty(path)) 
				throw new Exception(path+" is not found.");
			
			return Optional.of(multipart(target));
		}catch(Exception e) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<Multipart> download(String path) {
		return attrs(path);
	}
	
	
	@Override
	public Optional<Multipart> rename(String path, String filename) {
		try {
			Path target = root.resolve(path);
			if(! Files.exists(target))
				throw new Exception(path+" is not found.");
			FileSystemUtils.setAttribute(target, "filename", filename);
			return Optional.of(multipart(target));
		}catch(Exception e) {
			return Optional.empty();
		}
	}
	
	

	@Override
	public Optional<Multipart> version(String path, InputStream src) {
		try {
			Path target = root.resolve(path);
			if(! Files.isRegularFile(target) || StringUtils.isEmpty(path)) 
				throw new Exception(path+" is not found.");
	
			//////////////////////////////////////////
			String targetId = target.getParent().relativize(target).toString();
			String targetFilename = FileSystemUtils.getAttribute(target, "filename");
			String targetContentType = FileSystemUtils.getAttribute(target, "contentType");
			
			long count = Files.list(target.getParent()).filter((p) -> {
				return StringUtils.startsWithIgnoreCase(p.getFileName().toString(), targetId);
			}).count();
			String nextId = targetId+"-"+count;
			Path history = target.getParent().resolve(nextId);
			
			Files.copy(target, history);
			FileSystemUtils.setAttribute(history, "filename", targetFilename);
			FileSystemUtils.setAttribute(history, "contentType", targetContentType);
			FileSystemUtils.setAttribute(history, "version", "true");
			
			//////////////////////////////////////////
			Files.copy(src, target, StandardCopyOption.REPLACE_EXISTING);
			FileSystemUtils.setAttribute(target, "filename", targetFilename);
			FileSystemUtils.setAttribute(target, "contentType", targetContentType);
			FileSystemUtils.setAttribute(target, "version", "false");
			
			return Optional.of(multipart(target));
		}catch(Exception e) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<Multipart> delete(String path) {
		try {
			Path orgin = root.resolve(path);
			if(! Files.exists(orgin)) 
                throw new Exception(path+" is not found.");
                
			org.springframework.util.FileSystemUtils.deleteRecursively(orgin);

			return Optional.empty();
		}catch(Exception e) {
			return Optional.empty();
		}
	}

	

	@Override
	public Page<Multipart> versions(String path, Pageable pageable)  {

		try {
			Path orgin = root.resolve(path);
			if(! Files.isRegularFile(orgin) || StringUtils.isEmpty(path)) 
				throw new Exception(path+" is not found.");
			
			String orginId = orgin.getParent().relativize(orgin).toString();
			Stream<Path> paths = listWithAttribute(orgin.getParent(), "xxxxx", orginId);
			return multipart(paths, pageable, true);
		}catch(Exception e) {
			return new PageImpl<Multipart>(new ArrayList<Multipart>());
		}
	}

	
	@Override
	public Page<Multipart> search(String name, Pageable pageable, MultiValueMap<String,Object> params) {
		try {
			Stream<Path> paths = walkFileTreeWithAttribute(root, "filename", name);
			return multipart(paths, pageable, false);
		}catch(Exception e) {
			return new PageImpl<Multipart>(new ArrayList<Multipart>());
		}
	}

	

	@Override
	public Page<Multipart> childs(String path, Pageable pageable) {
		try {
			Path orgin = root.resolve(path);
			if(! Files.isDirectory(orgin)) {
				throw new Exception(path+" is not found.");
			}
			
            Stream<Path> paths = Files.list(orgin);
            return multipart(paths, pageable, false);
		}catch(Exception e) {
            //e.printStackTrace();
			return new PageImpl<Multipart>(new ArrayList<Multipart>());
		}
	}
	
	
	
	private Multipart multipart(Path path) throws Exception{
		Multipart m = new Multipart();
		m.setId(root.toUri().relativize(path.toUri()));
		m.setFilename(FileSystemUtils.getAttribute(path, "filename"));
		m.setContentType(FileSystemUtils.getAttribute(path, "contentType"));
		m.setVersion(Boolean.parseBoolean(FileSystemUtils.getAttribute(path, "version")));
		m.setSize(Files.size(path));
		m.setLastModified(Files.getLastModifiedTime(path).toMillis());
		m.setSource(path);
		return m;
	}

	private Page<Multipart> multipart(Stream<Path> path, Pageable pageable, boolean history) throws Exception{
		
		PageableFunctional func = new PageableFunctional(pageable, history);
		
        List<Multipart> list = path
                .filter((p)->{return Files.exists(p);})
                .map(castFunctionWithThrowable((p)->{return multipart(p);}))
				.sorted(func.getComparator())
				.filter(func.getPredicate())
				.collect(Collectors.toList());
		
		return new PageImpl<>(list, pageable, func.getTotal());
	}
	
	
	public Stream<Path> listWithAttribute(Path parent, String key, String value) throws IOException {
		return Files.list(parent).filter((p) -> {
			
			String name = null;
			try {
				name = FileSystemUtils.getAttribute(p, key).toUpperCase();
			}catch(Exception e) {
				name = p.getFileName().toString().toUpperCase();
			}
			return name.contains(value.toUpperCase());
		});
	}

	public Stream<Path> walkFileTreeWithAttribute(Path parent, String key, String value) throws IOException {
		
		Builder<Path> builder = Stream.builder();
		Files.walkFileTree(parent, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path p, BasicFileAttributes attrs) throws IOException {
				
				String name = null;
				try {
					name = FileSystemUtils.getAttribute(p, key).toUpperCase();
				}catch(Exception e) {
					name = p.getFileName().toString().toUpperCase();
				}
				
				if(name.contains(value.toUpperCase())) 
					builder.add(p);
				return FileVisitResult.CONTINUE;
			}
			@Override
			public FileVisitResult postVisitDirectory(Path p, IOException exc) throws IOException {
				
				String name = null;
				try {
					name = FileSystemUtils.getAttribute(p, key).toUpperCase();
				}catch(Exception e) {
					name = p.getFileName().toString().toUpperCase();
				}
				
				if(name.contains(value.toUpperCase())) 
					builder.add(p);
				return FileVisitResult.CONTINUE;
			}
		});
		return builder.build();
	}
	
	private class PageableFunctional {

		private Predicate<Multipart> predicate;
		private Comparator<Multipart> comparator;
		private long count = 0;
		
		private long getTotal() {
			return count;
		}
		private Predicate<Multipart> getPredicate() {
			return predicate;
		}
		private Comparator<Multipart> getComparator() {
			return comparator;
		}

		private PageableFunctional(Pageable pageable, boolean history) {
			
			this.comparator = new Comparator<Multipart>() {
				@Override
				public int compare(Multipart o1, Multipart o2) {
					return 0;
				}
			};
			
			for(Order o : pageable.getSort()) {
				if("filename".equals(o.getProperty())){
					
					Comparator<Multipart> c = new Comparator<Multipart>() {
						public int compare(Multipart o1, Multipart o2) {
							return o.isAscending() ? o1.getFilename().compareTo(o2.getFilename()) : o2.getFilename().compareTo(o1.getFilename());
						}
					};
					this.comparator = this.comparator.thenComparing(c);
				
				}else if("size".equals(o.getProperty())){
					Comparator<Multipart> c = new Comparator<Multipart>() {
						public int compare(Multipart o1, Multipart o2) {
							return o.isAscending() ? o1.getSize().compareTo(o2.getSize()) : o2.getSize().compareTo(o1.getSize());
						}
					};
					this.comparator = this.comparator.thenComparing(c);
				
				}else if("lastModified".equals(o.getProperty())){
					Comparator<Multipart> c = new Comparator<Multipart>() {
						public int compare(Multipart o1, Multipart o2) {
							return o.isAscending() ? o1.getLastModified().compareTo(o2.getLastModified()) : o2.getLastModified().compareTo(o1.getLastModified());
						}
					};
					this.comparator = this.comparator.thenComparing(c);
				
				}else if("contentType".equals(o.getProperty())){
					Comparator<Multipart> c = new Comparator<Multipart>() {
						public int compare(Multipart o1, Multipart o2) {
							return o.isAscending() ? o1.getContentType().compareTo(o2.getContentType()) : o2.getContentType().compareTo(o1.getContentType());
						}
					};
					this.comparator = this.comparator.thenComparing(c);
				}
			}
			///////////////////////////////////////////////
			//
			//////////////////////////////////////////////
			this.predicate = new Predicate<Multipart>() {
				@Override
				public boolean test(Multipart t) {
					if(! history && t.isVersion()) {
						return false;
					}
					count++;
					//logger.info(count+" "+(pageable.getOffset() < count && count <= pageable.getOffset() + pageable.getPageSize()));
					return pageable.getOffset() < count && count <= pageable.getOffset() + pageable.getPageSize();
				}
			};
		}
	}

	@Override
	public Resource<Multipart> toResource(Multipart entity, String httpUrl) {
		String self = UriComponentsBuilder.fromHttpUrl(httpUrl).path("/").path(entity.getId().toString()).build().toUriString();
		return new Resource<Multipart>(entity, new Link(self));
	}

}