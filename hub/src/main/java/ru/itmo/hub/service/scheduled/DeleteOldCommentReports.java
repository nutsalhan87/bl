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
import ru.itmo.hub.service.CommentReportService;

import java.time.Duration;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

@Configuration
public class DeleteOldCommentReports {
    @Bean("deleteOldReportsJobDetail")
    public JobDetail deleteOldReportsJobDetail() {
        return JobBuilder.newJob()
                .ofType(DeleteOldReportsJob.class)
                .storeDurably()
                .withIdentity("deleteOldReportsJobDetail")
                .withDescription("Invoke deleting old comment reports")
                .build();
    }

    @Bean("dayTrigger")
    @DependsOn("deleteOldReportsJobDetail")
    public Trigger dayTrigger(@Qualifier("deleteOldReportsJobDetail") JobDetail job) {
        return TriggerBuilder.newTrigger()
                .forJob(job)
                .withIdentity("dayTrigger")
                .withDescription("Sample trigger")
                .withSchedule(simpleSchedule().repeatForever().withIntervalInHours(24))
                .build();
    }

    @Slf4j
    @RequiredArgsConstructor
    public static class DeleteOldReportsJob extends QuartzJobBean {
        private final CommentReportService commentReportService;

        @Override
        protected void executeInternal(@NonNull JobExecutionContext context) {
            commentReportService.deleteOldReports(Duration.ofDays(7));
            log.info("Deleted old comment reports");
        }
    }
}
