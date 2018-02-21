package libedge.config;

import libedge.config.security.UserDetailsServiceImpl;
import libedge.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
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

	@Bean
	@ConfigurationProperties(prefix = "authorization.dataSource")
	public DataSource libEdgeAuthorizationDateSource() {
		return new DriverManagerDataSource();
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
	@ConfigurationProperties(prefix = "authorization.redis")
	public JedisPoolConfig getRedisConfig() {
		JedisPoolConfig config = new JedisPoolConfig();
		return config;
	}

	//TODO

	@Bean
	@Primary
	public JedisConnectionFactory libEdgeAuthorizationConnectionFactory() {
		JedisConnectionFactory factory = new JedisConnectionFactory();
		JedisPoolConfig config = getRedisConfig();
		factory.setPoolConfig(config);
		return factory;
	}

	@Bean
	@Autowired
	public RedisTemplate libEdgeAuthorizationRedisTemplate(
					@Qualifier("libEdgeAuthorizationConnectionFactory") RedisConnectionFactory redisConnectionFactory
	) {
		if (null == redisConnectionFactory) {
			log.error("Redis Template Service is not available");
			return null;
		}
		RedisSerializer serializer = new StringRedisSerializer();
		RedisTemplate<String, String> redisTemplate = new RedisTemplate<String, String>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
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
	@Autowired(required = false)
	public UserDetailsServiceImpl userDetailsServiceImpl(
					@Qualifier("libEdgeRoleRepository") RoleRepository roleRepository) {
		return new UserDetailsServiceImpl(roleRepository);
	}
}
