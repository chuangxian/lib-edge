package myzd;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author yrw
 * @since 2018/2/14
 */

@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@SpringBootTest(classes = EdgeGatewayServiceApplication.class)
public class AuthorizationTest {

	@Autowired
	private MockMvc mvc;

	//has authority
	@Test
	public void getAllowedTest() throws Exception {
		Long id = System.currentTimeMillis();
		mvc.perform(get(String.format("/api/v1/user/%d/authorize", id))
						.header("Authorization",
										"Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOiIxIiwicm9sZSI6IlNUQUZGIiwidXNlck5hbWUiOiLoooHlh6Hov6oiLCJkZXBhcnRtZW50IjoiMTQiLCJ1c2VyIjoibGl1QG1pbmd5aXpodWRhby5jb20iLCJpYXQiOjE1MDg4MTc2NTAsInJlcUlkIjoiZTNhY2Y5ZGFhOGZkYTdmYjM1OThlMTg4MTQyMTRlZGQifQ.vdqfZmMXg5E4O6uFb6vbPsz7xzgySMdJ8zirE6IfZNg")
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(1000000))
						.andExpect(jsonPath("$.message").value("OK"))
						.andExpect(jsonPath("$.data.id").value(id))
						.andExpect(jsonPath("$.data.name").value("Name of " + id)
						);
	}

	@Test
	public void postAllowedTest() throws Exception {
		Long id = System.currentTimeMillis();
		mvc.perform(post(String.format("/api/v1/user/authorize", id))
						.header("Authorization",
										"Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOiIxIiwicm9sZSI6IlNUQUZGIiwidXNlck5hbWUiOiLoooHlh6Hov6oiLCJkZXBhcnRtZW50IjoiMTQiLCJ1c2VyIjoibGl1QG1pbmd5aXpodWRhby5jb20iLCJpYXQiOjE1MDg4MTc2NTAsInJlcUlkIjoiZTNhY2Y5ZGFhOGZkYTdmYjM1OThlMTg4MTQyMTRlZGQifQ.vdqfZmMXg5E4O6uFb6vbPsz7xzgySMdJ8zirE6IfZNg")
						.content("{\"name\" : \"create a user\"}")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(1000000))
						.andExpect(jsonPath("$.message").value("OK"))
						.andExpect(jsonPath("$.data.name").value("create a user")
						);
	}

	//has no authority
	@Test
	public void fobiddenTest() throws Exception {
		mvc.perform(get("/api/v1/user/19960519/authorize")
						.header("Authorization",
										"Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOiIxNSIsInJvbGUiOiJTVEFGRiIsInVzZXJOYW1lIjoi6KKB5Yeh6L-qIiwiZGVwYXJ0bWVudCI6IjE0IiwidXNlciI6ImxpdUBtaW5neWl6aHVkYW8uY29tIiwiaWF0IjoxNTA4ODE3NjUwLCJyZXFJZCI6ImUzYWNmOWRhYThmZGE3ZmIzNTk4ZTE4ODE0MjE0ZWRkIn0.TOQlsUFRd3BxZvptDSQyi7czH4hm8tLiPT1S6I1v_xE")
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(1998001))
						.andExpect(jsonPath("$.message").value("Access is denied")
						);
	}

	//role not existed
	@Test
	public void canNotFindRoleTest() throws Exception {
		mvc.perform(get("/api/v1/user/19960519/authorize")
						.header("Authorization",
										"Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOiIxOTk4Iiwicm9sZSI6IlNUQUZGIiwidXNlck5hbWUiOiLoooHlh6Hov6oiLCJkZXBhcnRtZW50IjoiMTQiLCJ1c2VyIjoibGl1QG1pbmd5aXpodWRhby5jb20iLCJpYXQiOjE1MDg4MTc2NTAsInJlcUlkIjoiZTNhY2Y5ZGFhOGZkYTdmYjM1OThlMTg4MTQyMTRlZGQifQ.4Xd8UwGvo7MPjpBh2dnGjqkCbOPtx0omLomDaoP6ncY")
						.accept(MediaType.APPLICATION_JSON))
						.andDo(print())
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(1998001))
						.andExpect(jsonPath("$.message").value("Access is denied")
						);
	}

	@Test
	public void unAuthenticatedTest() throws Exception {
		mvc.perform(get("/api/v1/user/19960519/authorize")
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(1998001))
						.andExpect(jsonPath("$.message").value("Access is denied")
						);
	}

}
