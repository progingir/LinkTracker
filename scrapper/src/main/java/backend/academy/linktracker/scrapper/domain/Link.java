package backend.academy.linktracker.scrapper.domain;

import java.net.URI;
import java.time.OffsetDateTime;

public record Link(Long id, URI url, OffsetDateTime lastUpdate, OffsetDateTime lastCheckAt, int errorCount) {}
