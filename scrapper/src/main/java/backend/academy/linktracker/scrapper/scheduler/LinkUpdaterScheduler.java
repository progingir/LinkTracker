package backend.academy.linktracker.scrapper.scheduler;

import backend.academy.linktracker.scrapper.client.BotNotificationClient;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.service.LinkUpdateService;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinkUpdaterScheduler {

    private final LinkRepository linkRepository;
    private final List<LinkUpdateService> updateServices;
    private final BotNotificationClient botNotificationClient;

    @Scheduled(fixedDelayString = "${app.scheduler.interval:30s}")
    public void update() {
        log.atInfo().log("Начало фоновой проверки обновлений...");

        List<Link> allLinks = linkRepository.findAll();

        if (allLinks.isEmpty()) {
            log.atInfo().log("Фоновая проверка завершена: список ссылок пуст.");
            return;
        }

        var linkGroups = allLinks.stream().collect(Collectors.groupingBy(Link::url));

        for (var entry : linkGroups.entrySet()) {
            try {
                processLinkGroup(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                log.atError().setCause(e).addKeyValue("url", entry.getKey()).log("Ошибка при обработке группы ссылок");
            }
        }

        log.atInfo()
                .addKeyValue("total_urls", linkGroups.size())
                .addKeyValue("total_subscriptions", allLinks.size())
                .log("Фоновая проверка обновлений успешно завершена.");
    }

    private void processLinkGroup(URI url, List<Link> links) {
        updateServices.stream()
                .filter(service -> service.supports(url))
                .findFirst()
                .ifPresent(service -> {
                    service.fetchUpdateDate(url).ifPresent(externalDate -> {
                        List<Link> linksToNotify = links.stream()
                                .filter(l -> externalDate.isAfter(l.lastUpdate()))
                                .toList();

                        if (!linksToNotify.isEmpty()) {
                            log.atInfo()
                                    .addKeyValue("url", url)
                                    .addKeyValue("affected_chats", linksToNotify.size())
                                    .log("Найдено обновление, отправляю уведомления");

                            notifyBot(url, service.getUpdateDescription(url, externalDate), linksToNotify);
                        }

                        OffsetDateTime now = OffsetDateTime.now();
                        links.forEach(l -> linkRepository.updateLastUpdate(l.id(), now));
                    });
                });
    }

    private void notifyBot(URI url, String description, List<Link> links) {
        List<Long> chatIds = links.stream().map(Link::chatId).toList();
        Long linkId = links.isEmpty() ? 0L : links.getFirst().id();

        botNotificationClient.sendUpdate(new LinkUpdate(linkId, url, description, chatIds));
    }
}
