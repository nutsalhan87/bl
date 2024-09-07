package ru.itmo.hub.core.model.primary.videoreport;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface VideoReportRepository extends CrudRepository<VideoReport, Long> {
    boolean existsByVideoId(Long videoId);
    Optional<VideoReport> findByVideoId(Long videoId);
    @Query("SELECT vr.videoId FROM VideoReport AS vr")
    List<Long> getAllReportedVideoIds();
    void deleteByVideoId(Long videoId);
}
