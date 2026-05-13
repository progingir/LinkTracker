package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;


@Validated
@ConfigurationProperties(prefix = "app.kafka")
public record KafkaProperties(
    @NotBlank
    String topic,

    @Positive
    int partitions,

    @Positive
    int replicas,

    @NotBlank
    String minInsyncReplicas,

    @NotBlank
    String outboxCheckInterval,

    @NotBlank
    String outboxCleanupInterval
) {}
