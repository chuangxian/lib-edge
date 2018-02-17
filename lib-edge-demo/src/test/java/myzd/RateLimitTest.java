package myzd;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author yrw
 * @since 2018/2/17
 */

@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@SpringBootTest(classes = EdgeGatewayServiceApplication.class)
public class RateLimitTest {

	@Autowired
	private MockMvc mvc;

	//Bursts work
	//allowed
	@Test
	public void rateLimitTest() throws Exception {
		for (Long id = 0L; id < 10; id++) {
			mvc.perform(get("/api/v1/user/" + id)
							.accept(MediaType.APPLICATION_JSON))
							.andExpect(status().isOk())
							.andExpect(jsonPath("$.code").value(1000000))
							.andExpect(jsonPath("$.message").value("OK"))
							.andExpect(jsonPath("$.data.id").value(id));
		}

		//TODO: sometimes there is an off by one error
		mvc.perform(get("api/v1/user/100")
						.accept(MediaType.APPLICATION_JSON));

		//not allowed
		mvc.perform(get("/api/v1/user/100")
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk())
						.andDo(print())
						.andExpect(jsonPath("$.message").value("too many requests!")
						);

		Thread.sleep(2000);

		//After the burst is done, check the steady state
		for (Long id = 0L; id < 10; id++) {
			mvc.perform(get("/api/v1/user/" + id)
							.accept(MediaType.APPLICATION_JSON))
							.andExpect(status().isOk())
							.andExpect(jsonPath("$.code").value(1000000))
							.andExpect(jsonPath("$.message").value("OK"))
							.andExpect(jsonPath("$.data.id").value(id));
		}

		//not allowed
			mvc.perform(get("/api/v1/user/200")
							.accept(MediaType.APPLICATION_JSON))
							.andExpect(status().isOk())
							.andExpect(jsonPath("$.message").value("too many requests!")
							);
	}

}
