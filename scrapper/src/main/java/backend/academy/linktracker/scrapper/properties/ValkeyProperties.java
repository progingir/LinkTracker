package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.valkey", ignoreUnknownFields = false)
public record ValkeyProperties(
        boolean enabled,
        @NotNull Duration ttl,
        @NotNull Long maxSize,
        @NotEmpty List<String> nodes,
        @NotNull String invalidationTopic,
        @NotNull String password) {}
