package backend.academy.linktracker.scrapper.service.update;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.LinkProcessingResult;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.dto.UpdateResult;
import backend.academy.linktracker.scrapper.properties.SchedulerProperties;
import backend.academy.linktracker.scrapper.service.LinkService;
import backend.academy.linktracker.scrapper.service.SubscriptionService;
import backend.academy.linktracker.scrapper.service.formatter.NotificationFormatter;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final SchedulerProperties schedulerProperties;

    public LinkProcessingResult processLinkUpdate(Link link) {
        try {
            Optional<OffsetDateTime> newUpdateTime = executeCheck(link);

            if (link.errorCount() > 0) {
                linkService.resetErrorCount(link.id());
            }
            return new LinkProcessingResult(Optional.empty(), newUpdateTime);

        } catch (Exception e) {
            log.atError().addKeyValue("url", link.url()).setCause(e).log("Ошибка при проверке ссылки");
            linkService.incrementErrorCount(link.id());

            int nextErrorCount = link.errorCount() + 1;
            int threshold = schedulerProperties.errorThreshold();

            Optional<String> errorUrl = (nextErrorCount >= threshold && nextErrorCount % threshold == 0)
                    ? Optional.of(link.url().toString())
                    : Optional.empty();

            return new LinkProcessingResult(errorUrl, Optional.empty());
        }
    }

    private Optional<OffsetDateTime> executeCheck(Link link) {
        for (LinkUpdateService service : updateServices) {
            Optional<List<UpdateResult>> resultsOpt = service.fetchUpdates(link.url(), link.lastUpdate());

            if (resultsOpt.isPresent()) {

                if (link.lastUpdate() == null) {
                    return Optional.of(OffsetDateTime.now());
                }

                List<UpdateResult> results = resultsOpt.orElseThrow();
                if (results.isEmpty()) {
                    return Optional.empty();
                }

                notifyUsers(link, results);

                return Optional.of(results.stream()
                        .map(UpdateResult::updateDate)
                        .max(OffsetDateTime::compareTo)
                        .orElse(link.lastUpdate()));
            }
        }

        log.atWarn().addKeyValue("url", link.url()).log("Сервис для обработки ссылки не найден");
        return Optional.empty();
    }

    private void notifyUsers(Link link, List<UpdateResult> results) {
        List<Long> chatIds = subscriptionService.getChatIdsByLinkId(link.id());
        if (!chatIds.isEmpty()) {
            String text = formatter.formatUpdate(results);
            updateSender.sendUpdate(new LinkUpdate(link.id(), link.url(), text, chatIds, false));
        }
    }
}
