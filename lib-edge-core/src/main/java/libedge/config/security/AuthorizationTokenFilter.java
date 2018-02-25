package libedge.config.security;

import libedge.domain.exceptions.GenericException;
import libedge.services.impl.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yrw
 * @since 2018/2/6
 */
@Slf4j
public class AuthorizationTokenFilter extends OncePerRequestFilter {

	@Autowired
	private UserDetailsServiceImpl userDetailsService;

	@Autowired
	private JwtService jwtService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
					throws ServletException, IOException {
		log.debug("filter start");
		String auth = request.getHeader("Authorization");
		Map<String, String> userIdentityMap = new HashMap<>(16);

		if (auth != null && auth.startsWith("Bearer ")) {
			log.debug("解析Authorization，得到userIdentity");
			try {
				userIdentityMap = jwtService.decodeJwt(auth);
				if (userIdentityMap.size() == 0) {
					throw new GenericException("1212121", "decode failed!");
				}
			} catch (GenericException e) {
			}

			//存入内存
			HttpSession session = request.getSession();
			for (String key : userIdentityMap.keySet()) {
				session.setAttribute(key, userIdentityMap.get(key));
			}

			log.debug("filter:userIdentityMap: " + userIdentityMap);
			if (userDetailsService != null && userIdentityMap.get("uid") != null) {
				UserDetails userDetails = userDetailsService.loadUserByUsername(userIdentityMap.get("uid"));

				log.debug("配置用户权限");
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		}
		log.debug("filter over");
		filterChain.doFilter(request, response);
	}
}
