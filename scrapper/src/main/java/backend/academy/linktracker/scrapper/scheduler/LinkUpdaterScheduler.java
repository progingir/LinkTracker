package backend.academy.linktracker.scrapper.scheduler;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.LinkProcessingResult;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.properties.SchedulerProperties;
import backend.academy.linktracker.scrapper.service.LinkService;
import backend.academy.linktracker.scrapper.service.SubscriptionService;
import backend.academy.linktracker.scrapper.service.formatter.NotificationFormatter;
import backend.academy.linktracker.scrapper.service.update.LinkUpdateManager;
import backend.academy.linktracker.scrapper.service.update.UpdateSender;
import com.google.common.collect.Lists;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LinkUpdaterScheduler {

    private final LinkService linkService;
    private final SubscriptionService subscriptionService;
    private final LinkUpdateManager updateManager;
    private final NotificationFormatter formatter;
    private final UpdateSender updateSender;
    private final Executor executor;
    private final SchedulerProperties schedulerProperties;

    public LinkUpdaterScheduler(
            LinkService linkService,
            SubscriptionService subscriptionService,
            LinkUpdateManager updateManager,
            NotificationFormatter formatter,
            UpdateSender updateSender,
            @Qualifier("linkUpdaterExecutor") Executor executor,
            SchedulerProperties schedulerProperties) {
        this.linkService = linkService;
        this.subscriptionService = subscriptionService;
        this.updateManager = updateManager;
        this.formatter = formatter;
        this.updateSender = updateSender;
        this.executor = executor;
        this.schedulerProperties = schedulerProperties;
    }

    @Scheduled(fixedDelayString = "${app.scheduler.interval}")
    @SchedulerLock(name = "LinkUpdater_update", lockAtMostFor = "10m", lockAtLeastFor = "30s")
    public void update() {
        log.info("Начало фоновой проверки обновлений");
        int totalProcessed = 0;

        while (totalProcessed < schedulerProperties.maxLinksPerRun()) {
            List<Link> linksToCheck = linkService.findOldest(schedulerProperties.batchSize());

            if (linksToCheck.isEmpty()) {
                log.debug("Больше нет ссылок для проверки");
                break;
            }

            processBatch(linksToCheck);
            totalProcessed += linksToCheck.size();
        }

        log.info("Фоновая проверка завершена. Всего обработано ссылок: {}", totalProcessed);
    }

    private void processBatch(List<Link> linksToCheck) {
        ConcurrentMap<Long, List<String>> failedLinksByChat = new ConcurrentHashMap<>();
        ConcurrentMap<Long, OffsetDateTime> updatesToPersist = new ConcurrentHashMap<>();

        int threads = Math.max(1, schedulerProperties.threadsCount());
        int chunkSize = (int) Math.ceil((double) linksToCheck.size() / threads);
        List<List<Link>> chunks = Lists.partition(linksToCheck, Math.max(1, chunkSize));

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (List<Link> chunk : chunks) {
            futures.add(CompletableFuture.runAsync(
                    () -> processChunk(chunk, failedLinksByChat, updatesToPersist), executor));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        sendErrorReports(failedLinksByChat);

        if (!updatesToPersist.isEmpty()) {
            linkService.updateLastUpdateTimesBatch(updatesToPersist);
            log.debug("Пакетное обновление дат завершено для {} ссылок", updatesToPersist.size());
        }

        linkService.updateLastCheckTimeBatch(linksToCheck.stream().map(Link::id).toList(), OffsetDateTime.now());
    }

    private void processChunk(
            List<Link> chunk,
            ConcurrentMap<Long, List<String>> failedLinksByChat,
            ConcurrentMap<Long, OffsetDateTime> updatesToPersist) {
        for (Link link : chunk) {
            try {
                LinkProcessingResult result = updateManager.processLinkUpdate(link);

                result.errorUrl().ifPresent(url -> {
                    List<Long> chatIds = subscriptionService.getChatIdsByLinkId(link.id());
                    for (Long chatId : chatIds) {
                        failedLinksByChat
                                .computeIfAbsent(chatId, k -> new CopyOnWriteArrayList<>())
                                .add(url);
                    }
                });

                result.newUpdateTime().ifPresent(time -> updatesToPersist.put(link.id(), time));

            } catch (Exception e) {
                log.atError()
                        .addKeyValue("url", link.url())
                        .setCause(e)
                        .log("Критический сбой при обработке ссылки в потоке");
            }
        }
    }

    private void sendErrorReports(ConcurrentMap<Long, List<String>> failedLinksByChat) {
        failedLinksByChat.forEach((chatId, urls) -> {
            try {
                String reportText = formatter.formatErrorReport(urls);
                updateSender.sendUpdate(LinkUpdate.systemReport(reportText, List.of(chatId)));
            } catch (Exception e) {
                log.error("Не удалось отправить системный отчет в чат {}: {}", chatId, e.getMessage());
            }
        });
    }
}
