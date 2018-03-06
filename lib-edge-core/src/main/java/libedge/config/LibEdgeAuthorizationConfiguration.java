package libedge.config;

import libedge.config.security.UserDetailsServiceImpl;
import libedge.repository.RoleRepository;
import libedge.services.impl.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import redis.clients.jedis.JedisPoolConfig;

import javax.sql.DataSource;

/**
 * @author yrw
 * @since 2018/2/18
 */

@Slf4j
@Configuration
public class LibEdgeAuthorizationConfiguration {

	private final String DATASOURCE_URL = "${authorization.datasource.url}";
	private final String DATASOURCE_USERNAME = "${authorization.datasource.username}";
	private final String DATASOURCE_PASSWORD = "${authorization.datasource.password}";
	private final String DATASOURCE_DRIVER_CLASS_NAME = "${authorization.datasource.driverClassName}";

	private final String REDIS_HOST_NAME = "${authorization.redis.host}";
	private final String REDIS_PASSWORD = "${authorization.redis.password}";
	private final String REDIS_PORT = "${authorization.redis.port}";
	private final String REDIS_MAX_ACTIVE = "${authorization.redis.pool.max-active}";
	private final String REDIS_MAX_IDLE = "${authorization.redis.pool.max-idle}";
	private final String REDIS_MIN_IDLE = "${authorization.redis.pool.min-idle}";
	private final String REDIS_MAX_WAIT = "${authorization.redis.pool.max-wait}";

	@Bean
	public DataSource libEdgeAuthorizationDateSource(
					@Value(DATASOURCE_URL) String url,
					@Value(DATASOURCE_USERNAME) String username,
					@Value(DATASOURCE_PASSWORD) String password,
					@Value(DATASOURCE_DRIVER_CLASS_NAME) String driverClassName
	) {
		DriverManagerDataSource dataSource = new DriverManagerDataSource(url, username, password);
		dataSource.setDriverClassName(driverClassName);
		return dataSource;
	}

	@Bean
	@Autowired
	public JdbcTemplate libEdgeAuthorizationJdbcTemplate(
					@Qualifier("libEdgeAuthorizationDateSource") DataSource dataSource) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate();
		jdbcTemplate.setDataSource(dataSource);
		return jdbcTemplate;
	}

	@Bean
	public RedisTemplate libEdgeAuthorizationRedisTemplate(
					@Value(REDIS_HOST_NAME) String hostName,
					@Value(REDIS_PASSWORD) String password,
					@Value(REDIS_PORT) int port,
					@Value(REDIS_MAX_ACTIVE) String maxActive,
					@Value(REDIS_MAX_IDLE) String maxIdle,
					@Value(REDIS_MIN_IDLE) String minIdle,
					@Value(REDIS_MAX_WAIT) String maxWait
	) {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		if(StringUtils.isNotBlank(minIdle)) {
			poolConfig.setMinIdle(Integer.valueOf(minIdle));
		}
		if(StringUtils.isNotBlank(maxIdle)) {
			poolConfig.setMaxIdle(Integer.valueOf(maxIdle));
		}
		if(StringUtils.isNotBlank(maxActive)) {
			poolConfig.setMaxTotal(Integer.valueOf(maxActive));
		}
		if(StringUtils.isNotBlank(maxWait)) {
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
	public RoleRepository libEdgeRoleRepository(
					@Qualifier("libEdgeAuthorizationJdbcTemplate") JdbcTemplate jdbcTemplate,
					@Qualifier("libEdgeAuthorizationRedisTemplate") RedisTemplate redisTemplate) {
		return new RoleRepository(jdbcTemplate, redisTemplate);
	}

	@Bean
	@Autowired
	public UserDetailsServiceImpl userDetailsServiceImpl(
					@Qualifier("libEdgeRoleRepository") RoleRepository roleRepository) {
		return new UserDetailsServiceImpl(roleRepository);
	}

	@Bean
	public RedisTemplate libEdgeSessionRedisTemplate(){
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		JedisConnectionFactory factory = new JedisConnectionFactory(poolConfig);
		factory.setHostName("192.168.33.10");
		factory.setPort(6379);
		factory.afterPropertiesSet();

		RedisTemplate redisTemplate = new RedisTemplate();
		redisTemplate.setConnectionFactory(factory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new StringRedisSerializer());
		return redisTemplate;
	}

	@Bean
	public AuthenticationService libEdgeAuthenticationService(
					@Qualifier("libEdgeSessionRedisTemplate") RedisTemplate redisTemplate){
		return new AuthenticationService(redisTemplate);
	}
}
