package myzd;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author yrw
 * @since 2018/2/13
 */

@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@SpringBootTest(classes = EdgeGatewayServiceApplication.class)
public class DataTest {

	@Autowired
	private MockMvc mvc;

	@Test
	public void createUserTest() throws Exception {
		mvc.perform(post("/api/v1/user")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.content(
										"{\"name\" : \"create a user\"}"))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value("1000000"))
						.andExpect(jsonPath("$.message").value("OK"))
						.andExpect(jsonPath("$.data.id").exists())
						.andExpect(jsonPath("$.data.name").value("create a user")
						);

	}

	@Test
	public void updateUserTest() throws Exception {
		mvc.perform(put("/api/v1/user/100")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.content("{\"name\" : \"update a user\"}"))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value("1000000"))
						.andExpect(jsonPath("$.message").value("OK"))
						.andExpect(jsonPath("$.data.id").exists())
						.andExpect(jsonPath("$.data.name").value("update a user")
						);
	}

}
