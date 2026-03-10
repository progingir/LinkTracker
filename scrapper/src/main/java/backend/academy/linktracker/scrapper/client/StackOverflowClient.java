package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.StackOverflowResponse;
import java.util.Optional;
import backend.academy.linktracker.scrapper.properties.StackoverflowProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class StackOverflowClient {
    private final RestClient restClient;

    public StackOverflowClient(RestClient.Builder builder, StackoverflowProperties properties) {
        this.restClient = builder.baseUrl(properties.getUrl()).build();
    }

    public Optional<StackOverflowResponse.Item> fetchQuestion(Long questionId) {
        try {
            StackOverflowResponse response = restClient.get()
                .uri("/questions/{id}?site=stackoverflow", questionId)
                .retrieve()
                .body(StackOverflowResponse.class);

            if (response != null && !response.items().isEmpty()) {
                return Optional.of(response.items().getFirst());
            }
        } catch (Exception e) {
            log.atError().setCause(e).log("Ошибка при вызове StackOverflow API");
        }
        return Optional.empty();
    }
}
