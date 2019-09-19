package org.springframework.data.rest.webmvc.multipart.test;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

import java.util.ArrayList;
import java.util.List;

import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.headers.HeaderDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.restdocs.payload.SubsectionDescriptor;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.restdocs.request.RequestDocumentation;
import org.springframework.restdocs.request.RequestPartDescriptor;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;


public class MultipartDocs implements ResultHandler{

	
	//private String identifier;
	private RestDocumentationResultHandler handler;
	
	public MultipartDocs(String identifier) {
		//this.identifier = identifier;
		this.handler = document(identifier
					, preprocessRequest(prettyPrint())
					, preprocessResponse(prettyPrint())
					, HeaderDocumentation.requestHeaders(requestHeaders())
					, RequestDocumentation.requestParameters(requestParameters())
					, PayloadDocumentation.requestFields(requestFields())
					, HeaderDocumentation.responseHeaders(responseHeaders())
					, PayloadDocumentation.responseFields(responseFields())
				);
	}
	
	@Override
	public void handle(MvcResult result) throws Exception {
		handler.handle(result);
	}
	
	private final List<HeaderDescriptor> requestHeaders(){
		List<HeaderDescriptor> descriptors = new ArrayList<HeaderDescriptor>();
		requestHeaders(descriptors);
		return descriptors;
	}
	private final List<ParameterDescriptor> requestParameters(){
		List<ParameterDescriptor> descriptors =  new ArrayList<ParameterDescriptor>();
		requestParameters(descriptors);
		return descriptors;
	}
	
	private final List<FieldDescriptor> requestFields(){
		List<FieldDescriptor> descriptors =  new ArrayList<FieldDescriptor>();
		requestFields(descriptors);
		return descriptors;
	}
	
	private final List<HeaderDescriptor> responseHeaders(){
		List<HeaderDescriptor> descriptors =  new ArrayList<HeaderDescriptor>();
		responseHeaders(descriptors);
		return descriptors;
	}
	
	private final List<FieldDescriptor> responseFields(){
		List<FieldDescriptor> descriptors =  new ArrayList<FieldDescriptor>();
		responseFields(descriptors);
		return descriptors;
	}
	
	protected void requestHeaders(List<HeaderDescriptor> descriptors) {
	}
	protected void requestParameters(List<ParameterDescriptor> descriptors) {
	}
	protected void requestFields(List<FieldDescriptor> descriptors) {
	}
	protected void responseHeaders(List<HeaderDescriptor> descriptors) {
	}
	protected void responseFields(List<FieldDescriptor> descriptors) {
	}
	
	public final HeaderDescriptor headerWithName(String name) {
		return HeaderDocumentation.headerWithName(name);
	}
	public final SubsectionDescriptor subsectionWithPath(String path) {
		return PayloadDocumentation.subsectionWithPath(path);
	}
	public final FieldDescriptor fieldWithPath(String path) {
		return PayloadDocumentation.fieldWithPath(path);
	}
	public final ParameterDescriptor parameterWithName(String name) {
		return RequestDocumentation.parameterWithName(name);
	}
	public final RequestPartDescriptor partWithName(String name) {
		return RequestDocumentation.partWithName(name);
	}
	
	
	public RestDocumentationResultHandler docs(String identifier) {
		return document(identifier
				, preprocessRequest(prettyPrint())
				, preprocessResponse(prettyPrint())
				, HeaderDocumentation.requestHeaders(
					requestHeaders()
				)
				, RequestDocumentation.requestParameters(
					requestParameters()
				)
				, PayloadDocumentation.requestFields(
					requestFields()
				)
				, HeaderDocumentation.responseHeaders(
					responseHeaders()
				)
				, PayloadDocumentation.responseFields(
					responseFields()
				)
			);
	}
	


	private ResponseFieldsSnippet multipartResponseFields() {
		return PayloadDocumentation.responseFields(
				PayloadDocumentation.fieldWithPath("contentType").description("")
				,PayloadDocumentation.fieldWithPath("size").description("")
				,PayloadDocumentation.fieldWithPath("lastModified").description("")
				,PayloadDocumentation.fieldWithPath("filename").description("")
				,PayloadDocumentation.fieldWithPath("_links.self.href").description("")
			);
	}
	private ResponseFieldsSnippet multipartsResponseFields() {
		return PayloadDocumentation.responseFields(
				PayloadDocumentation.subsectionWithPath("_embedded").description("An array of file metadata resources")
				,PayloadDocumentation.subsectionWithPath("page").description("Pagination ")
				,PayloadDocumentation.subsectionWithPath("_links").ignored()
			);
	}
	
	
	

	
	public RestDocumentationResultHandler uploadFile(String identifier) {
		return document(identifier
				, preprocessRequest(prettyPrint())
				, preprocessResponse(prettyPrint())
				, RequestDocumentation.requestParts(
					RequestDocumentation.partWithName("file").description("Upload File")
				)
				, multipartResponseFields()
			);
	}
	public RestDocumentationResultHandler uploadDirectory(String identifier) {
		return document(identifier
				, preprocessRequest(prettyPrint())
				, preprocessResponse(prettyPrint())
				, RequestDocumentation.requestParameters(
						RequestDocumentation.parameterWithName("directory").description("directory name")
					)
				, multipartResponseFields()
			);
	}

	public RestDocumentationResultHandler read(String identifier, String flag) {
		return document(identifier
				, preprocessRequest(prettyPrint())
				, preprocessResponse(prettyPrint())
				, RequestDocumentation.requestParameters(
					RequestDocumentation.parameterWithName("flag").description(flag)
				)
			);
	}
	
	public RestDocumentationResultHandler readChilds(String identifier) {
		return document(identifier
				, preprocessRequest(prettyPrint())
				, preprocessResponse(prettyPrint())
				, RequestDocumentation.requestParameters(
					RequestDocumentation.parameterWithName("flag").description("childs"),
					RequestDocumentation.parameterWithName("page").description("the page number to access (0 indexed, defaults to 0).").optional(),
					RequestDocumentation.parameterWithName("size").description("the page size requested (defaults to 20).").optional(),
					RequestDocumentation.parameterWithName("sort").description("collection of sort directives in the format ($propertyname,)+[asc|desc]?.").optional()
				)
				, multipartsResponseFields()
			);
	}
	

	public RestDocumentationResultHandler remove(String identifier) {
		return document(identifier
				, preprocessRequest(prettyPrint())
				, preprocessResponse(prettyPrint())
			);
	}
		

	public RestDocumentationResultHandler updateRename(String identifier) {
		return document(identifier
				, preprocessRequest(prettyPrint())
				, preprocessResponse(prettyPrint())
				, PayloadDocumentation.requestFields(
						PayloadDocumentation.fieldWithPath("filename").description("")
				)
				, multipartResponseFields()
			);
	}
	
	
	public RestDocumentationResultHandler updateModify(String identifier) {

		return document(identifier
				, preprocessRequest(prettyPrint())
				, preprocessResponse(prettyPrint())
				, multipartResponseFields()
				);
		
	}
	public RestDocumentationResultHandler updateHistory(String identifier) {

		return document(identifier
				, preprocessRequest(prettyPrint())
				, preprocessResponse(prettyPrint())
				, RequestDocumentation.requestParameters(
						RequestDocumentation.parameterWithName("flag").description("history")
					)
				, multipartsResponseFields()
				);
		
	}
}
