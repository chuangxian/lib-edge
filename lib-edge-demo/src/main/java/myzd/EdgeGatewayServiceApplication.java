package myzd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * @author yrw
 * @since 2018/2/10
 */
@SpringBootApplication(exclude = {
				DataSourceAutoConfiguration.class,
				DataSourceTransactionManagerAutoConfiguration.class,
				HibernateJpaAutoConfiguration.class
})
@EntityScan(basePackageClasses = {EdgeGatewayServiceApplication.class})
public class EdgeGatewayServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(EdgeGatewayServiceApplication.class, args);
	}
}
