package libedge.services.impl;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;

/**
 * @author yrw
 * @since 2018/3/6
 */

public class AuthenticationService {

	private RedisTemplate redisTemplate;

	public AuthenticationService(RedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public Map<String, String> getUserDetailsFromSession(String token) {
		HashOperations<String, String, String> hash = redisTemplate.opsForHash();
		return hash.entries(token);
	}
}
