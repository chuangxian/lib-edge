package myzd.config;

import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


/**
 * @author
 */
@Configuration
public class TransferDefaultConfiguration {

	private final static String EXECUTOR_SET_CORE_POOL_SIZE = "executor.set.core.pool.size";
	private final static String EXECUTOR_SET_MAX_POOL_SIZE = "executor.set.max.pool.size";
	private final static String EXECUTOR_SET_QUEUE_CAPACITY = "executor.set.queue.capacity";
	@Autowired
	private Environment environment;

	@Bean
	public OkHttpClient okHttpClient() {
		return new OkHttpClient.Builder()
						.followRedirects(false)
						.followSslRedirects(false)
						.build();
	}

	@Bean
	public TaskExecutor kafkaMsgExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		int corePoolSize = StringUtils.isBlank(environment.getProperty(EXECUTOR_SET_CORE_POOL_SIZE)) ? 5 : Integer.parseInt(environment.getProperty(EXECUTOR_SET_CORE_POOL_SIZE));
		int maxPoolSize = StringUtils.isBlank(environment.getProperty(EXECUTOR_SET_MAX_POOL_SIZE)) ? 10 : Integer.parseInt(environment.getProperty(EXECUTOR_SET_MAX_POOL_SIZE));
		int queueCapacity = StringUtils.isBlank(environment.getProperty(EXECUTOR_SET_QUEUE_CAPACITY)) ? 25 : Integer.parseInt(environment.getProperty(EXECUTOR_SET_QUEUE_CAPACITY));
		executor.setCorePoolSize(corePoolSize);
		executor.setMaxPoolSize(maxPoolSize);
		executor.setQueueCapacity(queueCapacity);
		return executor;
	}
}
