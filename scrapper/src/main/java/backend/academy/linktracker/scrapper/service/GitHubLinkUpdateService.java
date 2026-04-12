package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.dto.GitHubEventResponse;
import backend.academy.linktracker.scrapper.dto.UpdateResult;
import backend.academy.linktracker.scrapper.util.TextUtil;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
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
    public List<UpdateResult> fetchUpdates(URI url, OffsetDateTime lastKnownUpdate) {
        var info = parser.parseGithub(url);
        List<GitHubEventResponse> events = client.fetchEvents(info.owner(), info.repo());
        List<UpdateResult> results = new ArrayList<>();

        for (GitHubEventResponse event : events) {
            if (lastKnownUpdate == null || event.createdAt().isAfter(lastKnownUpdate)) {
                boolean isIssue =
                        "IssuesEvent".equals(event.type()) && event.payload().issue() != null;
                boolean isPR = "PullRequestEvent".equals(event.type())
                        && event.payload().pullRequest() != null;

                if ((isIssue || isPR) && "opened".equals(event.payload().action())) {
                    String safeAuthor = TextUtil.escapeMarkdown(event.actor().login());
                    String rawTitle = isIssue
                            ? event.payload().issue().title()
                            : event.payload().pullRequest().title();
                    String safeTitle = TextUtil.escapeMarkdown(rawTitle);

                    String rawBody = isIssue
                            ? event.payload().issue().body()
                            : event.payload().pullRequest().body();
                    String preview = TextUtil.escapeMarkdown(TextUtil.truncate(rawBody == null ? "" : rawBody, 200));

                    String eventName = isIssue ? "Новый Issue" : "Новый Pull Request";

                    String description = String.format(
                            "🛠 **%s** в репозитории!%n👤 Автор: %s%n📝 Тема: %s%n⏱ Время: %s%n%n📄 Превью:%n%s",
                            eventName, safeAuthor, safeTitle, event.createdAt(), preview);

                    results.add(new UpdateResult(event.createdAt(), description));
                }
            }
        }

        return results;
    }
}
