package backend.academy.linktracker.scrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;

public record StackOverflowCommentResponse(List<Comment> items) {
    public record Comment(
            @JsonProperty("comment_id") Long commentId,
            @JsonProperty("body") String body,
            @JsonProperty("owner") Owner owner,
            @JsonProperty("creation_date") OffsetDateTime creationDate) {}

    public record Owner(@JsonProperty("display_name") String displayName) {}
}
