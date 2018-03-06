package libedge.config;

import libedge.annotations.Authentication;
import libedge.config.security.UserDetailsServiceImpl;
import libedge.config.security.WebSecurityConfig;
import libedge.services.impl.AuthenticationService;
import libedge.services.impl.JwtService;
import libedge.services.impl.PipeService;
import libedge.services.impl.RateLimiterService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yrw
 * @since 2018/2/25
 */
@Configuration
public class LibEdgeEnvConfiguration {

	@Bean
	public AnnotationConfigApplicationContext libEdgeapplicationContext() {
		return new AnnotationConfigApplicationContext(
						LibEdgePipeConfiguration.class,
						LibEdgeAuthorizationConfiguration.class,
						LibEdgeRateLimitConfiguration.class);
	}

	@Bean
	public UserDetailsServiceImpl userDetailsServiceImpl() {
		return (UserDetailsServiceImpl) libEdgeapplicationContext().getBean("userDetailsServiceImpl");
	}

	@Bean
	public JwtService libEdgeJwtService() {
		return (JwtService) libEdgeapplicationContext().getBean("libEdgeJwtService");
	}

	@Bean
	public PipeService libEdgePipeService() {
		return (PipeService) libEdgeapplicationContext().getBean("pipeService");
	}

	@Bean
	public RateLimiterService rateLimiterService() {
		return (RateLimiterService) libEdgeapplicationContext().getBean("redisRateLimiter");
	}

	@Bean
	public AuthenticationService libEdgeAuthenticationService(){
		return (AuthenticationService)libEdgeapplicationContext().getBean("libEdgeAuthenticationService");
	}

}
