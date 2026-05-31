package backend.academy.linktracker.scrapper.configuration;

import io.github.resilience4j.common.retry.configuration.RetryConfigCustomizer;
import io.github.resilience4j.core.IntervalFunction;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClientResponseException;

@Configuration
@ConfigurationProperties(prefix = "app.retry")
public class RetryConfiguration {

    private List<Integer> statuses;
    private Duration initialInterval;
    private Double multiplier;

    public List<Integer> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<Integer> statuses) {
        this.statuses = statuses;
    }

    public Duration getInitialInterval() {
        return initialInterval;
    }

    public void setInitialInterval(Duration initialInterval) {
        this.initialInterval = initialInterval;
    }

    public Double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
    }

    private boolean isRetryableException(Throwable throwable) {
        if (throwable instanceof IOException) return true;
        if (throwable instanceof TimeoutException) return true;
        if (statuses == null) return false;

        if (throwable instanceof RestClientResponseException e) {
            return statuses.contains(e.getStatusCode().value());
        }
        return false;
    }

    @Bean
    public RetryConfigCustomizer githubRetryCustomizer() {
        Predicate<Throwable> pred = this::isRetryableException;
        return RetryConfigCustomizer.of("github", builder -> {
            builder.retryOnException(pred);
            if (multiplier != null && multiplier > 1.0) {
                builder.intervalFunction(IntervalFunction.ofExponentialBackoff(initialInterval, multiplier));
            } else if (initialInterval != null) {
                builder.intervalFunction(IntervalFunction.of(initialInterval));
            }
        });
    }

    @Bean
    public RetryConfigCustomizer stackoverflowRetryCustomizer() {
        Predicate<Throwable> pred = this::isRetryableException;
        return RetryConfigCustomizer.of("stackoverflow", builder -> {
            builder.retryOnException(pred);
            if (multiplier != null && multiplier > 1.0) {
                builder.intervalFunction(IntervalFunction.ofExponentialBackoff(initialInterval, multiplier));
            } else if (initialInterval != null) {
                builder.intervalFunction(IntervalFunction.of(initialInterval));
            }
        });
    }
}
