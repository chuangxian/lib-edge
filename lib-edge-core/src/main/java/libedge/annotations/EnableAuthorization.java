package libedge.annotations;

import libedge.config.LibEdgeAuthorizationConfiguration;
import libedge.config.security.AuthorizationTokenFilter;
import libedge.config.security.WebSecurityConfig;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import java.lang.annotation.*;

/**
 * @author yrw
 * @since 2018/2/18
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableWebSecurity
@Import(value = {LibEdgeAuthorizationConfiguration.class,
				AuthorizationTokenFilter.class})
public @interface EnableAuthorization {
}
