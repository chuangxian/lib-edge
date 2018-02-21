package myzd;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author yrw
 * @since 2018/2/14
 */


@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@SpringBootTest(classes = EdgeGatewayServiceApplication.class)
public class PenetrateTest {

	@Autowired
	private MockMvc mvc;

	//200
	//json
	@Test
	public void getUserTest() throws Exception {
		mvc.perform(get("/api/v1/user/99")
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value("1000000"))
						.andExpect(jsonPath("$.message").value("OK"))
						.andExpect(jsonPath("$.data.id").value(99))
						.andExpect(jsonPath("$.data.name").value("Name of 99")
						);

	}

	@Test
	public void createUserTest() throws Exception {
		mvc.perform(post("/api/v1/user")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.content("{\"name\" : \"create a user\"}"))
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

	//file
	@Test
	public void fileTest() throws Exception {
		mvc.perform(get("/api/v1/test/download")
						.accept("*/*"))
						.andExpect(status().isOk())
						.andExpect(header().stringValues("Content-Type", "image/png"))
						.andExpect(header().stringValues("Content-Disposition", "attachment;filename=baidu.png")
						);
	}

	//>=500
	@Test
	public void ServerErrorTest() throws Exception {
		mvc.perform(get("/api/v1/servererror?code=1212121&message=error")
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(1212121))
						.andExpect(jsonPath("$.message").value("error")
						);
	}

	//301or302
	@Test
	public void redirectTest() throws Exception {
		MvcResult mvcResult =
						mvc.perform(get("/api/v1/redirect?url=/api/v1/user/99")
										.accept(MediaType.APPLICATION_JSON))
										.andExpect(status().is3xxRedirection())
										.andReturn();

		assertThat(mvcResult.getResponse().getRedirectedUrl()).endsWith("/api/v1/user/99");
	}

	//otherCode
	@Test
	public void clientErrorTest1() throws Exception {
		mvc.perform(get("/api/v1/clienterror")
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().is4xxClientError()
						);
	}

	@Test
	public void clientErrorTest2() throws Exception {
		mvc.perform(get("/api/v1/notfound")
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().is4xxClientError()
						);
	}
}
