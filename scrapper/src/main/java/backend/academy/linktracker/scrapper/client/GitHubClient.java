package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.GitHubEventResponse;
import backend.academy.linktracker.scrapper.dto.GitHubResponse;
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
                    .log("Ошибка при вызове GitHub API");
            return Optional.empty();
        }
    }

    public List<GitHubEventResponse> fetchEvents(String owner, String repo) {
        try {
            GitHubEventResponse[] events = restClient
                    .get()
                    .uri("/repos/{owner}/{repo}/events", owner, repo)
                    .retrieve()
                    .body(GitHubEventResponse[].class);

            return events != null ? List.of(events) : List.of();

        } catch (Exception e) {
            log.atError()
                    .setCause(e)
                    .addKeyValue("owner", owner)
                    .addKeyValue("repo", repo)
                    .log("Ошибка при вызове GitHub API для получения событий (fetchEvents)");

            return List.of();
        }
    }
}
