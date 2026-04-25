package backend.academy.linktracker.scrapper.service.update;

import backend.academy.linktracker.scrapper.dto.UpdateResult;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface LinkUpdateService {

    Optional<List<UpdateResult>> fetchUpdates(URI url, OffsetDateTime lastKnownUpdate);
}
