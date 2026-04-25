package backend.academy.linktracker.scrapper.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.OffsetDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record GitHubEventResponse(String type, Actor actor, OffsetDateTime createdAt, Payload payload) {
    public record Actor(String login) {}

    public record Payload(String action, Issue issue, PullRequest pullRequest, Comment comment) {}

    public record Issue(String title, String body, String htmlUrl) {}

    public record PullRequest(String title, String body, String htmlUrl) {}

    public record Comment(String body) {}
}
