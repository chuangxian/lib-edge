package myzd.config;

import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


@Configuration
public class TransferDefaultConfiguration {
  @Bean
  public OkHttpClient okHttpClient() {
    return new OkHttpClient.Builder().build();
  }

  @Bean
  public TaskExecutor kafkaMsgExecutor(
    @Value("${executor.set.core.pool.size}")String corePoolSize,
    @Value("${executor.set.max.pool.size}")String maxPoolSize,
    @Value("${executor.set.queue.capacity}")String queueCapacity
    ) {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(Integer.parseInt(corePoolSize));
    executor.setMaxPoolSize(Integer.parseInt(maxPoolSize));
    executor.setQueueCapacity(Integer.parseInt(queueCapacity));
    return executor;
  }
}
