package org.springframework.data.rest.webmvc.multipart;

import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;

public class MultipartControllerTests extends ApplicationTests{

	
	
	@Test
	public void contextLoads() throws Exception {
		
		//MultipartDocs docs = new MultipartDocs("multipart");
		
		String beanName = "default";
//		String beanName = "oops";
		////////////////////////////////////////
		//
		////////////////////////////////////////
		$.MULTIPART("/multipart").is4xx();

		$.MULTIPART("/multipart/abcd").is4xx();

		$.MULTIPART("/multipart/"+beanName).P("directory", "NewDirectory").is2xx("m1");//.and(docs.uploadDirectory("upload-step2"));
		$.MULTIPART("m1").P("directory", "ChildDirectory").is2xx("m2");//.and(docs.uploadDirectory("upload-step3"));
		
		$.GET("m1").is2xx();
		$.GET("m2").is2xx();
		$.GET("m1").P("flag", "download").is4xx();
		$.GET("m2").P("flag", "preview").is4xx();

//////		//rename
//////		Map<String,Object> m = new HashMap<String,Object>();
//////		m.put("filename", "ChangeDirectory");
//////		$.PATCH("m1").C(m).is2xx("m1");
//////		$.GET("m1").is2xx();

		////////////////////////////////////////
		//
		////////////////////////////////////////
		MockMultipartFile f1 = new MockMultipartFile("file", "MyFile.html", "text/plain", ("File Contents..........").getBytes());
		MockMultipartFile f2 = new MockMultipartFile("file", "hello2.html", "text/plain", ("C"+System.currentTimeMillis()+"D").getBytes());
		MockMultipartFile f3 = new MockMultipartFile("file", "hello3.json", "application/json", ("{time:"+System.currentTimeMillis()+"}").getBytes());
		MockMultipartFile f4 = new MockMultipartFile("file", "hello4.json", "application/json", ("{time:"+System.currentTimeMillis()+"}").getBytes());
		MockMultipartFile f5 = new MockMultipartFile("file", "hello5.json", "application/json", ("{time:"+System.currentTimeMillis()+"}").getBytes());

		//create
		$.MULTIPART("/multipart/"+beanName+"/a/b/c").F(f1).is4xx();
		$.MULTIPART("/multipart/"+beanName).F(f1).is2xx("m3");//.and(docs.uploadFile("upload-step1"));
		$.MULTIPART("m1").F(f2).is2xx("m4");
		$.MULTIPART("m1").F(f3).is2xx();
		$.MULTIPART("m2").F(f4).is2xx("m5");//.and(docs.uploadFile("upload-step4"));
		$.MULTIPART("m2").F(f5).is2xx();
		
		//read
		$.GET("m3").is2xx();
		$.GET("m4").is2xx();
		$.GET("m5").is2xx();

		//Download
		$.GET("m3").P("flag", "preview").is2xx();
		$.GET("m3").P("flag", "download").is2xx();
		$.GET("m4").P("flag", "preview").is2xx();
		$.GET("m4").P("flag", "download").is2xx();
		$.GET("m5").P("flag", "preview").is2xx();//.and(docs.read("read-step2", "preview"));
		$.GET("m5").P("flag", "download").is2xx();//.and(docs.read("read-step1", "download"));
		
//////		//versions
//////		Map<String,Object> f = new HashMap<String,Object>();
//////		f.put("filename", "Renamed.html");
//////		$.PATCH("m5").C(f).is2xx();//.and(docs.updateRename("update-step1"));
//////		$.GET("m5").is2xx();
//////
//////		$.PUT("m5").C("a", "Modify Contents..........").is2xx();//.and(docs.updateModify("update-step2"));
//////		$.GET("m5").is2xx();
//////		
//////		
//////		//version
//////		$.GET("/multipart").is4xx();
//////		$.GET("m1").P("flag", "history").is2xx();
//////		$.GET("m2").P("flag", "history").is2xx();
//////		$.GET("m3").P("flag", "history").is2xx();
//////		$.GET("m4").P("flag", "history").is2xx();
//////		$.GET("m5").P("flag", "history").is2xx();//.and(docs.updateHistory("update-step3"));;

		//childs
		$.GET("m1").P("flag", "childs").is2xx();
		$.GET("m2").P("flag", "childs").is2xx();
		$.GET("m3").P("flag", "childs").is2xx();
		$.GET("m4").P("flag", "childs").is2xx();
		$.GET("m5").P("flag", "childs").is2xx();
		$.GET("m2").P("flag", "childs").P("sort", "filename,asc").is2xx();//.and(docs.readChilds("read-step3"));

		// Delete
		$.DELETE("m3").is2xx();//.and(docs.remove("remove-step1"));
		$.GET("m1").P("flag", "childs").is2xx();

		$.DELETE("m2").is2xx();//.and(docs.remove("remove-step2"));
		$.GET("m1").P("flag", "childs").is2xx();
        
		$.DELETE("m4").is2xx();
		$.DELETE("m1").is2xx();
	}	
}
