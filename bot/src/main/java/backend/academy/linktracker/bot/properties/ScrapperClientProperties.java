package backend.academy.linktracker.bot.properties;

import jakarta.validation.constraints.NotEmpty;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.scrapper-client")
public class ScrapperClientProperties {
    @NotEmpty
    private String baseUrl;

    @NotEmpty
    private String grpcChannelName = "scrapper-channel";

    private Duration timeout = Duration.ofSeconds(10);
}
