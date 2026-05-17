package backend.academy.linktracker.scrapper.scheduler;

import backend.academy.linktracker.scrapper.entity.OutboxMessageEntity;
import backend.academy.linktracker.scrapper.repository.OutboxRepository;
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

    private final OutboxRepository outboxRepository;
    private final OutboxProcessor outboxProcessor;

    @Scheduled(fixedDelayString = "${app.kafka.outbox-check-interval:5000}")
    @SchedulerLock(name = "processOutbox", lockAtLeastFor = "4s", lockAtMostFor = "10s")
    public void processOutbox() {
        List<OutboxMessageEntity> pendingMessages = outboxRepository.findMessagesToProcess(50);
        if (pendingMessages.isEmpty()) return;

        log.atInfo().addKeyValue("count", pendingMessages.size()).log("Обработка Outbox");
        for (OutboxMessageEntity message : pendingMessages) {
            try {
                outboxProcessor.processSingleMessage(message);
            } catch (Exception e) {
                log.atError().setCause(e).log("Ошибка обработки сообщения");
            }
        }
    }

    @Scheduled(fixedDelayString = "${app.kafka.outbox-cleanup-interval:3600000}")
    @SchedulerLock(name = "cleanupOutbox", lockAtLeastFor = "1m", lockAtMostFor = "5m")
    public void cleanupOutbox() {
        log.atInfo().log("Запуск очистки обработанных сообщений Outbox");
        try {
            outboxRepository.cleanup();
            log.atInfo().log("Очистка Outbox завершена успешно");
        } catch (Exception e) {
            log.atError().setCause(e).log("Ошибка при очистке Outbox");
        }
    }
}
