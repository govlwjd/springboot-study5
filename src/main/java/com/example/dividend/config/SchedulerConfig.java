package com.example.dividend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
public class SchedulerConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler threadPool = new ThreadPoolTaskScheduler();

        int n = Runtime.getRuntime().availableProcessors(); // CPU 코어 개수
        threadPool.setPoolSize(n); // n + 1 or n * 2
        threadPool.initialize();

        taskRegistrar.setTaskScheduler(threadPool); // 설정한 threadPool 을 스케줄러에서 사용한다.
    }
}
