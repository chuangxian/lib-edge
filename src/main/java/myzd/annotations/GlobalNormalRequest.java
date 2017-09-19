package myzd.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GlobalNormalRequest {
  /**
   * 透传服务的host的环境变量值, eg: ims.service.host.
   */
  String clientHost() default "";
}
