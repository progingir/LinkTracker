package backend.academy.linktracker.scrapper.service.update;

import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.entity.OutboxMessageEntity;
import backend.academy.linktracker.scrapper.repository.OutboxRepository;
import backend.academy.linktracker.scrapper.repository.jpa.SpringDataJpaOutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxProcessor {

    private final OutboxRepository outboxRepository;
    private final KafkaUpdateSender kafkaSender;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSingleMessage(OutboxMessageEntity message) {
        try {
            LinkUpdate update = objectMapper.readValue(message.getPayload(), LinkUpdate.class);
            kafkaSender.sendUpdate(update);

            message.setStatus(OutboxMessageEntity.OutboxStatus.SENT);

            log.atDebug().addKeyValue("message_id", message.getId()).log("Сообщение успешно отправлено в Kafka");

        } catch (Exception e) {
            log.atError()
                    .setCause(e)
                    .addKeyValue("message_id", message.getId())
                    .log("Не удалось отправить сообщение из Outbox");

            int currentAttempts = (message.getAttempts() == null) ? 0 : message.getAttempts();
            message.setAttempts(currentAttempts + 1);

            if (message.getAttempts() >= 5) {
                message.setStatus(OutboxMessageEntity.OutboxStatus.FAILED);

                log.atWarn()
                        .addKeyValue("message_id", message.getId())
                        .log("Исчерпано количество попыток отправки сообщения. Статус изменен на FAILED.");
            }
        }
        outboxRepository.save(message);
    }
}
