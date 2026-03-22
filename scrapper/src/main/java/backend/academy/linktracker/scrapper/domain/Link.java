package backend.academy.linktracker.scrapper.domain;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

public record Link(
    Long id,
    Long chatId,
    URI url,
    List<String> tags,
    OffsetDateTime lastUpdate
) {}
