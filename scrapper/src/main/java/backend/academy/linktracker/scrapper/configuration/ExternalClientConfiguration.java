package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.properties.GithubProperties;
import backend.academy.linktracker.scrapper.properties.StackoverflowProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ExternalClientConfiguration {

    @Bean
    public RestClient githubRestClient(RestClient.Builder builder, GithubProperties properties) {
        return builder.baseUrl(properties.getUrl()).build();
    }

    @Bean
    public RestClient stackoverflowRestClient(RestClient.Builder builder, StackoverflowProperties properties) {
        return builder.baseUrl(properties.getUrl()).build();
    }
}
