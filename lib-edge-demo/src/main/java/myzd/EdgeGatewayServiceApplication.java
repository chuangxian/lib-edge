package myzd;

import libedge.annotations.EnableAuthentication;
import libedge.annotations.EnableAuthorization;
import libedge.annotations.EnablePenetration;
import libedge.annotations.EnableRateLimit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SpringBootWebSecurityConfiguration;

/**
 * @author yrw
 * @since 2018/2/10
 */
@SpringBootApplication
@EntityScan(basePackageClasses = {EdgeGatewayServiceApplication.class})
@EnableAutoConfiguration(exclude = {
				DataSourceAutoConfiguration.class,
				DataSourceTransactionManagerAutoConfiguration.class,
				HibernateJpaAutoConfiguration.class
})
@EnableAuthentication
@EnableAuthorization
@EnablePenetration
@EnableRateLimit
public class EdgeGatewayServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(EdgeGatewayServiceApplication.class, args);
	}
}
