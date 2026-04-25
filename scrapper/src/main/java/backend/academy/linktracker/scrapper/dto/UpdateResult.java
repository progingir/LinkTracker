package backend.academy.linktracker.scrapper.dto;

import java.time.OffsetDateTime;

public record UpdateResult(OffsetDateTime updateDate, String description) {}
