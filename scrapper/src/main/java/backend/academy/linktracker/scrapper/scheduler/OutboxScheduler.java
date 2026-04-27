package backend.academy.linktracker.scrapper.scheduler;

import backend.academy.linktracker.scrapper.entity.OutboxMessageEntity;
import backend.academy.linktracker.scrapper.repository.jpa.SpringDataJpaOutboxRepository;
import backend.academy.linktracker.scrapper.service.update.OutboxProcessor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private final SpringDataJpaOutboxRepository outboxRepository;
    private final OutboxProcessor outboxProcessor;

    @Scheduled(fixedDelayString = "${app.kafka.outbox-check-interval:5000}")
    @SchedulerLock(name = "processOutbox", lockAtLeastFor = "4s", lockAtMostFor = "10s")
    public void processOutbox() {
        List<OutboxMessageEntity> pendingMessages =
                outboxRepository.findTop50ByStatusOrderByIdAsc(OutboxMessageEntity.OutboxStatus.PENDING);

        if (pendingMessages.isEmpty()) {
            return;
        }

        log.atInfo().addKeyValue("count", pendingMessages.size()).log("Начата обработка ожидающих сообщений из Outbox");

        for (OutboxMessageEntity message : pendingMessages) {
            try {
                outboxProcessor.processSingleMessage(message);
            } catch (Exception e) {
                log.atError()
                        .setCause(e)
                        .addKeyValue("message_id", message.getId())
                        .log("Непредвиденная ошибка при обработке сообщения из Outbox");
            }
        }
    }
}
