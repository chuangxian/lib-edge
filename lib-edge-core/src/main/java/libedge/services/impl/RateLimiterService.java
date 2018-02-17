package libedge.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author yrw
 * @since 2018/2/15
 */
@Slf4j
public class RateLimiterService {

	public static final String REPLENISH_RATE_KEY = "replenishRate";
	public static final String BURST_CAPACITY_KEY = "burstCapacity";

	private final RedisTemplate<String, String> redisTemplate;
	private final RedisScript<List<Long>> script;


	public RateLimiterService(
					RedisTemplate<String, String> redisTemplate,
					RedisScript<List<Long>> redisScript) {
		this.redisTemplate = redisTemplate;
		this.script = redisScript;
	}

	/**
	 * @param id   用户id
	 * @param args 发送速率 桶容量
	 * @return boolean
	 */
	@SuppressWarnings("unchecked")
	public boolean isAllowed(String id, String requestAction, Map<String, Integer> args) {
		// How many requests per second do you want a user to be allowed to do?
		Integer replenishRate = args.get(REPLENISH_RATE_KEY);

		// How much bursting do you want to allow?
		Integer burstCapacity;
		if (args.get(BURST_CAPACITY_KEY) == -1) {
			burstCapacity = 2*replenishRate;
		} else {
			burstCapacity = args.get(BURST_CAPACITY_KEY);
		}

		try {
			// Make a unique key per us
			String prefix = String.format("rate_limiter.%s.%s", id, requestAction);

			// You need two Redis keys for Token Bucket.
			List<String> keys = Arrays.asList(prefix + ".tokens", prefix + ".timestamp");

			// The arguments to the LUA script. time() returns unixtime in seconds.
			RedisSerializer serializer = new StringRedisSerializer();
			List<Long> result = this.redisTemplate.execute(this.script, serializer, serializer, keys,
							replenishRate+"", burstCapacity+"", Instant.now().getEpochSecond() + "", "1");
			return result.get(0) == 1;

		} catch (Exception e) {

			log.error("Error determining if user allowed from redis", e);
		}
		return false;
	}
}
