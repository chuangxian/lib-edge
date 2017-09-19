package myzd.config;

import myzd.domain.TransferAuditor;
import myzd.domain.ValidateMessage;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;


@Configuration
public class TransferDefaultConfiguration {
  @Bean
  @Scope(scopeName = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
  public TransferAuditor transferAuditor() {
    return new TransferAuditor();
  }

  @Bean
  @Scope(scopeName = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
  public ValidateMessage validateMessage(){
    return new ValidateMessage();
  }

  @Bean
  public OkHttpClient okHttpClient() {
    return new OkHttpClient.Builder().build();
  }
}
