package ru.itmo.hub;

import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerConfigException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.format.FormatterRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.quartz.SimpleThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.itmo.hub.api.dto.CommentSortingStrategy;

@Slf4j
@SpringBootApplication
public class BlApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlApplication.class, args);
    }

    @Bean("taskExecutor")
    public TaskExecutor taskExecutor() throws SchedulerConfigException {
        var taskExecutor = new SimpleThreadPoolTaskExecutor();
        taskExecutor.setThreadCount(16);
        taskExecutor.initialize();
        return taskExecutor;
    }

    @Bean
    public TaskScheduler taskScheduler() {
        var taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(16);
        taskScheduler.setErrorHandler(t -> log.error(t.getMessage()));
        taskScheduler.initialize();
        return taskScheduler;
    }

    @Configuration
    static class WebConfig implements WebMvcConfigurer {
        @Override
        public void addFormatters(FormatterRegistry registry) {
            registry.addConverter(new CommentSortingStrategy.StringToEnumConverter());
        }
    }
}
