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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author yrw
 * @since 2018/2/20
 */

@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@SpringBootTest(classes = EdgeGatewayServiceApplication.class)
public class AuthenticationTest {

	@Autowired
	private MockMvc mvc;

	//authentication
	//has identity message
	@Test
	public void authenticationTest() throws Exception {
		Long id = System.currentTimeMillis();
		mvc.perform(get(String.format("/api/v1/user/%d/authenticate", id))
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

	//has no identity message
	@Test
	public void unAuthenticatedTest() throws Exception {
		mvc.perform(get("/api/v1/user/19960519/authenticate")
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(1998001))
						.andExpect(jsonPath("$.message").value("unauthenticated")
						);
	}

	//uid为空
	@Test
	public void uidNullTest() throws Exception {
		mvc.perform(get("/api/v1/user/19960519/authenticate")
						.header("Authorization",
										"Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiU1RBRkYiLCJ1c2VyTmFtZSI6IuiigeWHoei_qiIsImRlcGFydG1lbnQiOiIxNCIsInVzZXIiOiJsaXVAbWluZ3lpemh1ZGFvLmNvbSIsImlhdCI6MTUwODgxNzY1MCwicmVxSWQiOiJlM2FjZjlkYWE4ZmRhN2ZiMzU5OGUxODgxNDIxNGVkZCJ9.VlWzTIQ5L1nXOE8sX5zEcz10So-hR6DYokWDY115Dco")
						.accept(MediaType.APPLICATION_JSON))
						.andDo(print())
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(1998001))
						.andExpect(jsonPath("$.message").value("unauthenticated")
						);
	}
}
