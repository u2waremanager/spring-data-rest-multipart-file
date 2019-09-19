package org.springframework.data.rest.webmvc.multipart.test;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


@RunWith(SpringRunner.class)
@SpringBootTest
public abstract class ApplicationMockTests {

	protected Log logger = LogFactory.getLog(getClass());

	
	public final @Rule JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-restdocs-snippets");
	protected @Value("${spring.data.rest.base-path:}") String springDataRestBasePath;
	protected @Autowired WebApplicationContext context;
	protected ApplicationMockMvc $;
	
	@Before
	public void before() throws Exception {
		
		MockMvc mvc = MockMvcBuilders.webAppContextSetup(context)
				.apply(documentationConfiguration(restDocumentation))
				.build();
		this.$ = new ApplicationMockMvc(mvc, springDataRestBasePath);
		
	}


}
