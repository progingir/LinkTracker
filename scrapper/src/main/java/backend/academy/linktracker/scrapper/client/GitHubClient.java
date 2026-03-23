package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.GitHubResponse;
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
}
