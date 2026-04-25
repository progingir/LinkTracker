package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.GitHubIssueResponse;
import backend.academy.linktracker.scrapper.dto.GitHubResponse;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class GitHubClient {
    private final RestClient restClient;

    public GitHubClient(RestClient githubRestClient) {
        this.restClient = githubRestClient;
    }

    public Optional<GitHubResponse> fetchRepository(String owner, String repo) {
        try {
            return Optional.ofNullable(restClient
                    .get()
                    .uri("/repos/{owner}/{repo}", owner, repo)
                    .retrieve()
                    .body(GitHubResponse.class));
        } catch (Exception e) {
            log.atError()
                    .setCause(e)
                    .addKeyValue("owner", owner)
                    .addKeyValue("repo", repo)
                    .log("Ошибка при вызове GitHub API (fetchRepository)");
            return Optional.empty();
        }
    }

    public List<GitHubIssueResponse> fetchIssues(String owner, String repo, OffsetDateTime since) {
        try {
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

        } catch (Exception e) {
            log.atError()
                    .setCause(e)
                    .addKeyValue("owner", owner)
                    .addKeyValue("repo", repo)
                    .log("Ошибка при вызове GitHub API для получения issues");

            return List.of();
        }
    }

    public List<backend.academy.linktracker.scrapper.dto.GitHubEventResponse> fetchEvents(String owner, String repo) {
        try {
            var events = restClient
                    .get()
                    .uri("/repos/{owner}/{repo}/events", owner, repo)
                    .retrieve()
                    .body(backend.academy.linktracker.scrapper.dto.GitHubEventResponse[].class);

            return events != null ? List.of(events) : List.of();
        } catch (Exception e) {
            log.error("Ошибка при получении событий: {}", e.getMessage());
            return List.of();
        }
    }
}
