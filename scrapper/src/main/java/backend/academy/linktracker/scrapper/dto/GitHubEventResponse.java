package backend.academy.linktracker.scrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record GitHubEventResponse(
        String type,
        Actor actor,
        @JsonProperty("created_at") OffsetDateTime createdAt,
        Payload payload) {
    public record Actor(String login) {}

    public record Payload(String action, Issue issue, PullRequest pullRequest) {}

    public record Issue(String title, String body) {}

    public record PullRequest(String title, String body) {}
}
