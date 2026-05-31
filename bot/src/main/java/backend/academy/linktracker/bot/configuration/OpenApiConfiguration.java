package backend.academy.linktracker.bot.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI botOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bot API")
                        .description("Service for interacting with users via Telegram")
                        .version("0.0.1"));
    }
}
