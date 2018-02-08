package myzd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableWebSecurity
@EntityScan(basePackageClasses = {EdgeGatewayServiceApplication.class})
public class EdgeGatewayServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(EdgeGatewayServiceApplication.class, args);
  }
}
