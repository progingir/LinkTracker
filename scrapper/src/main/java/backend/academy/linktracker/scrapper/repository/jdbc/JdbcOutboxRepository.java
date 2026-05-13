package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.entity.OutboxMessageEntity;
import backend.academy.linktracker.scrapper.repository.OutboxRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;

public class JdbcOutboxRepository implements OutboxRepository {

    private final JdbcClient jdbcClient;

    public JdbcOutboxRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public OutboxMessageEntity save(OutboxMessageEntity entity) {
        jdbcClient
                .sql("INSERT INTO outbox_messages (id, payload, created_at, status, attempts) VALUES (?, ?, ?, ?, ?)")
                .param(1, entity.getId())
                .param(2, entity.getPayload())
                .param(3, entity.getCreatedAt())
                .param(4, entity.getStatus().name())
                .param(5, entity.getAttempts())
                .update();
        return entity;
    }

    @Override
    public List<OutboxMessageEntity> findMessagesToProcess(int limit) {
        return jdbcClient
                .sql(
                        "SELECT id, payload, created_at, status, attempts FROM outbox_messages WHERE status = 'PENDING' OR status = 'FAILED' ORDER BY created_at ASC LIMIT ?")
                .param(1, limit)
                .query((rs, rowNum) -> OutboxMessageEntity.builder()
                        .id(rs.getObject("id", UUID.class))
                        .payload(rs.getString("payload"))
                        .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                        .status(OutboxMessageEntity.OutboxStatus.valueOf(rs.getString("status")))
                        .attempts(rs.getInt("attempts"))
                        .build())
                .list();
    }

    @Override
    public void cleanup() {
        jdbcClient.sql("DELETE FROM outbox_messages WHERE status = 'SENT'").update();
    }
}
