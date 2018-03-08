package libedge.services.impl;

import com.google.common.base.Charsets;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import javax.websocket.Session;
import java.util.Date;
import java.util.Map;

/**
 * @author yrw
 * @since 2018/3/6
 */

public class SessionCacheService {

	private RedisTemplate<String, String> redisTemplate;

	@Value("${session.expire.seconds}")
	private Integer expireSeconds;

	public SessionCacheService(RedisTemplate<String, String> redisTemplate){
		this.redisTemplate = redisTemplate;
	}

	private String hashState(Map<String, String> state) {
		Hasher hasher = Hashing.md5().newHasher();
		state.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
			hasher.putString(entry.getKey(), Charsets.UTF_8);
			hasher.putString(entry.getValue() == null ? "" : entry.getValue(), Charsets.UTF_8);
		});
		return Hex.encodeHexString(hasher.hash().asBytes());
	}

	public String createCache(Map<String, String> state2cache) {
		if (state2cache == null) {
			return null;
		}
		String key = hashState(state2cache);
		redisTemplate.delete(key);
		redisTemplate.opsForHash().putAll(key, state2cache);
		redisTemplate.expireAt(key, DateUtils.addMinutes(new Date(), expireSeconds));
		return key;
	}

	public Map<String, String> getState(String key) {
		HashOperations<String, String, String> hash = redisTemplate.opsForHash();
		return hash.entries(key);
	}

	public void deleteState(String key) {
		redisTemplate.delete(key);
	}
}
