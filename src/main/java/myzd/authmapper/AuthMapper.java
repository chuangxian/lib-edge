package myzd.authmapper;

import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.StatementType;
import org.mybatis.caches.redis.RedisCache;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author yrw
 * @since 2/7/2018
 */
@Mapper
@Repository
@CacheNamespace(implementation=RedisCache.class, flushInterval = 6000)
public interface AuthMapper {

	/**
	 * 从数据库中得到用户权限集合
	 *
	 * @param condition role和user的关系表的表名
	 * @return List<String>
	 */
	@Options(statementType = StatementType.STATEMENT)
	@Select("select role_id from ${roleUserTable} where user_id = '${id}'")
	public List<String> getRoles(Map<String, String> condition);
}
