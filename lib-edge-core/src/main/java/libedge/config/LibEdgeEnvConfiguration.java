package libedge.config;

import libedge.config.security.UserDetailsServiceImpl;
import libedge.services.impl.JwtService;
import libedge.services.impl.PipeService;
import libedge.services.impl.RateLimiterService;
import libedge.services.impl.SessionCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author yrw
 * @since 2018/2/25
 */
@Configuration
public class LibEdgeEnvConfiguration {

  @Autowired
  ConfigurableEnvironment env;

  private static AnnotationConfigApplicationContext internalContext;

  public AnnotationConfigApplicationContext libEdgeApplicationContext() {
    if (internalContext != null) return internalContext;
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.register(
      LibEdgePipeConfiguration.class,
      LibEdgeAuthorizationConfiguration.class,
      LibEdgeRateLimitConfiguration.class
    );
    context.setEnvironment(env);
    context.refresh();
    internalContext = context;
    return context;
  }

  @Bean
  public UserDetailsServiceImpl userDetailsServiceImpl() {
    return (UserDetailsServiceImpl) libEdgeApplicationContext().getBean("userDetailsServiceImpl");
  }

  @Bean
  public JwtService libEdgeJwtService() {
    return (JwtService) libEdgeApplicationContext().getBean("libEdgeJwtService");
  }

  @Bean
  public PipeService libEdgePipeService() {
    return (PipeService) libEdgeApplicationContext().getBean("pipeService");
  }

  @Bean
  public RateLimiterService rateLimiterService() {
    return (RateLimiterService) libEdgeApplicationContext().getBean("redisRateLimiter");
  }

  @Bean
  public SessionCacheService libEdgeSessionCacheService(){
    return (SessionCacheService) libEdgeApplicationContext().getBean("libEdgeSessionCacheService");
  }
}