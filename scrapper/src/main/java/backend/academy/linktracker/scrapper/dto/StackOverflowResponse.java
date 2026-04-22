package backend.academy.linktracker.scrapper.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.OffsetDateTime;
import java.util.List;

public record StackOverflowResponse(List<Item> items) {

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Item(Long questionId, OffsetDateTime lastActivityDate, String title) {}
}
