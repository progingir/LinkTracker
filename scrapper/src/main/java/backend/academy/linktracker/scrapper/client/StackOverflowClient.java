package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.StackOverflowResponse;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class StackOverflowClient {
    private final RestClient restClient;

    public StackOverflowClient(RestClient stackoverflowRestClient) {
        this.restClient = stackoverflowRestClient;
    }

    public Optional<StackOverflowResponse.Item> fetchQuestion(Long questionId) {
        try {
            StackOverflowResponse response = restClient
                    .get()
                    .uri("/questions/{id}?site=stackoverflow", questionId)
                    .retrieve()
                    .body(StackOverflowResponse.class);

            if (response != null && !response.items().isEmpty()) {
                return Optional.of(response.items().getFirst());
            }
        } catch (Exception e) {
            log.atError().setCause(e).addKeyValue("question_id", questionId).log("Ошибка при вызове StackOverflow API");
        }
        return Optional.empty();
    }
}
