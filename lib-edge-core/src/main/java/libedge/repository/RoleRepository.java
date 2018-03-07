package libedge.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author yrw
 * @since 2018/2/18
 */
public class RoleRepository {

	private JdbcTemplate jdbcTemplate;

	private RedisTemplate<String, String> redisTemplate;

	@Value("${authorization.sql}")
	private String sql;

	@Value("${authorization.expire}")
	private int expireTime;

	public RoleRepository(JdbcTemplate jdbcTemplate, RedisTemplate redisTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		this.redisTemplate = redisTemplate;
	}

	public List<String> getRoles(String id) {
		List<String> result = getRolesFromRedis(id);
		if (result != null && result.size() != 0) {
			return result;
		}
		result = getRolesFromDb(id);
		if (result == null || result.size() == 0) {
			return new ArrayList<>();
		}
		setRolesIntoRedis(id, result);
		return result;
	}

	private List<String> getRolesFromRedis(String id) {
		String result = redisTemplate.opsForValue().get("lib-edge-userAuthority:"+id);
		if (result == null) {
			return null;
		}
		String[] roles = result.split(",");
		List<String> list = new ArrayList<>();
		for (String role : roles) {
			list.add(role);
		}
		return list;
	}

	private List<String> getRolesFromDb(String id) {
		return jdbcTemplate.queryForList(sql, java.lang.String.class, id);
	}

	private void setRolesIntoRedis(String id, List<String> result) {
		StringBuilder value = new StringBuilder();
		for (String r : result) {
			value.append(r).append(",");
		}
		if (value.length() > 0) {
			redisTemplate.opsForValue().set("lib-edge-userAuthority:"+id, value.toString(), expireTime, TimeUnit.SECONDS);
		}
	}
}
