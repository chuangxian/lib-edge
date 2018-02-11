package myzd.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yrw
 * @since 2/10/2018
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Authentication {

	/**
	 * JWT加密的密钥的环境变量名
	 */
	String envEncryption() default "";

}
