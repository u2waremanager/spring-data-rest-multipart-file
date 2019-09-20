package org.springframework.data.rest.webmvc.multipart;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;

import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;

public class MultipartControllerDocs {

	public RestDocumentationResultHandler upload() {
		return document("upload", (builder)->{
			
		});
	}
	public RestDocumentationResultHandler mkdir() {
		return document("mkdir", (builder)->{
			
		});
	}
	public RestDocumentationResultHandler attrs() {
		return document("attrs", (builder)->{
			
		});
	}
	public RestDocumentationResultHandler download() {
		return document("download", (builder)->{
			
		});
	}
	public RestDocumentationResultHandler delete() {
		return document("delete", (builder)->{
			
		});
	}
}
