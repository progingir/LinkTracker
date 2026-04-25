package backend.academy.linktracker.scrapper.service.update;

import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.constant.GithubConstants;
import backend.academy.linktracker.scrapper.dto.GitHubIssueResponse;
import backend.academy.linktracker.scrapper.dto.UpdateResult;
import backend.academy.linktracker.scrapper.service.parser.LinkParser;
import backend.academy.linktracker.scrapper.util.TextUtil;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GitHubLinkUpdateService implements LinkUpdateService {
    private final GitHubClient client;
    private final LinkParser parser;

    @Override
    public Optional<List<UpdateResult>> fetchUpdates(URI url, OffsetDateTime lastKnownUpdate) {
        var info = parser.parseGithub(url);
        if (info == null) {
            return Optional.empty();
        }

        List<GitHubIssueResponse> issues = client.fetchIssues(info.owner(), info.repo(), lastKnownUpdate);
        List<UpdateResult> results = new ArrayList<>();

        for (GitHubIssueResponse issue : issues) {
            if (lastKnownUpdate == null || issue.createdAt().isAfter(lastKnownUpdate)) {

                boolean isPR = issue.pullRequest() != null;
                String eventName = isPR ? GithubConstants.NEW_PR_TITLE : GithubConstants.NEW_ISSUE_TITLE;

                String safeAuthor = TextUtil.escapeMarkdown(issue.user().login());
                String safeTitle = TextUtil.escapeMarkdown(issue.title());

                String rawBody = issue.body();
                String preview = TextUtil.escapeMarkdown(TextUtil.truncate(rawBody == null ? "" : rawBody, 200));

                String description = String.format(
                        "**%s** в репозитории!%n Автор: %s%n Тема: %s%n Время: %s%n%n Превью:%n%s",
                        eventName, safeAuthor, safeTitle, issue.createdAt(), preview);

                results.add(new UpdateResult(issue.createdAt(), description));
            }
        }

        return Optional.of(results);
    }
}
