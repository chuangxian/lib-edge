package myzd.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Authentication {

	enum Algorithm {
		HS256;
	}

	enum Mode {
		JWT, SESSION;
	}

	/**
	 * 用来做认证的方式
	 */
	Mode mode();

	/**
	 * 加密解密的算法
	 */
	Algorithm algorithm() default Algorithm.HS256;

	/**
	 * JWT加密的密钥的环境变量名
	 */
	String envEncryption() default "";

	/**
	 * JWT解密的密钥的环境变量名
	 */
	String envDecryption() default "";

}
