package backend.academy.linktracker.scrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;

public record StackOverflowAnswerResponse(List<Answer> items) {
    public record Answer(
            @JsonProperty("creation_date") OffsetDateTime creationDate,
            Owner owner,
            @JsonProperty("body_markdown") String body) {}

    public record Owner(@JsonProperty("display_name") String displayName) {}
}
