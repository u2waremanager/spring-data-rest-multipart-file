package org.springframework.data.rest.webmvc.multipart;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.rest.webmvc.BaseUri;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.rest.webmvc.support.BaseUriLinkBuilder;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.HttpRequestMethodNotSupportedException;
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

@BasePathAwareController
public class MultipartController {

	//protected Log logger = LogFactory.getLog(getClass());
	
	private static final String MULTIPART = "/multipart";
	private static final String MULTIPART_FILE    = MULTIPART+"/**";	
	
	protected @Autowired MultipartRepository repository;
	
	protected @Autowired BaseUri baseUri;
	protected @Autowired PagedResourcesAssembler<Multipart> resourcesAssembler;
	protected ResourceAssembler<Multipart, Resource<Multipart>> resourceAssembler = new ResourceAssembler<Multipart, Resource<Multipart>>(){
		public Resource<Multipart> toResource(Multipart entity) {
			URI uri = baseUri.getUriComponentsBuilder().path(MULTIPART).build().toUri();
			Link link = BaseUriLinkBuilder.create(uri).slash(entity.getId()).withSelfRel();
			return new Resource<Multipart>(entity, link);
		}
	};
    
	//upload
	@RequestMapping(value=MULTIPART_FILE, method=RequestMethod.POST, consumes="multipart/form-data") 
	public @ResponseBody ResponseEntity<?> create(
			@RequestParam(value="file", required=false) MultipartFile file,
			@RequestParam(value="directory", required=false) String directory) throws ResourceNotFoundException, HttpRequestMethodNotSupportedException{
		
		String path = getCurrentRequestWildcardMappingValue();
        
		Optional<Multipart> multipart = (file != null) ? repository.create(path, file) : repository.create(path, directory);
		return toResource(multipart);
	}

	
	
	//update
	@RequestMapping(value=MULTIPART_FILE, method = RequestMethod.PUT)
	public @ResponseBody ResponseEntity<?> update(HttpServletRequest request) throws ResourceNotFoundException, HttpRequestMethodNotSupportedException, IOException {
		String path = getCurrentRequestWildcardMappingValue();

		Optional<Multipart> multipart = repository.update(path, request.getInputStream());
		return toResource(multipart);
	}

	//update - rename
	@RequestMapping(value=MULTIPART_FILE, method = RequestMethod.PATCH)
	public @ResponseBody ResponseEntity<?> update(@RequestBody Multipart request) throws ResourceNotFoundException, HttpRequestMethodNotSupportedException {
		String path = getCurrentRequestWildcardMappingValue();
		
		Optional<Multipart> multipart = repository.update(path, request.getFilename());
		return toResource(multipart);
	}
	
	//delete
	@RequestMapping(value=MULTIPART_FILE, method= RequestMethod.DELETE) 
	public @ResponseBody ResponseEntity<Object> delete() throws ResourceNotFoundException, HttpRequestMethodNotSupportedException {
		String path = getCurrentRequestWildcardMappingValue();
		repository.delete(path);
		return new ResponseEntity<Object>(HttpStatus.OK);
	}
	
	
	//read
	@RequestMapping(value=MULTIPART_FILE, method= RequestMethod.GET) 
	public @ResponseBody ResponseEntity<?> read() throws ResourceNotFoundException, HttpRequestMethodNotSupportedException {
		String path = getCurrentRequestWildcardMappingValue();
		
		Optional<Multipart> multipart = repository.read(path);
		return toResource(multipart);
	}

	@RequestMapping(value=MULTIPART_FILE, method= RequestMethod.GET, params = {"flag=preview"}) 
	public View preview() throws ResourceNotFoundException, HttpRequestMethodNotSupportedException {
		return download(false);
	}

	@RequestMapping(value=MULTIPART_FILE, method= RequestMethod.GET, params = {"flag=download"}) 
	public View download() throws ResourceNotFoundException, HttpRequestMethodNotSupportedException {
		return download(true);
    }
    
	private View download(Boolean download) throws ResourceNotFoundException, HttpRequestMethodNotSupportedException {
		String path = getCurrentRequestWildcardMappingValue();
		Optional<Multipart> content = repository.read(path);
		return new MultipartView(content.get(), download);
    }


	//search
	@RequestMapping(value=MULTIPART_FILE, method= RequestMethod.GET, params = {"flag=history"}) 
	public @ResponseBody ResponseEntity<?> history(
			Pageable pageable) throws ResourceNotFoundException, HttpRequestMethodNotSupportedException {
		
		String path = getCurrentRequestWildcardMappingValue();
		
		Page<Multipart> multiparts = repository.history(path, pageable);
		return toResources(multiparts);
	}
	
	@RequestMapping(value=MULTIPART_FILE, method= RequestMethod.GET, params = {"flag=childs"}) 
	public @ResponseBody ResponseEntity<?> childs(
			Pageable pageable) throws ResourceNotFoundException, HttpRequestMethodNotSupportedException {
		
		String path = getCurrentRequestWildcardMappingValue();
		Page<Multipart> multiparts = repository.childs(path, pageable);
		return toResources(multiparts);
	}



//	 @RequestMapping(value=MULTIPART, method= RequestMethod.GET, params = {"flag=search"}) 
//	 public @ResponseBody ResponseEntity<?> search(
//	 		@RequestParam(value="name", required = false) String name,
//	 		Pageable pageable) throws ResourceNotFoundException, HttpRequestMethodNotSupportedException {
//		
//	 	Page<Multipart> multiparts = repository.search(path, pageable);
//	 	return toResources(multiparts);
//	 }
	
	
//	private URI getCurrentRequestUri(){
//		return ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
//	}
//
//	private String getCurrentRequestUri(URI uri){
//		return getCurrentRequestUri().relativize(uri).toString(); 
//	}
	
	private String getCurrentRequestWildcardMappingValue(){
		ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = sra.getRequest();
		AntPathMatcher apm = new AntPathMatcher();
		String pattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		String wildcard = apm.extractPathWithinPattern(pattern, path);
		return wildcard;
	}	

	protected ResponseEntity<?> toResources(Page<Multipart> multiparts){
		Object content = null;
		if(multiparts.getContent().size() == 0) {
			content = resourcesAssembler.toEmptyResource(multiparts, Multipart.class);
		}else {
			content = resourcesAssembler.toResource(multiparts, resourceAssembler);
		}
		return new ResponseEntity<Object>(content, HttpStatus.OK);
	}
	
	protected ResponseEntity<?> toResource(Optional<Multipart> multipart){
		return multipart.map((it)->{
			Object content = resourceAssembler.toResource(it);
			return new ResponseEntity<Object>(content, HttpStatus.OK);
		}).orElseThrow(() -> new ResourceNotFoundException());
	}

}
