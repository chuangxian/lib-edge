package libedge.services.impl;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;

/**
 * @author yrw
 * @since 2018/3/6
 */

public class CacheService {

	private RedisTemplate<String, String> redisTemplate;

	public CacheService(RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public Map<String, String> getState(String key) {
		HashOperations<String, String, String> hash = redisTemplate.opsForHash();
		return hash.entries(key);
	}
}
