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

	//authentication

	//has identity message
	@Test
	public void authenticationTest() throws Exception {
		mvc.perform(get("/api/v1/authenticate")
						.header("Authorization",
										"Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOiIxIiwicm9sZSI6IlNUQUZGIiwidXNlck5hbWUiOiLoooHlh6Hov6oiLCJkZXBhcnRtZW50IjoiMTQiLCJ1c2VyIjoibGl1QG1pbmd5aXpodWRhby5jb20iLCJpYXQiOjE1MDg4MTc2NTAsInJlcUlkIjoiZTNhY2Y5ZGFhOGZkYTdmYjM1OThlMTg4MTQyMTRlZGQifQ.wFRkxu2qUW8kDcIX4yC9UZ1TZ_XdG97yvEDisA_Xja0")
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(1000000))
						.andExpect(jsonPath("$.message").value("OK"))
						.andExpect(jsonPath("$.data").value("success authenticated")
						);

	}

	//has no identity message
	@Test
	public void unAuthenticatedTest() throws Exception {
		mvc.perform(get("/api/v1/authenticate")
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(1998001))
						.andExpect(jsonPath("$.message").value("unauthenticated")
						);
	}

	//authorization

	//has authority
	@Test
	public void allowedTest() throws Exception {
		mvc.perform(post("/api/v1/authorize")
						.content("{\"name\":\"test\"}")
						.header("Authorization",
										"Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOiIxIiwicm9sZSI6IlNUQUZGIiwidXNlck5hbWUiOiLoooHlh6Hov6oiLCJkZXBhcnRtZW50IjoiMTQiLCJ1c2VyIjoibGl1QG1pbmd5aXpodWRhby5jb20iLCJpYXQiOjE1MDg4MTc2NTAsInJlcUlkIjoiZTNhY2Y5ZGFhOGZkYTdmYjM1OThlMTg4MTQyMTRlZGQifQ.vdqfZmMXg5E4O6uFb6vbPsz7xzgySMdJ8zirE6IfZNg")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(1000000))
						.andExpect(jsonPath("$.message").value("OK"))
						.andExpect(jsonPath("$.data.name").value("test")
						);
	}

	//has no authority
	@Test
	public void fobiddenTest() throws Exception {
		mvc.perform(post("/api/v1/authorize")
						.content("{\"name\":\"test\"}")
						.header("Authorization",
										"Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOiIxNSIsInJvbGUiOiJTVEFGRiIsInVzZXJOYW1lIjoi6KKB5Yeh6L-qIiwiZGVwYXJ0bWVudCI6IjE0IiwidXNlciI6ImxpdUBtaW5neWl6aHVkYW8uY29tIiwiaWF0IjoxNTA4ODE3NjUwLCJyZXFJZCI6ImUzYWNmOWRhYThmZGE3ZmIzNTk4ZTE4ODE0MjE0ZWRkIn0.TOQlsUFRd3BxZvptDSQyi7czH4hm8tLiPT1S6I1v_xE")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.message").value("不允许访问")
						);
	}

}
