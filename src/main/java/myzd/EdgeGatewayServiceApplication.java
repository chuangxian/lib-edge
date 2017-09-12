package myzd;

import io.prometheus.client.spring.boot.EnablePrometheusEndpoint;
import io.prometheus.client.spring.boot.EnableSpringBootMetricsCollector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EntityScan(basePackageClasses = {EdgeGatewayServiceApplication.class})
@EnableKafka
@EnableSpringBootMetricsCollector
@EnablePrometheusEndpoint
public class EdgeGatewayServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(EdgeGatewayServiceApplication.class, args);
  }
}
