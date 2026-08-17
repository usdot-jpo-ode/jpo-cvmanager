package us.dot.its.jpo.rsustatusmonitor.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // number of core threads
        executor.setMaxPoolSize(10); // maximum allowed threads
        executor.setQueueCapacity(1000); // queue before rejecting
        executor.setThreadNamePrefix("async-exec-");
        executor.initialize();
        return executor;
    }
}