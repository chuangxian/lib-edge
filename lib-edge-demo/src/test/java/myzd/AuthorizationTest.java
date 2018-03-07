package myzd;

import libedge.services.impl.AuthenticationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
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

	@MockBean
	private AuthenticationService authenticationService;

	//has authority
	@Test
	public void getAllowedTest() throws Exception {

		Map<String, String> map = new HashMap<>();
		map.put("name", "刘钱");
		map.put("position", "高级后端工程师");
		map.put("mobile", "15037889077");
		map.put("gender", "1");
		map.put("email", "qian.liu@mingyizhudao.com");
		map.put("avatar", "https://p.qlogo.cn/bizmail/l9HibhqvRAIpIiaCn3dCrRYUH5t6EbQR7KTZU77F63UgtvqYyqTbcNtA/0");
		map.put("status", "1");
		map.put("bd_manager", "0");
		map.put("staff_id", "1");
		map.put("userid", "1");
		map.put("department", "[14]");

		when(authenticationService.getUserDetailsFromSession(anyString())).thenReturn(map);

		Long id = System.currentTimeMillis();
		mvc.perform(get(String.format("/api/v1/user/%d/authorize", id))
						.header("Authorization", "fcbb276443dc6aa29bbdfc30119b6395")
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

		Map<String, String> map = new HashMap<>();
		map.put("name", "刘钱");
		map.put("position", "高级后端工程师");
		map.put("mobile", "15037889077");
		map.put("gender", "1");
		map.put("email", "qian.liu@mingyizhudao.com");
		map.put("avatar", "https://p.qlogo.cn/bizmail/l9HibhqvRAIpIiaCn3dCrRYUH5t6EbQR7KTZU77F63UgtvqYyqTbcNtA/0");
		map.put("status", "1");
		map.put("bd_manager", "0");
		map.put("staff_id", "1");
		map.put("userid", "1");
		map.put("department", "[14]");

		when(authenticationService.getUserDetailsFromSession(anyString())).thenReturn(map);

		Long id = System.currentTimeMillis();
		mvc.perform(post(String.format("/api/v1/user/authorize", id))
						.header("Authorization", "fcbb276443dc6aa29bbdfc30119b6395")
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

		Map<String, String> map = new HashMap<>();
		map.put("name", "刘钱");
		map.put("position", "高级后端工程师");
		map.put("mobile", "15037889077");
		map.put("gender", "1");
		map.put("email", "qian.liu@mingyizhudao.com");
		map.put("avatar", "https://p.qlogo.cn/bizmail/l9HibhqvRAIpIiaCn3dCrRYUH5t6EbQR7KTZU77F63UgtvqYyqTbcNtA/0");
		map.put("status", "1");
		map.put("bd_manager", "0");
		map.put("staff_id", "5");
		map.put("userid", "5");
		map.put("department", "[14]");

		when(authenticationService.getUserDetailsFromSession(anyString())).thenReturn(map);

		mvc.perform(get("/api/v1/user/19960519/authorize")
						.header("Authorization", "fcbb276443dc6aa29bbdfc30119b6395")
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(1998001))
						.andExpect(jsonPath("$.message").value("Access is denied")
						);
	}

	//role not existed
	@Test
	public void canNotFindRoleTest() throws Exception {

		Map<String, String> map = new HashMap<>();
		map.put("name", "刘钱");
		map.put("position", "高级后端工程师");
		map.put("mobile", "15037889077");
		map.put("gender", "1");
		map.put("email", "qian.liu@mingyizhudao.com");
		map.put("avatar", "https://p.qlogo.cn/bizmail/l9HibhqvRAIpIiaCn3dCrRYUH5t6EbQR7KTZU77F63UgtvqYyqTbcNtA/0");
		map.put("status", "1");
		map.put("bd_manager", "0");
		map.put("staff_id", "9998");
		map.put("userid", "9998");
		map.put("department", "[14]");

		when(authenticationService.getUserDetailsFromSession(anyString())).thenReturn(map);

		mvc.perform(get("/api/v1/user/19960519/authorize")
						.header("Authorization", "fcbb276443dc6aa29bbdfc30119b6395")
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
