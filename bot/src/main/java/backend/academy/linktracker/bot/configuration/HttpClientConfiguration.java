package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.exception.ScrapperApiException;
import backend.academy.linktracker.bot.properties.ScrapperClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientConfiguration {

    @Bean
    public RestClient scrapperRestClient(RestClient.Builder builder, ScrapperClientProperties properties) {
        var factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(properties.getTimeout());
        return builder.requestFactory(factory)
                .baseUrl(properties.getBaseUrl())
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    throw new ScrapperApiException(
                            response.getStatusCode().value(), "Ошибка API Scrapper: " + response.getStatusCode());
                })
                .build();
    }
}
