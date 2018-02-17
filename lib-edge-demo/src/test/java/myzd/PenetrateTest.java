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

	//200 json and file
	//json has been tested
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
		mvc.perform(get("/api/v1/redirect?url=/api/v1/user/99")
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().is3xxRedirection()
						);
	}

	//404
	@Test
	public void otherCodeTest() throws Exception {
		mvc.perform(get("/api/v1/notfound")
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().is4xxClientError()
						);
	}
}
