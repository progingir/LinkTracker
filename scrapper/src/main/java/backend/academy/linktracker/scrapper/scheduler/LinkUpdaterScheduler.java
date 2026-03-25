package backend.academy.linktracker.scrapper.scheduler;

import backend.academy.linktracker.scrapper.client.BotNotificationClient;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import backend.academy.linktracker.scrapper.service.LinkUpdateService;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinkUpdaterScheduler {

    private final LinkRepository linkRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final List<LinkUpdateService> updateServices;
    private final BotNotificationClient botNotificationClient;

    @Value("${app.scheduler.batch-size}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${app.scheduler.interval}")
    public void update() {
        log.atInfo()
            .addKeyValue("batch_size", batchSize)
            .log("Начало фоновой проверки обновлений");

        List<Link> linksToCheck = linkRepository.findOldest(batchSize);

        if (linksToCheck.isEmpty()) {
            return;
        }

        for (Link link : linksToCheck) {
            try {
                processSingleLink(link);
            } catch (Exception e) {
                log.atError()
                    .setCause(e)
                    .addKeyValue("link_id", link.id())
                    .addKeyValue("link", link.url())
                    .log("Ошибка при обработке ссылки");
            } finally {
                linkRepository.updateLastCheckTime(link.id(), OffsetDateTime.now());
            }
        }
    }

    private void processSingleLink(Link link) {
        updateServices.stream()
            .filter(service -> service.supports(link.url()))
            .findFirst()
            .ifPresent(service -> {
                service.fetchUpdateDate(link.url()).ifPresent(externalDate -> {

                    if (externalDate.isAfter(link.lastUpdate())) {

                        List<Long> chatIds = subscriptionRepository.findChatIdsByLinkId(link.id());

                        if (!chatIds.isEmpty()) {
                            log.atInfo()
                                .addKeyValue("link_id", link.id())
                                .addKeyValue("link", link.url())
                                .addKeyValue("chats_count", chatIds.size())
                                .log("Найдено обновление для ссылки, уведомляю чаты");

                            String description = service.getUpdateDescription(link.url(), externalDate);
                            notifyBot(link.id(), link.url(), description, chatIds);

                            linkRepository.updateLastUpdateTime(link.id(), externalDate);
                        }
                    }
                });
            });
    }

    private void notifyBot(Long linkId, URI url, String description, List<Long> chatIds) {
        botNotificationClient.sendUpdate(new LinkUpdate(linkId, url, description, chatIds));
    }
}
