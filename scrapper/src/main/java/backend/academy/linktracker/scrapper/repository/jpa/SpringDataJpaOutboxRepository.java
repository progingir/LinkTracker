package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.entity.OutboxMessageEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataJpaOutboxRepository extends JpaRepository<OutboxMessageEntity, UUID> {
    List<OutboxMessageEntity> findTop50ByStatusOrderByIdAsc(OutboxMessageEntity.OutboxStatus status);
}
