package myzd.config.security;

import lombok.extern.slf4j.Slf4j;
import myzd.services.impl.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class AuthorizationTokenFilter extends OncePerRequestFilter {

	@Autowired
	private UserDetailsServiceImpl userDetailsService;

	@Autowired
	private JwtService jwtService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		log.debug("filter start");
		String auth = request.getHeader("Authorization");
		Map<String, String> userIdentityMap = new HashMap<>();
		if(auth != null) {
			log.debug("解析Authorization，得到role");
		}else {
			log.debug("从session里得到userIdentity");
			HttpSession httpSession = request.getSession();
			Enumeration<String> sessionAttributeNames = httpSession.getAttributeNames();
			for (Enumeration e = sessionAttributeNames; e.hasMoreElements(); ) {
				String key = e.nextElement().toString();
				String value = String.valueOf(httpSession.getAttribute(key));
				userIdentityMap.put(key, value);

				log.debug("filter:userIdentityMap: "+userIdentityMap);
			}
		}
		if(userIdentityMap.size() != 0) {

			UserDetails userDetails = userDetailsService.loadUserByUsername(userIdentityMap.get("username"));

			log.debug("配置用户权限");
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
			List<GrantedAuthority> list = (List<GrantedAuthority>) userDetails.getAuthorities();
			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		log.debug("filter over");
		filterChain.doFilter(request, response);
	}

}
