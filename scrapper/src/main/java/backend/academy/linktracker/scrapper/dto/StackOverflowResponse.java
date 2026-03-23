package backend.academy.linktracker.scrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;

public record StackOverflowResponse(List<Item> items) {
    public record Item(
            @JsonProperty("question_id") Long questionId,
            @JsonProperty("last_activity_date") OffsetDateTime lastActivityDate,
            String title) {}
}
