package libedge.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import libedge.services.impl.JwtService;
import libedge.services.impl.PipeService;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;


/**
 * @author yrw
 * @since 2018/2/17
 */
@Configuration
public class LibEdgePipeConfiguration {

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
	public ObjectMapper libEdgePipeObjectMapper() {
		return new ObjectMapper();
	}

	@Bean
	public JwtService libEdgeJwtService() {
		return new JwtService();
	}
}
