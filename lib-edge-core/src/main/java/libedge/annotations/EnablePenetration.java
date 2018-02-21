package libedge.annotations;

import libedge.config.LibEdgePipeConfiguration;
import libedge.config.security.WebSecurityConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author yrw
 * @since 2018/2/16
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({LibEdgePipeConfiguration.class})
@Documented
public @interface EnablePenetration {
}
