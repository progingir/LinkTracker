package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.entity.LinkEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataJpaLinkRepository extends JpaRepository<LinkEntity, Long> {

    Optional<LinkEntity> findByUrl(String url);

    List<LinkEntity> findAllByOrderByLastCheckAtAsc(Pageable pageable);

    @Modifying
    @Query("UPDATE LinkEntity l SET l.lastCheckAt = :lastCheck WHERE l.id = :id")
    void updateLastCheckAt(@Param("id") Long id, @Param("lastCheck") OffsetDateTime lastCheck);

    @Modifying
    @Query("UPDATE LinkEntity l SET l.lastUpdate = :lastUpdate WHERE l.id = :id")
    void updateLastUpdateTime(@Param("id") Long id, @Param("lastUpdate") OffsetDateTime lastUpdate);

    @Modifying
    @Query("DELETE FROM LinkEntity l WHERE l.id = :id")
    void deleteLinkById(@Param("id") Long id);
}
