package libedge.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

/**
 * @author yrw
 * @since 2018/2/18
 */

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserDetailsServiceImpl userDetailsService;

	@Autowired
	public void configureAuthentication(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
		if (userDetailsService != null) {
			authenticationManagerBuilder
							// 设置UserDetailsService
							.userDetailsService(this.userDetailsService);
		}
	}

	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception {
		httpSecurity
						.csrf().disable()
						//禁用session
						.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
						.and()
						.authorizeRequests()
						.anyRequest().permitAll();

		// 禁用缓存
		httpSecurity.headers().cacheControl();
	}

	@Bean
	public AuthorizationTokenFilter authorizationTokenFilter() {
		return new AuthorizationTokenFilter();
	}
}
