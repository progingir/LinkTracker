package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.domain.Link;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface LinkRepository {
    Link save(URI url);

    Optional<Link> findByUrl(URI url);

    Optional<Link> findById(Long id);

    void remove(Long id);

    List<Link> findOldest(int limit);

    void updateLastCheckTime(Long linkId, OffsetDateTime lastCheck);
    void updateLastUpdateTime(Long linkId, OffsetDateTime lastUpdate);
}
