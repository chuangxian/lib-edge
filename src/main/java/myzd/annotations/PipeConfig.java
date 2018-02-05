package myzd.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zks
 * 透传配置的注解
 * 环境 demo 应用本身 测试环境
 * http协议 web容器做法 身份验证
 * 某个接口 返回多个接口数据，拼装起来
 * data:{
 *   userInfo:{code: data:{}}
 *   config:{code: data:{}}
 *   code:
 *   .....
 * }
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PipeConfig {
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
