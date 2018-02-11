package myzd.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


/**
 * @author yrw
 * 2/6/2018
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	/**
	 * 定义安全策略
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
						.authorizeRequests()
						.anyRequest().permitAll()
						.and()
						.csrf().disable();
	}

	/**
	 * 定义认证用户信息获取来源，密码校验规则等
	 */
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth
						// 设置UserDetailsService
						.userDetailsService(new UserDetailsServiceImpl());
	}

	@Bean
	public AuthorizationTokenFilter authenticationTokenFilterBean() throws Exception {
		return new AuthorizationTokenFilter();
	}
}
