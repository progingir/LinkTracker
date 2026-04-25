package backend.academy.linktracker.scrapper.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.OffsetDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record GitHubIssueResponse(
        Long id,
        String title,
        String body,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        User user,
        Object pullRequest) {
    public record User(String login) {}
}
