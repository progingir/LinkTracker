package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.entity.OutboxMessageEntity;
import backend.academy.linktracker.scrapper.repository.OutboxRepository;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class JpaOutboxRepository implements OutboxRepository {

    private final SpringDataJpaOutboxRepository jpaRepository;

    @Override
    public OutboxMessageEntity save(OutboxMessageEntity entity) {
        return jpaRepository.saveAndFlush(entity);
    }

    @Override
    public List<OutboxMessageEntity> findMessagesToProcess(int limit) {
        return jpaRepository.findTop50ByStatusOrderByIdAsc(OutboxMessageEntity.OutboxStatus.PENDING);
    }

    @Override
    @Transactional
    public void cleanup() {
        jpaRepository.deleteAllByStatusIn(Set.of(
            OutboxMessageEntity.OutboxStatus.SENT,
            OutboxMessageEntity.OutboxStatus.FAILED
        ));
    }
}
