package ru.itmo.hub.service.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.quartz.QuartzJobBean;
import ru.itmo.hub.service.TagSuggestionService;

import java.time.Duration;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

@Configuration
public class DeleteOldTagSuggestions {
    @Bean("tagSuggestionsJobDetail")
    public JobDetail deleteOldTagSuggestionsJobDetail() {
        return JobBuilder.newJob()
                .ofType(DeleteOldTagSuggestionsJob.class)
                .storeDurably()
                .withIdentity("deleteOldTagSuggestionsJobDetail")
                .withDescription("Invoke deleting old tag suggestions")
                .build();
    }

    @Bean("tagSuggestionsDayTrigger")
    @DependsOn("tagSuggestionsJobDetail")
    public Trigger deleteOldTagSuggestionsDayTrigger(@Qualifier("tagSuggestionsJobDetail") JobDetail job) {
        return TriggerBuilder.newTrigger()
                .forJob(job)
                .withIdentity("tagSuggestionsDayTrigger")
                .withDescription("Tag suggestions trigger")
                .withSchedule(simpleSchedule().repeatForever().withIntervalInHours(24))
                .build();
    }

    @Slf4j
    @RequiredArgsConstructor
    public static class DeleteOldTagSuggestionsJob extends QuartzJobBean {
        private final TagSuggestionService tagSuggestionService;

        @Override
        protected void executeInternal(@NonNull JobExecutionContext context) {
            tagSuggestionService.deleteOldTagSuggestions(Duration.ofDays(7));
            log.info("Deleted old tag suggestions");
        }
    }
}
