package myzd.config.security;

import lombok.extern.slf4j.Slf4j;
import myzd.domain.exceptions.GenericException;
import myzd.services.impl.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yrw
 * @since 2/6/2018
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
		if (auth != null) {
			log.debug("解析Authorization，得到userIdentity");
			try {
				userIdentityMap = jwtService.decodeJwt(auth);

				//放进session里
				HttpSession session = request.getSession();
				for (String key : userIdentityMap.keySet()) {
					session.setAttribute(key, userIdentityMap.get(key));
				}
			} catch (GenericException e) {
			}
		} else {
			log.debug("从session里得到userIdentity");
			HttpSession httpSession = request.getSession();
			Enumeration<String> sessionAttributeNames = httpSession.getAttributeNames();
			for (Enumeration e = sessionAttributeNames; e.hasMoreElements(); ) {
				String key = e.nextElement().toString();
				String value = String.valueOf(httpSession.getAttribute(key));
				userIdentityMap.put(key, value);
			}
		}
		if (userIdentityMap.size() != 0) {

			log.debug("filter:userIdentityMap: " + userIdentityMap);

			UserDetails userDetails = userDetailsService.loadUserByUsername(userIdentityMap.get("uid"));

			log.debug("配置用户权限");
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		log.debug("filter over");
		filterChain.doFilter(request, response);
	}
}
