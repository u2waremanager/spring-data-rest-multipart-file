package org.springframework.data.rest.webmvc.multipart;

import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.web.bind.annotation.RequestMapping;

@BasePathAwareController
public class MultipartUIController {
    
    @RequestMapping("/multipart-ui")
	public String html5Forwarding() {
		return "forward:/multipart-ui/index.html";
	}

    @RequestMapping("/multipart-docs")
	public String html5ForwardingFor() {
		return "forward:/multipart-docs/index.html";
	}

}