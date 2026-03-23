package backend.academy.linktracker.bot.properties;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.grpc")
public class GrpcScrapperProperties {

    @NotNull
    private Duration scrapperDeadline = Duration.ofSeconds(5);
}
