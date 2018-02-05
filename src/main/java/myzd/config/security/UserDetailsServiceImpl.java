package myzd.config.security;

import myzd.domain.UserPermission;
import myzd.mapper.AuthMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class UserDetailsServiceImpl implements UserDetailsService{

	@Autowired
	private AuthMapper authMapper;

	@Value("${table.permissions}")
	private String permissionsTableName;

	@Value("${table.users}")
	private String usersTableName;

	@Value("${table.permission.user.relation}")
	private String permissionUserTableName;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserPermission user = new UserPermission();
		user.setUsername(username);
		user.setAuthority(getAuthorization(username));
		return user;
	}

	//得到该用户的权限集合
	public List<String> getAuthorization(String username) {
		List<String> authList = new ArrayList<>();
		Map<String, String> condition = new HashMap<>();
		condition.put("permissionsTable", permissionsTableName);
		condition.put("usersTable", usersTableName);
		condition.put("permissionUserTable", permissionUserTableName);
		condition.put("username", username);
		authList.addAll(authMapper.getAuthorizaion(condition));
		return authList;
	}
}
