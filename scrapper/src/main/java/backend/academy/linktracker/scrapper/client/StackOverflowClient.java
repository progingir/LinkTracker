package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.StackOverflowAnswerResponse;
import backend.academy.linktracker.scrapper.dto.StackOverflowCommentResponse;
import backend.academy.linktracker.scrapper.dto.StackOverflowResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@Retry(name = "stackoverflow")
@CircuitBreaker(name = "stackoverflow")
public class StackOverflowClient {
    private final RestClient restClient;

    public StackOverflowClient(RestClient stackoverflowRestClient) {
        this.restClient = stackoverflowRestClient;
    }

    public Optional<StackOverflowResponse.Item> fetchQuestion(Long questionId) {
        StackOverflowResponse response = restClient
                .get()
                .uri("/questions/{id}?site=stackoverflow", questionId)
                .retrieve()
                .body(StackOverflowResponse.class);

        if (response != null && response.items() != null && !response.items().isEmpty()) {
            return Optional.of(response.items().getFirst());
        }
        return Optional.empty();
    }

    public StackOverflowAnswerResponse fetchAnswers(Long questionId, OffsetDateTime fromDate) {
        return restClient
                .get()
                .uri(uriBuilder -> {
                    uriBuilder
                            .path("/questions/{id}/answers")
                            .queryParam("site", "stackoverflow")
                            .queryParam("filter", "withbody");

                    if (fromDate != null) {
                        uriBuilder.queryParam("fromdate", fromDate.toEpochSecond());
                    }

                    return uriBuilder.build(questionId);
                })
                .retrieve()
                .body(StackOverflowAnswerResponse.class);
    }

    public StackOverflowCommentResponse fetchComments(Long questionId, OffsetDateTime fromDate) {
        return restClient
                .get()
                .uri(uriBuilder -> {
                    uriBuilder
                            .path("/questions/{id}/comments")
                            .queryParam("site", "stackoverflow")
                            .queryParam("filter", "withbody");

                    if (fromDate != null) {
                        uriBuilder.queryParam("fromdate", fromDate.toEpochSecond());
                    }

                    return uriBuilder.build(questionId);
                })
                .retrieve()
                .body(StackOverflowCommentResponse.class);
    }
}
