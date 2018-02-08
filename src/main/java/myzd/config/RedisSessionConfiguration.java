package myzd.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author Created by Chen Baichuan 25/7/2017
 */

@Slf4j
@Configuration
@EnableRedisHttpSession
public class RedisSessionConfiguration {

	private final static String REDIS_REMOTE_HOST = "spring.redis.host";
	private final static String REDIS_POOL_MAX_ACTIVE = "spring.redis.pool.max-active";
	private final static String REDIS_POOL_MAX_IDLE = "spring.redis.pool.max-idle";
	private final static String REDIS_POOL_MIN_IDLE = "spring.redis.pool.min-idle";
	private final static String REDIS_POOL_MAX_WAIT = "spring.redis.pool.max-wait";
	private final static String COOKIE_NAME = "server.session.cookie.name";
	private final static String COOKIE_PATH = "server.session.cookie.path";
	private final static String COOKIE_DOMAIN = "server.session.cookie.domain";
	private final static String COOKIE_HTTP_ONLY = "server.session.cookie.http-only";
	private final static String COOKIE_SECURE = "server.session.cookie.secure";
	private final static String MAX_INACTIVE_INTERVAL_IN_SECONDS = "spring.session.timeout";

	@Autowired
	private Environment env;

	@Bean
	public RedisOperationsSessionRepository sessionRepository(RedisConnectionFactory redisConnectionFactory) {
		RedisOperationsSessionRepository sessionRepository = new RedisOperationsSessionRepository(redisConnectionFactory);
		sessionRepository.setDefaultMaxInactiveInterval(Integer.parseInt(env.getProperty(MAX_INACTIVE_INTERVAL_IN_SECONDS)));
		return sessionRepository;
	}

	@Bean
	public JedisConnectionFactory connectionFactory() {
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		jedisPoolConfig.setMaxTotal(Integer.parseInt(env.getProperty(REDIS_POOL_MAX_ACTIVE)));
		jedisPoolConfig.setMaxIdle(Integer.parseInt(env.getProperty(REDIS_POOL_MAX_IDLE)));
		jedisPoolConfig.setMinIdle(Integer.parseInt(env.getProperty(REDIS_POOL_MIN_IDLE)));
		jedisPoolConfig.setMaxWaitMillis(Long.parseLong(env.getProperty(REDIS_POOL_MAX_WAIT)));
		jedisPoolConfig.setTestOnBorrow(true);
		jedisPoolConfig.setTestOnReturn(true);
		jedisPoolConfig.setTestWhileIdle(true);

		JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(jedisPoolConfig);
		jedisConnectionFactory.setUsePool(true);
		jedisConnectionFactory.setHostName(env.getProperty((REDIS_REMOTE_HOST)));
		return jedisConnectionFactory;
	}

	@Bean
	@Autowired
	public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		if (null == redisConnectionFactory) {
			log.error("Redis Template Service is not available");
			return null;
		}
		RedisTemplate<String, String> redisTemplate = new RedisTemplate<String, String>();
		redisTemplate.setConnectionFactory(connectionFactory());
		return redisTemplate;
	}

	@Bean
	public CookieSerializer cookieSerializer() {
		DefaultCookieSerializer defaultCookieSerializer = new DefaultCookieSerializer();
		defaultCookieSerializer.setCookieName(env.getProperty(COOKIE_NAME));
		defaultCookieSerializer.setCookiePath(env.getProperty(COOKIE_PATH));
		defaultCookieSerializer.setUseSecureCookie(Boolean.parseBoolean(env.getProperty(COOKIE_SECURE)));
		defaultCookieSerializer.setUseHttpOnlyCookie(Boolean.parseBoolean(env.getProperty(COOKIE_HTTP_ONLY)));
		defaultCookieSerializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$");
		return defaultCookieSerializer;
	}
}
