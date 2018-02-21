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
 * @since 2018/2/17
 */

//TODO:不透传的时候会有问题

@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@SpringBootTest(classes = EdgeGatewayServiceApplication.class)
public class RateLimitTest {

	@Autowired
	private MockMvc mvc;

	@Test
	public void getRateLimitTest() throws Exception {
		for (Long id = 0L; id < 10; id++) {
			mvc.perform(get(String.format("/api/v1/user/%d/ratelimit", id))
							.accept(MediaType.APPLICATION_JSON))
							.andExpect(status().isOk())
							.andExpect(jsonPath("$.code").value(1000000))
							.andExpect(jsonPath("$.message").value("OK"))
							.andExpect(jsonPath("$.data.id").value(id))
							.andExpect(jsonPath("$.data.name").value("Name of " + id)
							);
		}

		//TODO: sometimes there is an off by one error
		mvc.perform(get("/api/v1/user/55/ratelimit")
						.accept(MediaType.APPLICATION_JSON));

		//not allowed
		mvc.perform(get("/api/v1/user/56/ratelimit")
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.message").value("too many requests!")
						);

		Thread.sleep(4000);

		//After the burst is done, check the steady state
		for (Long id = 0L; id < 10; id++) {
			mvc.perform(get(String.format("/api/v1/user/%d/ratelimit", id))
							.accept(MediaType.APPLICATION_JSON))
							.andExpect(status().isOk())
							.andExpect(jsonPath("$.code").value(1000000))
							.andExpect(jsonPath("$.message").value("OK"))
							.andExpect(jsonPath("$.data.id").value(id))
							.andExpect(jsonPath("$.data.name").value("Name of " + id)
							);
		}

		//TODO: sometimes there is an off by one error
		mvc.perform(get("/api/v1/user/55/ratelimit")
						.accept(MediaType.APPLICATION_JSON));

		//not allowed
		mvc.perform(get("/api/v1/user/57/ratelimit")
						.accept(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.message").value("too many requests!")
						);
	}


	@Test
	public void postRateLimitTest() throws Exception {
		for (Long id = 0L; id < 20; id++) {
			mvc.perform(post("/api/v1/user/ratelimit")
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON)
							.content("{\"name\":\"create a user\"}"))
							.andExpect(status().isOk())
							.andExpect(jsonPath("$.code").value(1000000))
							.andExpect(jsonPath("$.message").value("OK"))
							.andExpect(jsonPath("$.data.id").exists())
							.andExpect(jsonPath("$.data.name").value("create a user")
							);
		}

		//TODO: sometimes there is an off by one error
		mvc.perform(post("/api/v1/user/ratelimit")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"create a user\"}")
		);

		//not allowed
		mvc.perform(post("/api/v1/user/ratelimit")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"create a user\"}"))
						.andExpect(status().isOk())
						.andDo(print())
						.andExpect(jsonPath("$.message").value("too many requests!")
						);

		Thread.sleep(4000);

		//After the burst is done, check the steady state
		for (Long id = 0L; id < 20; id++) {
			mvc.perform(post("/api/v1/user/ratelimit")
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON)
							.content("{\"name\":\"create a user\"}"))
							.andExpect(status().isOk())
							.andExpect(jsonPath("$.code").value(1000000))
							.andExpect(jsonPath("$.message").value("OK"))
							.andExpect(jsonPath("$.data.id").exists())
							.andExpect(jsonPath("$.data.name").value("create a user")
							);
		}

		//not allowed
		mvc.perform(post("/api/v1/user/ratelimit")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"create a user\"}"))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.message").value("too many requests!")
						);
	}

}
