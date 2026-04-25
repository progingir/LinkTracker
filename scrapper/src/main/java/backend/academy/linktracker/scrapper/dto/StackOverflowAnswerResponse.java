package backend.academy.linktracker.scrapper.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.OffsetDateTime;
import java.util.List;

public record StackOverflowAnswerResponse(List<Answer> items) {

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Answer(OffsetDateTime creationDate, Owner owner, String bodyMarkdown) {}

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Owner(String displayName) {}
}
