package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.GitHubResponse;
import backend.academy.linktracker.scrapper.properties.GithubProperties;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class GitHubClient {
    private final RestClient restClient;

    public GitHubClient(RestClient.Builder builder, GithubProperties properties) {
        this.restClient = builder.baseUrl(properties.getUrl()).build();
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
                .log("Ошибка при вызове GitHub API");
            return Optional.empty();
        }
    }
}
