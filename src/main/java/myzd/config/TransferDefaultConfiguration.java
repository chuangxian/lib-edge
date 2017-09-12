package myzd.config;

import myzd.domain.TransferAuditor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class TransferDefaultConfiguration {
  @Bean
  public TransferAuditor transferAuditor() {
    return new TransferAuditor();
  }
}
