package libedge.config.security;

import libedge.domain.exceptions.GenericException;
import libedge.repository.RoleRepository;
import lombok.Data;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yrw
 * @since 2018/2/18
 */
@Data
public class UserDetailsServiceImpl implements UserDetailsService {

	private RoleRepository roleRepository;

	public UserDetailsServiceImpl(RoleRepository roleRepository) {
		this.roleRepository = roleRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
		UserPermission user = new UserPermission();
		user.setId(id);
		try {
			user.setRoles(getRoles(id));
		} catch (GenericException e) {
			e.printStackTrace();
		}
		return user;
	}

	/**
	 * 得到该用户的权限集合
	 *
	 * @param id
	 * @return List<String>
	 */
	public List<String> getRoles(String id) throws GenericException {
		List<String> roleList = new ArrayList<>();
		roleList.addAll(roleRepository.getRoles(id));
		return roleList;
	}
}
