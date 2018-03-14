package libedge.config;

import libedge.services.impl.RateLimiterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
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

	private final String REDIS_HOST_NAME = "${rateLimit.redis.host}";
	private final String REDIS_PASSWORD = "${rateLimit.redis.password}";
	private final String REDIS_PORT = "${rateLimit.redis.port}";
	private final String REDIS_MAX_ACTIVE = "${rateLimit.redis.pool.max-active}";
	private final String REDIS_MAX_IDLE = "${rateLimit.redis.pool.max-idle}";
	private final String REDIS_MIN_IDLE = "${rateLimit.redis.pool.min-idle}";
	private final String REDIS_MAX_WAIT = "${rateLimit.redis.pool.max-wait}";

	@Bean
	@SuppressWarnings("unchecked")
	public RedisScript libEdgeRateLimiterRedisScript() {
		DefaultRedisScript redisScript = new DefaultRedisScript<>();
		redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("META-INF/scripts/request_rate_limiter.lua")));
		redisScript.setResultType(List.class);
		return redisScript;
	}


	@Bean
	public RedisTemplate libEdgeRateLimiterRedisTemplate(
					@Value(REDIS_HOST_NAME) String hostName,
					@Value(REDIS_PASSWORD) String password,
					@Value(REDIS_PORT) int port,
					@Value(REDIS_MAX_ACTIVE) String maxActive,
					@Value(REDIS_MAX_IDLE) String maxIdle,
					@Value(REDIS_MIN_IDLE) String minIdle,
					@Value(REDIS_MAX_WAIT) String maxWait
	) {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		if (StringUtils.isNotBlank(minIdle)) {
			poolConfig.setMinIdle(Integer.valueOf(minIdle));
		}
		if (StringUtils.isNotBlank(maxIdle)) {
			poolConfig.setMaxIdle(Integer.valueOf(maxIdle));
		}
		if (StringUtils.isNotBlank(maxActive)) {
			poolConfig.setMaxTotal(Integer.valueOf(maxActive));
		}
		if (StringUtils.isNotBlank(maxWait)) {
			poolConfig.setMaxWaitMillis(Integer.valueOf(maxWait));
		}
		JedisConnectionFactory factory = new JedisConnectionFactory(poolConfig);
		factory.setHostName(hostName);
		factory.setPassword(password);
		factory.setPort(port);
		factory.afterPropertiesSet();

		RedisSerializer serializer = new StringRedisSerializer();
		RedisTemplate redisTemplate = new RedisTemplate();
		redisTemplate.setConnectionFactory(factory);
		redisTemplate.setKeySerializer(serializer);
		redisTemplate.setValueSerializer(serializer);
		return redisTemplate;
	}

	@Bean
	@Autowired
	public RateLimiterService redisRateLimiter(
					@Qualifier("libEdgeRateLimiterRedisTemplate") RedisTemplate<String, String> redisTemplate,
					@Qualifier("libEdgeRateLimiterRedisScript") RedisScript<List<Long>> redisScript,
					@Autowired ApplicationContext applicationContext) {
		return new RateLimiterService(redisTemplate, redisScript);
	}
}
