package backend.academy.linktracker.scrapper.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.OffsetDateTime;
import java.util.List;

public record StackOverflowCommentResponse(List<Comment> items) {

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Comment(Long commentId, String body, Owner owner, OffsetDateTime creationDate) {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Owner(String displayName) {}
}
