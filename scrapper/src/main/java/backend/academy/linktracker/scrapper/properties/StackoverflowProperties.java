package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.NotEmpty;
import java.time.Duration;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.stackoverflow")
@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class StackoverflowProperties {
    @NotEmpty
    @URL
    private String url;

    @NotEmpty
    private String key;

    @NotEmpty
    private String accessToken;

    private Duration timeout = Duration.ofSeconds(10);
}
