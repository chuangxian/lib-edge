package libedge.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import libedge.services.impl.JwtService;
import libedge.services.impl.PipeService;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.IOException;


/**
 * @author yrw
 * @since 2018/2/17
 */
@Configuration
public class LibEdgePipeConfiguration {

	@Bean
	public ObjectMapper libEdgePipeObjectMapper() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.registerModule(new JavaTimeModule());
		return mapper;
	}

	@Bean
	@Autowired
	public PipeService pipeService(
					Environment env,
					@Qualifier("libEdgePipeOkHttpClient") OkHttpClient okHttpClient,
					@Qualifier("libEdgePipeObjectMapper") ObjectMapper objectMapper,
					@Qualifier("libEdgeJwtService") JwtService jwtService) {
		return new PipeService(env, okHttpClient, objectMapper, jwtService);
	}

	@Bean
	public OkHttpClient libEdgePipeOkHttpClient() {
		return new OkHttpClient.Builder()
						.followRedirects(false)
						.followSslRedirects(false)
						.build();
	}

	@Bean
	public JwtService libEdgeJwtService() {
		return new JwtService();
	}
}
