package libedge.annotations;

import java.lang.annotation.*;

/**
 * @author yrw
 * @since 2018/2/19
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnablePenetration
public @interface EnableAuthentication {
}
