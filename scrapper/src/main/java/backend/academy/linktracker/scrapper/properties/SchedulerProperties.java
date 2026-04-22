package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.scheduler", ignoreUnknownFields = false)
public record SchedulerProperties(
        @Min(1) int batchSize,
        @Min(1) int threadsCount,
        @Min(1) int maxLinksPerRun,
        @Min(1) int errorThreshold,
        String interval) {}
