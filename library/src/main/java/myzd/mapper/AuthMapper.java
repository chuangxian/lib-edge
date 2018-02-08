package myzd.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author yrw
 */
@Mapper
@Repository
public interface AuthMapper {
	/**
	 * 从数据库中得到用户权限集合
	 *
	 * @param condition permissions, users, permission_user表名，username
	 * @return List<String>
	 */
	public List<String> getRoles(Map<String, String> condition);

}
