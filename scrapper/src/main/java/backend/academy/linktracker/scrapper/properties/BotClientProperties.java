package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.bot-client")
@Validated
@Getter
@Setter
public class BotClientProperties {

    @NotEmpty
    @URL
    private String baseUrl;

    @NotEmpty
    private String grpcChannelName = "bot-channel";

    private java.time.Duration deadline = java.time.Duration.ofSeconds(5);
}
