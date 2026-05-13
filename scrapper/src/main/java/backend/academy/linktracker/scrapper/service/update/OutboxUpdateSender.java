package backend.academy.linktracker.scrapper.service.update;

import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.entity.OutboxMessageEntity;
import backend.academy.linktracker.scrapper.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.use-queue", havingValue = "true", matchIfMissing = true)
public class OutboxUpdateSender implements UpdateSender {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void sendUpdate(LinkUpdate update) {
        try {
            String payload = objectMapper.writeValueAsString(update);

            OutboxMessageEntity entity = OutboxMessageEntity.builder()
                    .id(UUID.randomUUID())
                    .payload(payload)
                    .createdAt(OffsetDateTime.now())
                    .status(OutboxMessageEntity.OutboxStatus.PENDING)
                    .build();

            outboxRepository.save(entity);

            log.atInfo().addKeyValue("link_id", update.id()).log("Обновление для ссылки успешно сохранено в Outbox");

        } catch (Exception e) {
            log.atError()
                    .setCause(e)
                    .addKeyValue("link_id", update.id())
                    .log("Критическая ошибка при попытке сохранить обновление в Outbox");

            throw new RuntimeException("Ошибка работы Transactional Outbox", e);
        }
    }
}
