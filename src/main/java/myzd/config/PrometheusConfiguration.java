package myzd.config;

import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.DefaultExports;
import io.prometheus.client.spring.boot.SpringBootMetricsCollector;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

/**
 * Created by Chen Baichuan on 18/07/2017.
 */
@Configuration
public class PrometheusConfiguration {
  @Bean
  SpringBootMetricsCollector springBootMetricsCollector(Collection<PublicMetrics> publicMetrics) {

    SpringBootMetricsCollector springBootMetricsCollector = new SpringBootMetricsCollector(publicMetrics);
    springBootMetricsCollector.register();

    return springBootMetricsCollector;
  }

  @Bean
  ServletRegistrationBean servletRegistrationBean() {
    DefaultExports.initialize();
    return new ServletRegistrationBean(new MetricsServlet(), "/prometheus");
  }
}
