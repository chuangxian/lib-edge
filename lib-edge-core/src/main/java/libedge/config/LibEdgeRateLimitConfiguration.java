package libedge.config;

import libedge.services.impl.RateLimiterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;

/**
 * @author yrw
 * @since 2018/2/17
 */

@Slf4j
@Configuration
public class LibEdgeRateLimitConfiguration {

	@Bean
	@SuppressWarnings("unchecked")
	public RedisScript rateLimiterRedisScript() {
		DefaultRedisScript redisScript = new DefaultRedisScript<>();
		redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("META-INF/scripts/request_rate_limiter.lua")));
		redisScript.setResultType(List.class);
		return redisScript;
	}

	@Bean
	@ConfigurationProperties(prefix = "rateLimit.redis")
	public JedisPoolConfig getRedisConfig() {
		JedisPoolConfig config = new JedisPoolConfig();
		return config;
	}

	@Bean
	public JedisConnectionFactory rateLimitConnectionFactory() {
		JedisConnectionFactory factory = new JedisConnectionFactory();
		JedisPoolConfig config = getRedisConfig();
		factory.setPoolConfig(config);
		return factory;
	}

	@Bean
	@Autowired
	public RedisTemplate<String, String> rateLimiterRedisTemplate(
					@Qualifier("rateLimitConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
		if (null == redisConnectionFactory) {
			log.error("Redis Template Service is not available");
			return null;
		}
		RedisSerializer serializer = new StringRedisSerializer();
		RedisTemplate<String, String> redisTemplate = new RedisTemplate<String, String>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.setKeySerializer(serializer);
		return redisTemplate;
	}

	@Bean
	@Autowired
	public RateLimiterService redisRateLimiter(
					@Qualifier("rateLimiterRedisTemplate") RedisTemplate<String, String> redisTemplate,
					@Qualifier("rateLimiterRedisScript") RedisScript<List<Long>> redisScript) {
		return new RateLimiterService(redisTemplate, redisScript);
	}
}
