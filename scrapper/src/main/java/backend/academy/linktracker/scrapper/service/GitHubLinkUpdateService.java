package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.dto.GitHubResponse;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GitHubLinkUpdateService implements LinkUpdateService {
    private final GitHubClient client;
    private final LinkParser parser;

    @Override
    public boolean supports(URI url) {
        return parser.parseGithub(url) != null;
    }

    @Override
    public Optional<OffsetDateTime> fetchUpdateDate(URI url) {
        var info = parser.parseGithub(url);
        return client.fetchRepository(info.owner(), info.repo()).map(GitHubResponse::updatedAt);
    }

    @Override
    public String getUpdateDescription(URI url, OffsetDateTime date) {
        return "Обнаружена новая активность в GitHub репозитории!";
    }
}
