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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author yrw
 * @since 2018/2/13
 */
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@SpringBootTest(classes = EdgeGatewayServiceApplication.class)
public class FilterValueTest {

	@Autowired
	private MockMvc mvc;

	@Test
	public void testPojo() throws Exception {
		mvc.perform(get("/api/v1/user/10")
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value("1000000"))
						.andExpect(jsonPath("$.message").value("OK"))
						.andExpect(jsonPath("$.data.id").value(10L)
						);
	}

	//page
	@Test
	public void testPageResult() throws Exception {
		mvc.perform(get("/api/v1/test/page")
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(1000000))
						.andExpect(jsonPath("$.message").value("OK"))
						.andExpect(jsonPath("$.data.list[*]").value("Test")
						);
	}

	//list
	@Test
	public void testListResult() throws Exception {
		mvc.perform(get("/api/v1/test/list").accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.code").value(1000000))
						.andExpect(jsonPath("$.message").value("OK"))
						.andExpect(jsonPath("$.data.list.length()").value(1))
						.andExpect(jsonPath("$.data.list[0].id").exists())
						.andExpect(jsonPath("$.data.list[0].name").value("Test")
						);
	}

	@Test
	public void testListMap() throws Exception {
		mvc.perform(get("/api/v1/test/list/map").accept(MediaType.APPLICATION_JSON))
						.andExpect(jsonPath("$.code").value(1000000))
						.andExpect(jsonPath("$.message").value("OK"))
						.andExpect(jsonPath("$.data.list.length()").value(1))
						.andExpect(jsonPath("$.data.list[0].key").value("test")
						);
	}


}
