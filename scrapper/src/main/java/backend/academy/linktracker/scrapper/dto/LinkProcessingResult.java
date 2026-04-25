package backend.academy.linktracker.scrapper.dto;

import java.time.OffsetDateTime;
import java.util.Optional;

public record LinkProcessingResult(Optional<String> errorUrl, Optional<OffsetDateTime> newUpdateTime) {}
