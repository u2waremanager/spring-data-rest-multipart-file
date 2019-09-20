package org.springframework.data.rest.webmvc.multipart;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.rest.webmvc.BaseUri;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.mvc.BasicLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.util.UriComponentsBuilder;

@BasePathAwareController
public class MultipartController {

	protected Log logger = LogFactory.getLog(getClass());
	
	public final String MULTIPART = "multipart";
	
	@RequestMapping("/multipart-ui/{beanName:[^\\\\\\\\.]*}")
	public String html5Forwarding(@PathVariable String beanName) {
		return "forward:/multipart-ui/index.html";
	}
	
	@RequestMapping("/multipart-docs")
	public String html5Forwarding() {
		return "forward:/multipart-docs/index.html";
	}
	
	//upload & mkdir
	@RequestMapping(value="/multipart/{beanName}/**", method=RequestMethod.POST, consumes="multipart/form-data") 
	public @ResponseBody ResponseEntity<?> create(
			@PathVariable String beanName,
			@RequestParam(value="file", required=false) MultipartFile file,
			@RequestParam(value="directory", required=false) String directory,
			@RequestParam(value="random", required=false, defaultValue = "true") Boolean random) throws ResourceNotFoundException, HttpRequestMethodNotSupportedException{
		
		MultipartService service = resolveMultipartService(beanName);
		ResourceAssembler<Multipart, Resource<Multipart>> resourceAssembler = resolveResourceAssembler(beanName, service);
		String path = getCurrentRequestWildcardMappingValue();
		
		Optional<Multipart> multipart = (file != null) 
				? service.upload(path, random ? new UploadFile(file, UUID.randomUUID().toString()) : file) 
				: service.mkdir(path,  random ? new UploadDirectory(directory, UUID.randomUUID().toString()) : new UploadDirectory(directory));
		
		return toResource(multipart, resourceAssembler);
	}

	//download
	@RequestMapping(value="/multipart/{beanName}/**", method= RequestMethod.GET, params = {"flag=preview"}) 
	public View preview(@PathVariable String beanName) throws ResourceNotFoundException, HttpRequestMethodNotSupportedException {
		return download(beanName, false);
	}

	@RequestMapping(value="/multipart/{beanName}/**", method= RequestMethod.GET, params = {"flag=download"}) 
	public View download(@PathVariable String beanName) throws ResourceNotFoundException, HttpRequestMethodNotSupportedException {
		return download(beanName, true);
    }
    
	private View download(String beanName, Boolean download) throws ResourceNotFoundException, HttpRequestMethodNotSupportedException {
		MultipartService service = resolveMultipartService(beanName);
		String path = getCurrentRequestWildcardMappingValue();
		
		Optional<Multipart> multipart = service.download(path);
		return multipart.map((content)->{
			return new MultipartView(content, download);
		}).orElseThrow(() -> new ResourceNotFoundException());
    }
	
	//delete
	@RequestMapping(value="/multipart/{beanName}/**", method= RequestMethod.DELETE) 
	public @ResponseBody ResponseEntity<Object> delete(
			@PathVariable String beanName) throws ResourceNotFoundException, HttpRequestMethodNotSupportedException {
		MultipartService service = resolveMultipartService(beanName);
		String path = getCurrentRequestWildcardMappingValue();
		
		service.delete(path);
		return new ResponseEntity<Object>(HttpStatus.OK);
	}
	
	//attrs
	@RequestMapping(value="/multipart/{beanName}/**", method= RequestMethod.GET) 
	public @ResponseBody ResponseEntity<?> attrs(@PathVariable String beanName) throws ResourceNotFoundException, HttpRequestMethodNotSupportedException {
		MultipartService service = resolveMultipartService(beanName);
		ResourceAssembler<Multipart, Resource<Multipart>> resourceAssembler = resolveResourceAssembler(beanName, service);
		String path = getCurrentRequestWildcardMappingValue();
		
		Optional<Multipart> multipart = service.attrs(path);
		return toResource(multipart, resourceAssembler);
	}

	//rename
	@RequestMapping(value="/multipart/{beanName}/**", method = RequestMethod.PATCH)
	public @ResponseBody ResponseEntity<?> rename(
			@PathVariable String beanName,
			@RequestBody Multipart request) throws ResourceNotFoundException, HttpRequestMethodNotSupportedException {
		MultipartService service = resolveMultipartService(beanName);
		ResourceAssembler<Multipart, Resource<Multipart>> resourceAssembler = resolveResourceAssembler(beanName, service);
		String path = getCurrentRequestWildcardMappingValue();
		
		Optional<Multipart> multipart = service.rename(path, request.getFilename());
		return toResource(multipart, resourceAssembler);
	}
	
	
	//version
	@RequestMapping(value="/multipart/{beanName}/**", method = RequestMethod.PUT)
	public @ResponseBody ResponseEntity<?> version(
			@PathVariable String beanName,
			HttpServletRequest request) throws ResourceNotFoundException, HttpRequestMethodNotSupportedException, IOException {
		MultipartService service = resolveMultipartService(beanName);
		ResourceAssembler<Multipart, Resource<Multipart>> resourceAssembler = resolveResourceAssembler(beanName, service);
		String path = getCurrentRequestWildcardMappingValue();

		Optional<Multipart> multipart = service.version(path, request.getInputStream());
		return toResource(multipart, resourceAssembler);
	}
	
	
	
	//versions
	@RequestMapping(value="/multipart/{beanName}/**", method= RequestMethod.GET, params = {"flag=version"}) 
	public @ResponseBody ResponseEntity<?> versions(
			@PathVariable String beanName,
			Pageable pageable) throws ResourceNotFoundException, HttpRequestMethodNotSupportedException {
		MultipartService service = resolveMultipartService(beanName);
		ResourceAssembler<Multipart, Resource<Multipart>> resourceAssembler = resolveResourceAssembler(beanName, service);
		String path = getCurrentRequestWildcardMappingValue();
		
		Page<Multipart> multiparts = service.versions(path, pageable);
		return toResources(multiparts, resourcesAssembler, resourceAssembler);
	}
	

	//search
	@RequestMapping(value="/multipart/{beanName}/**", method= RequestMethod.GET, params = {"flag=search"}) 
	public @ResponseBody ResponseEntity<?> search(
			@PathVariable String beanName,
			@RequestParam MultiValueMap<String,Object> params,
			Pageable pageable) throws ResourceNotFoundException, HttpRequestMethodNotSupportedException {
		MultipartService service = resolveMultipartService(beanName);
		ResourceAssembler<Multipart, Resource<Multipart>> resourceAssembler = resolveResourceAssembler(beanName, service);
		String path = getCurrentRequestWildcardMappingValue();
		
		Page<Multipart> multiparts = service.search(path, pageable, params);
		return toResources(multiparts, resourcesAssembler, resourceAssembler);
	}
	
	

	//childs
	@RequestMapping(value="/multipart/{beanName}/**", method= RequestMethod.GET, params = {"flag=childs"}) 
	public @ResponseBody ResponseEntity<?> childs(
			@PathVariable String beanName,
			Pageable pageable) throws ResourceNotFoundException, HttpRequestMethodNotSupportedException {
		MultipartService service = resolveMultipartService(beanName);
		ResourceAssembler<Multipart, Resource<Multipart>> resourceAssembler = resolveResourceAssembler(beanName, service);
		String path = getCurrentRequestWildcardMappingValue();
		
		Page<Multipart> multiparts = service.childs(path, pageable);
		return toResources(multiparts, resourcesAssembler, resourceAssembler);
	}


	protected @Autowired BaseUri baseUri;
	protected @Autowired ApplicationContext context;
	protected @Autowired PagedResourcesAssembler<Multipart> resourcesAssembler;
	
	
	protected MultipartService resolveMultipartService(String beanName) {
		try {
			return context.getBean(beanName, MultipartService.class);
		}catch(Exception e) {
			throw new ResourceNotFoundException(beanName);
		}
	}
	
	protected UriComponentsBuilder resolveBaseUri(String beanName) {
		return baseUri.getUriComponentsBuilder()
				.pathSegment("multipart")
				.pathSegment(beanName)
				.path("/");
	}

	
	protected ResourceAssembler<Multipart, Resource<Multipart>> resolveResourceAssembler(String beanName, MultipartService service) {
		return new ResourceAssembler<Multipart, Resource<Multipart>>() {
			public Resource<Multipart> toResource(Multipart entity) {
				
				if(entity == null) {
					logger.info(entity);
					logger.info(entity);
					logger.info(entity);
				}
				String self = resolveBaseUri(beanName).path(entity.getId().toString()).toUriString();	
				
				return new Resource<Multipart>(entity, new Link(self));
			}
		};
	}
	
	protected ResponseEntity<?> toResources(Page<Multipart> page, PagedResourcesAssembler<Multipart> resourcesAssembler, ResourceAssembler<Multipart, Resource<Multipart>> resourceAssembler){
		Object content = null;
		if(! page.iterator().hasNext()) {
			content = resourcesAssembler.toEmptyResource(page, Multipart.class);
		}else {
			content = resourcesAssembler.toResource(page, resourceAssembler);
		}
		return new ResponseEntity<Object>(content, HttpStatus.OK);
	}

	protected ResponseEntity<?> toResource(Optional<Multipart> multipart, ResourceAssembler<Multipart, Resource<Multipart>> resourceAssembler){
		return multipart.map((entity)->{
			return new ResponseEntity<Resource<Multipart>>(resourceAssembler.toResource(entity), HttpStatus.OK);
		}).orElseThrow(() -> new ResourceNotFoundException());
	}

	protected String getCurrentRequestWildcardMappingValue(){
		ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = sra.getRequest();
		AntPathMatcher apm = new AntPathMatcher();
		String pattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
		
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		String wildcard = apm.extractPathWithinPattern(pattern, path);
		
		return wildcard;
	}

	
//	private URI getCurrentRequestUri(){
//		return ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
//	}
//
//	private String getCurrentRequestUri(URI uri){
//		return getCurrentRequestUri().relativize(uri).toString(); 
//	}	
	
//	private static final String MULTIPART = "/multipart";
//	private static final String MULTIPART_FILE    = MULTIPART+"/**";	
	
//	protected @Autowired MultipartRepository repository;
//	
//	protected @Autowired BaseUri baseUri;
//	
//	protected ResourceAssembler<Multipart, Resource<Multipart>> resourceAssembler = new ResourceAssembler<Multipart, Resource<Multipart>>(){
//		public Resource<Multipart> toResource(Multipart entity) {
//			URI uri = baseUri.getUriComponentsBuilder().path(MULTIPART).build().toUri();
//			Link link = BaseUriLinkBuilder.create(uri).slash(entity.getId()).withSelfRel();
//			return new Resource<Multipart>(entity, link);
//		}
//	};
}
