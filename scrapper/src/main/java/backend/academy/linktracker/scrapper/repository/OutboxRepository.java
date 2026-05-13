package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.entity.OutboxMessageEntity;
import java.util.List;

public interface OutboxRepository {

    OutboxMessageEntity save(OutboxMessageEntity entity);

    List<OutboxMessageEntity> findMessagesToProcess(int limit);

    void cleanup();
}
