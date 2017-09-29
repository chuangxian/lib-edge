package myzd.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FilterParam {
  /**
   * 控制过滤的开关，若为true，则过滤功能奏效
   *
   * @return boolean
   */
  boolean enable() default true;

  /**
   * 未包含的参数被过滤掉
   *
   * @return String
   */
  String[] include() default "";

  /**
   * 包含的参数被过滤掉
   *
   * @return String
   */
  String[] exclude() default "";
}
