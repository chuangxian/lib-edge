package myzd.config.security;

import myzd.domain.UserPermission;
import myzd.mapper.AuthMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author yrw
 * 2/6/2018
 */
@Component
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private AuthMapper authMapper;

	@Value("${table.role.user}")
	private String roleUserTable;

	@Override
	public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
		UserPermission user = new UserPermission();
		user.setId(id);
		user.setRoles(getRoles(id));
		return user;
	}

	/**
	 * 得到该用户的权限集合
	 * @param id
	 * @return List<String>
	 */
	public List<String> getRoles(String id) {
		List<String> roleList = new ArrayList<>();
		Map<String, String> condition = new HashMap<>(4);
		condition.put("roleUserTable", roleUserTable);
		condition.put("id", id);
		roleList.addAll(authMapper.getRoles(condition));
		return roleList;
	}
}
