package libedge.annotations;

import libedge.config.LibEdgeRateLimitConfiguration;
import libedge.config.security.WebSecurityConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author yrw
 * @since 2018/2/16
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({LibEdgeRateLimitConfiguration.class})
@Documented
public @interface EnableRateLimit {
}
