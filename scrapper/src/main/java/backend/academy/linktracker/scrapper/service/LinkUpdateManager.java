package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.dto.UpdateResult;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkUpdateManager {

    private final List<LinkUpdateService> updateServices;
    private final LinkService linkService;
    private final SubscriptionService subscriptionService;
    private final NotificationFormatter formatter;
    private final UpdateSender updateSender;

    @Value("${app.scheduler.error-threshold:5}")
    private int errorThreshold;

    public Optional<String> processLinkUpdate(Link link) {
        try {
            executeCheck(link);
            linkService.resetErrorCount(link.id());
            return Optional.empty();
        } catch (Exception e) {
            log.atError()
                    .addKeyValue("url", link.url())
                    .addKeyValue("link_id", link.id())
                    .setCause(e)
                    .log("Ошибка при выполнении проверки ссылки");

            linkService.incrementErrorCount(link.id());

            int nextErrorCount = link.errorCount() + 1;
            if (nextErrorCount >= errorThreshold && nextErrorCount % errorThreshold == 0) {
                log.atWarn()
                        .addKeyValue("url", link.url())
                        .addKeyValue("error_count", nextErrorCount)
                        .log("Ссылка достигла порога ошибок и будет включена в отчет");

                return Optional.of(link.url().toString());
            }
            return Optional.empty();
        }
    }

    private void executeCheck(Link link) {
        LinkUpdateService service = updateServices.stream()
                .filter(s -> s.supports(link.url()))
                .findFirst()
                .orElse(null);

        if (service == null) {
            log.atDebug().addKeyValue("url", link.url()).log("Сервис для обработки ссылки не найден");
            return;
        }

        if (link.lastUpdate() == null) {
            linkService.updateLastUpdateTime(link.id(), OffsetDateTime.now());
            return;
        }

        List<UpdateResult> results = service.fetchUpdates(link.url(), link.lastUpdate());
        if (results.isEmpty()) {
            return;
        }

        List<Long> chatIds = subscriptionService.getChatIdsByLinkId(link.id());
        if (!chatIds.isEmpty()) {
            String text = formatter.formatUpdate(results);
            updateSender.sendUpdate(new LinkUpdate(link.id(), link.url(), text, chatIds, false));

            log.atInfo()
                    .addKeyValue("url", link.url())
                    .addKeyValue("link_id", link.id())
                    .addKeyValue("notified_chats_count", chatIds.size())
                    .addKeyValue("updates_found", results.size())
                    .log("Обнаружены обновления, пользователи уведомлены");
        }

        OffsetDateTime latest = results.stream()
                .map(UpdateResult::updateDate)
                .max(OffsetDateTime::compareTo)
                .orElse(link.lastUpdate());

        linkService.updateLastUpdateTime(link.id(), latest);
    }
}
