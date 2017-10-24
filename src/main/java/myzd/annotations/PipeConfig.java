package myzd.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zks
 * 透传配置的注解
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

  /**
   * JWT加密的密钥的环境变量名
   */
  String envEncryption() default "";

  /**
   * JWT解密的密钥的环境变量名
   */
  String envDecryption() default "";
}
