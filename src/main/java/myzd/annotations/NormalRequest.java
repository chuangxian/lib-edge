package myzd.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by admin on 2017/7/25.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NormalRequest {
  /**
   * 透传服务的host的环境变量值, eg: ims.service.host.
   * 默认为ims服务
   */
  String clientHost() default "";

  /**
   * 可选填的透传URL.
   * 不填写默认与edge的接口相同
   * 暂不支持包含PathParam的url.
   */
  String clientUrl() default "";
}
