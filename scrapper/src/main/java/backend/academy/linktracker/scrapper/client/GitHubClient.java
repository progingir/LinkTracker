package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.GitHubEventResponse;
import backend.academy.linktracker.scrapper.dto.GitHubIssueResponse;
import backend.academy.linktracker.scrapper.dto.GitHubResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@Retry(name = "github")
@CircuitBreaker(name = "github")
public class GitHubClient {
    private final RestClient restClient;

    public GitHubClient(RestClient githubRestClient) {
        this.restClient = githubRestClient;
    }

    public Optional<GitHubResponse> fetchRepository(String owner, String repo) {
        return Optional.ofNullable(restClient
                .get()
                .uri("/repos/{owner}/{repo}", owner, repo)
                .retrieve()
                .body(GitHubResponse.class));
    }

    public List<GitHubIssueResponse> fetchIssues(String owner, String repo, OffsetDateTime since) {
        GitHubIssueResponse[] issues = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/repos/{owner}/{repo}/issues")
                        .queryParam("since", since != null ? since.toString() : null)
                        .queryParam("state", "all")
                        .queryParam("sort", "created")
                        .queryParam("direction", "desc")
                        .build(owner, repo))
                .retrieve()
                .body(GitHubIssueResponse[].class);

        return issues != null ? List.of(issues) : List.of();
    }

    public List<GitHubEventResponse> fetchEvents(String owner, String repo) {
        var events = restClient
                .get()
                .uri("/repos/{owner}/{repo}/events", owner, repo)
                .retrieve()
                .body(GitHubEventResponse[].class);

        return events != null ? List.of(events) : List.of();
    }
}
