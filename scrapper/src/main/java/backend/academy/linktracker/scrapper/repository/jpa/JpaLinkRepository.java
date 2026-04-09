package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.entity.LinkEntity;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;

@RequiredArgsConstructor
public class JpaLinkRepository implements LinkRepository {

    private static final OffsetDateTime NEVER_CHECKED = OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);

    private final SpringDataJpaLinkRepository jpaRepository;

    @Override
    public Link save(URI url) {
        LinkEntity entity = new LinkEntity();
        entity.setUrl(url.toString());
        entity.setLastUpdate(OffsetDateTime.now());
        entity.setLastCheckAt(NEVER_CHECKED);

        entity = jpaRepository.saveAndFlush(entity);

        return mapToDomain(entity);
    }

    @Override
    public Optional<Link> findByUrl(URI url) {
        return jpaRepository.findByUrl(url.toString()).map(this::mapToDomain);
    }

    @Override
    public Optional<Link> findById(Long id) {
        return jpaRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public void remove(Long id) {
        jpaRepository.deleteLinkById(id);
    }

    @Override
    public List<Link> findOldest(int limit) {
        return jpaRepository.findAllByOrderByLastCheckAtAsc(PageRequest.of(0, limit)).stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public void updateLastCheckTime(Long linkId, OffsetDateTime lastCheck) {
        jpaRepository.updateLastCheckAt(linkId, lastCheck);
    }

    @Override
    public void updateLastUpdateTime(Long linkId, OffsetDateTime lastUpdate) {
        jpaRepository.updateLastUpdateTime(linkId, lastUpdate);
    }

    private Link mapToDomain(LinkEntity entity) {
        return new Link(entity.getId(), URI.create(entity.getUrl()), entity.getLastUpdate(), entity.getLastCheckAt());
    }

    @Override
    public void updateLastCheckTimeBatch(List<Long> ids, OffsetDateTime lastCheck) {
        jpaRepository.updateLastCheckAtBatch(ids, lastCheck);
    }
}
