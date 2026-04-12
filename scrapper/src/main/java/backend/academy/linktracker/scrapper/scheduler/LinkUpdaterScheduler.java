package backend.academy.linktracker.scrapper.scheduler;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.service.LinkService;
import backend.academy.linktracker.scrapper.service.LinkUpdateManager;
import backend.academy.linktracker.scrapper.service.NotificationFormatter;
import backend.academy.linktracker.scrapper.service.SubscriptionService;
import backend.academy.linktracker.scrapper.service.UpdateSender;
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
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.scheduler.batch-size:50}")
    private int batchSize;

    @Value("${app.scheduler.threads-count:4}")
    private int threadsCount;

    public LinkUpdaterScheduler(
            LinkService linkService,
            SubscriptionService subscriptionService,
            LinkUpdateManager updateManager,
            NotificationFormatter formatter,
            UpdateSender updateSender,
            @Qualifier("linkUpdaterExecutor") Executor executor) {
        this.linkService = linkService;
        this.subscriptionService = subscriptionService;
        this.updateManager = updateManager;
        this.formatter = formatter;
        this.updateSender = updateSender;
        this.executor = executor;
    }

    @Scheduled(fixedDelayString = "${app.scheduler.interval}")
    @SchedulerLock(name = "LinkUpdater_update", lockAtMostFor = "5m", lockAtLeastFor = "30s")
    public void update() {
        List<Link> linksToCheck = linkService.findOldest(batchSize);
        if (linksToCheck.isEmpty()) {
            return;
        }

        ConcurrentMap<Long, List<String>> failedLinksByChat = new ConcurrentHashMap<>();

        List<List<Link>> chunks = partition(linksToCheck, getChunkSize(linksToCheck.size(), threadsCount));
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (List<Link> chunk : chunks) {
            futures.add(CompletableFuture.runAsync(() -> processChunk(chunk, failedLinksByChat), executor));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        sendErrorReports(failedLinksByChat);

        linkService.updateLastCheckTimeBatch(linksToCheck.stream().map(Link::id).toList(), OffsetDateTime.now());

        log.atInfo().addKeyValue("batch_size", linksToCheck.size()).log("Проверка батча ссылок успешно завершена");
    }

    private void processChunk(List<Link> chunk, ConcurrentMap<Long, List<String>> failedLinksByChat) {
        for (Link link : chunk) {
            try {
                updateManager.processLinkUpdate(link).ifPresent(url -> {
                    List<Long> chatIds = subscriptionService.getChatIdsByLinkId(link.id());
                    for (Long chatId : chatIds) {
                        failedLinksByChat
                                .computeIfAbsent(chatId, k -> new CopyOnWriteArrayList<>())
                                .add(url);
                    }
                });
            } catch (Exception e) {
                log.atError()
                        .addKeyValue("url", link.url())
                        .addKeyValue("link_id", link.id())
                        .setCause(e)
                        .log("Критический сбой при обработке ссылки");
            }
        }
    }

    private void sendErrorReports(ConcurrentMap<Long, List<String>> failedLinksByChat) {
        failedLinksByChat.forEach((chatId, urls) -> {
            try {
                String reportText = formatter.formatErrorReport(urls);
                updateSender.sendUpdate(LinkUpdate.systemReport(reportText, List.of(chatId)));
            } catch (Exception e) {
                log.atError()
                        .addKeyValue("chat_id", chatId)
                        .addKeyValue("failed_links_count", urls.size())
                        .setCause(e)
                        .log("Не удалось отправить системный отчет в чат");
            }
        });
    }

    private <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        if (size <= 0) return partitions;
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(new ArrayList<>(list.subList(i, Math.min(i + size, list.size()))));
        }
        return partitions;
    }

    private int getChunkSize(int totalSize, int threads) {
        return (int) Math.ceil((double) totalSize / Math.max(1, threads));
    }
}
