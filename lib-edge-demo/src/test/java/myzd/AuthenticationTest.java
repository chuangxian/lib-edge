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
										"Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOjI3LCJtb2JpbGUiOiIxNzcxNzM5NDU2MCIsImF2YXRhciI6Imh0dHBzOi8vcC5xbG9nby5jbi9iaXptYWlsL0E0ckw0M1JhbDdjdzlsaWNCN0lsT3dWRG1FWGliMklCRjJpY2R5Vkt1QkY3VmZLV0ZSS0pqaWFzNkEvMCIsInVzZXJOYW1lIjoi5byg5YWL5Y2HIiwidWlkIjoiU0gwMDE3IiwibmJmIjoxNTE5NzEyOTQwLCJzdGFmZl9pZCI6IlNIMDAxNyIsIm5hbWUiOiLlvKDlhYvljYciLCJleHAiOjE1MjAzMTc3NDAsImRlcGFydG1lbnQiOiIxNCIsInVzZXIiOiJjbGFyay56aGFuZ0BtaW5neWl6aHVkYW8uY29tIiwiaWF0IjoxNTE5NzEyOTQwLCJlbWFpbCI6ImNsYXJrLnpoYW5nQG1pbmd5aXpodWRhby5jb20iLCJqdGkiOiLsmpbgtIbtj5bgrqzovKDhuJXhq47mpY_lmY3ulbTupLfguJ_is6Hpjqbrm67pu5oifQ.oZU3y5DLTbI5D8FCKPmwjWxBp_UHxjpSGViDI2zwGQc")
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
