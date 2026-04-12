package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.dto.UpdateResult;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

public interface LinkUpdateService {

    boolean supports(URI url);

    List<UpdateResult> fetchUpdates(URI url, OffsetDateTime lastKnownUpdate);
}
