package org.springframework.data.rest.webmvc.multipart;

import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;

public class MultipartControllerTests extends ApplicationTests{

	
	
	@Test
	public void contextLoads() throws Exception {
		
		MultipartControllerDocs docs = new MultipartControllerDocs();
		
		String beanName = "mystorage";
//		String beanName = "oops";
		////////////////////////////////////////
		//
		////////////////////////////////////////
		$.MULTIPART("/multipart").is4xx();

		$.MULTIPART("/multipart/abcd").is4xx();

		$.MULTIPART("/multipart/"+beanName).P("directory", "New Directory").is2xx("m1").and(docs.mkdir(1));
		$.MULTIPART("m1").P("directory", "Child Directory").is2xx("m2").and(docs.mkdir(2));
		
		$.GET("m1").is2xx().and(docs.attrs(1));
		$.GET("m2").is2xx().and(docs.attrs(2));
		$.GET("m1").P("flag", "download").is4xx();
		$.GET("m2").P("flag", "preview").is4xx();

////////		//rename
////////		Map<String,Object> m = new HashMap<String,Object>();
////////		m.put("filename", "ChangeDirectory");
////////		$.PATCH("m1").C(m).is2xx("m1");
////////		$.GET("m1").is2xx();

		////////////////////////////////////////
		//
		////////////////////////////////////////
		MockMultipartFile f1 = new MockMultipartFile("file", "MyFile1.html", "text/plain", ("File Contents..........").getBytes());
		MockMultipartFile f2 = new MockMultipartFile("file", "MyFile2.html", "text/plain", ("C"+System.currentTimeMillis()+"D").getBytes());
		MockMultipartFile f3 = new MockMultipartFile("file", "MyFile3.json", "application/json", ("{time:"+System.currentTimeMillis()+"}").getBytes());
		MockMultipartFile f4 = new MockMultipartFile("file", "MyFile4.json", "application/json", ("{time:"+System.currentTimeMillis()+"}").getBytes());
		MockMultipartFile f5 = new MockMultipartFile("file", "MyFile5.json", "application/json", ("{time:"+System.currentTimeMillis()+"}").getBytes());

		//create
		$.MULTIPART("/multipart/"+beanName+"/a/b/c").F(f1).is4xx();
		$.MULTIPART("/multipart/"+beanName).F(f1).is2xx("m3").and(docs.upload(3));
		$.MULTIPART("m1").F(f2).is2xx("m4");
		$.MULTIPART("m1").F(f3).is2xx();
		$.MULTIPART("m2").F(f4).is2xx("m5").and(docs.upload(5));
		$.MULTIPART("m2").F(f5).is2xx();
		
		//read
		$.GET("m3").is2xx().and(docs.attrs(3));
		$.GET("m4").is2xx();
		$.GET("m5").is2xx().and(docs.attrs(5));

		//Download
		$.GET("m3").P("flag", "preview").is2xx().and(docs.preview(3));
		$.GET("m3").P("flag", "download").is2xx().and(docs.download(3));
		$.GET("m4").P("flag", "preview").is2xx();
		$.GET("m4").P("flag", "download").is2xx();
		$.GET("m5").P("flag", "preview").is2xx().and(docs.preview(5));
		$.GET("m5").P("flag", "download").is2xx().and(docs.download(5));
		
////////		//versions
////////		Map<String,Object> f = new HashMap<String,Object>();
////////		f.put("filename", "Renamed.html");
////////		$.PATCH("m5").C(f).is2xx();//.and(docs.updateRename("update-step1"));
////////		$.GET("m5").is2xx();
////////
////////		$.PUT("m5").C("a", "Modify Contents..........").is2xx();//.and(docs.updateModify("update-step2"));
////////		$.GET("m5").is2xx();
////////		
////////		
////////		//version
////////		$.GET("/multipart").is4xx();
////////		$.GET("m1").P("flag", "history").is2xx();
////////		$.GET("m2").P("flag", "history").is2xx();
////////		$.GET("m3").P("flag", "history").is2xx();
////////		$.GET("m4").P("flag", "history").is2xx();
////////		$.GET("m5").P("flag", "history").is2xx();//.and(docs.updateHistory("update-step3"));;

		//childs
		$.GET("m1").P("flag", "childs").is2xx().and(docs.childs(1));
		$.GET("m2").P("flag", "childs").is2xx().and(docs.childs(2));
		$.GET("m3").P("flag", "childs").is2xx();
		$.GET("m4").P("flag", "childs").is2xx();
		$.GET("m5").P("flag", "childs").is2xx();
		$.GET("m2").P("flag", "childs").is2xx();

		// Delete
		$.DELETE("m3").is2xx().and(docs.delete(3));
		$.GET("m1").P("flag", "childs").is2xx();

		$.DELETE("m2").is2xx();//.and(docs.remove("remove-step2"));
		$.GET("m1").P("flag", "childs").is2xx();
        
		$.DELETE("m4").is2xx();
		$.DELETE("m1").is2xx();
	}	
}
