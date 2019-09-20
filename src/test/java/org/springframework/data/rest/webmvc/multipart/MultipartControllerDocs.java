package org.springframework.data.rest.webmvc.multipart;


import static org.springframework.data.rest.webmvc.multipart.RestDocsBuilder.document;

import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;

public class MultipartControllerDocs {

	
	private void responseFields(RestDocsBuilder builder) {
		builder.responseFields().fieldWithPath("filename").description("filename");
		builder.responseFields().fieldWithPath("contentType").description("contentType");
		builder.responseFields().fieldWithPath("size").description("size");
		builder.responseFields().fieldWithPath("lastModified").description("lastModified");
		builder.responseFields().fieldWithPath("_links.self.href").description("download link");
	}
	
	
	public RestDocumentationResultHandler upload(int idx) {
		return document("upload-"+idx, (builder)->{
			builder.requestParts().partWithName("file").description("file");
		});
	}
	public RestDocumentationResultHandler mkdir(int idx) {
		return document("mkdir-"+idx, (builder)->{
			builder.requestParameters().parameterWithName("directory").description("directory");
			responseFields(builder);
		});
	}
	public RestDocumentationResultHandler attrs(int idx) {
		return document("attrs-"+idx, (builder)->{
			responseFields(builder);
		});
	}
	public RestDocumentationResultHandler download(int idx) {
		return document("download-"+idx, (builder)->{
			builder.requestParameters().parameterWithName("flag").description("download");
			
		});
	}
	public RestDocumentationResultHandler preview(int idx) {
		return document("preview-"+idx, (builder)->{
			builder.requestParameters().parameterWithName("flag").description("preview");
		});
	}

	public RestDocumentationResultHandler childs(int idx) {
		return document("childs-"+idx, (builder)->{
			builder.requestParameters().parameterWithName("flag").description("childs");
			
			builder.responseFields().subsectionWithPath("_embedded").description("Arrays of <<>>");
			builder.responseFields().subsectionWithPath("page").description("Arrays of <<>>");
			builder.responseFields().subsectionWithPath("_links").description("Arrays of <<>>");
		});
	}
	
	public RestDocumentationResultHandler delete(int idx) {
		return document("delete-"+idx, (builder)->{
			
		});
	}
}
