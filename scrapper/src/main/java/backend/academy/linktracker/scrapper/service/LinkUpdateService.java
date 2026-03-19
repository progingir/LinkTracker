package backend.academy.linktracker.scrapper.service;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Optional;

public interface LinkUpdateService {
    boolean supports(URI url);

    Optional<OffsetDateTime> fetchUpdateDate(URI url);

    String getUpdateDescription(URI url, OffsetDateTime updateDate);
}
