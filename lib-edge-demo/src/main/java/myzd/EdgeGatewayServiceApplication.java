package myzd;

import libedge.annotations.EnablePenetration;
import libedge.annotations.EnableRateLimit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * @author yrw
 * @since 2/10/2018
 */
@SpringBootApplication
@EntityScan(basePackageClasses = {EdgeGatewayServiceApplication.class})
@EnableAutoConfiguration(exclude = {
	DataSourceAutoConfiguration.class,
	DataSourceTransactionManagerAutoConfiguration.class,
	HibernateJpaAutoConfiguration.class
})
@EnablePenetration
@EnableRateLimit
public class EdgeGatewayServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(EdgeGatewayServiceApplication.class, args);
	}
}
