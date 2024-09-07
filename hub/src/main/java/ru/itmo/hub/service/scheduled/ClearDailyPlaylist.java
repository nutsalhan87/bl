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
import ru.itmo.hub.service.PlaylistService;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;


@Configuration
public class ClearDailyPlaylist {
    @Bean("clearDailyPlaylistJobDetail")
    public JobDetail clearDailyPlaylistJobDetail() {
        return JobBuilder.newJob()
                .ofType(ClearDailyPlaylist.ClearDailyPlaylistJob.class)
                .storeDurably()
                .withIdentity("clearDailyPlaylistJobDetail")
                .withDescription("Invoke clearing daily playlist")
                .build();
    }

    @Bean("clearDailyPlaylistDayTrigger")
    @DependsOn("clearDailyPlaylistJobDetail")
    public Trigger clearDailyPlaylistDayTrigger(@Qualifier("clearDailyPlaylistJobDetail") JobDetail job) {
        return TriggerBuilder.newTrigger()
                .forJob(job)
                .withIdentity("clearDailyPlaylistDayTrigger")
                .withDescription("Playlist trigger")
                .withSchedule(simpleSchedule().repeatForever().withIntervalInHours(24))
                .build();
    }

    @Slf4j
    @RequiredArgsConstructor
    public static class ClearDailyPlaylistJob extends QuartzJobBean {
        private final PlaylistService playlistService;

        @Override
        protected void executeInternal(@NonNull JobExecutionContext context) {
            playlistService.clearDailyPlaylist();
            log.info("Cleared daily playlist!");
        }
    }
}
