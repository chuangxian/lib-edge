package myzd.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by zks on 2017/9/6.
 * 接口流量控制注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FlowControl {

	/**
	 * 一秒钟内可访问次数
	 */
	int secondTimes() default -1;

	/**
	 * 一分钟内可访问次数
	 */
	int minuteTimes() default -1;

	/**
	 * 一小时内可访问次数
	 */
	int hourTimes() default -1;

	/**
	 * 一天内可访问次数
	 */
	int dayTimes() default -1;

}
