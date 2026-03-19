package backend.academy.linktracker.scrapper.scheduler;

import backend.academy.linktracker.scrapper.client.BotNotificationClient;
import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.service.LinkParser;
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
    private final GitHubClient gitHubClient;
    private final StackOverflowClient stackOverflowClient;
    private final LinkParser linkParser;
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
        OffsetDateTime lastCheck = links.stream()
                .map(Link::lastUpdate)
                .min(OffsetDateTime::compareTo)
                .orElse(OffsetDateTime.now());

        var githubInfo = linkParser.parseGithub(url);
        if (githubInfo != null) {
            gitHubClient.fetchRepository(githubInfo.owner(), githubInfo.repo()).ifPresent(response -> {
                if (response.updatedAt().isAfter(lastCheck)) {
                    notifyBot(url, "Обнаружена новая активность в GitHub репозитории!", links);
                }
            });
        }

        Long questionId = linkParser.parseStackOverflow(url);
        if (questionId != null) {
            stackOverflowClient.fetchQuestion(questionId).ifPresent(response -> {
                if (response.lastActivityDate().isAfter(lastCheck)) {
                    notifyBot(url, "Обнаружена новая активность в вопросе: " + response.title(), links);
                }
            });
        }

        OffsetDateTime now = OffsetDateTime.now();
        links.forEach(l -> linkRepository.updateLastUpdate(l.id(), now));
    }

    private void notifyBot(URI url, String description, List<Link> links) {
        List<Long> chatIds = links.stream().map(Link::chatId).toList();

        Long linkId = links.isEmpty() ? Long.valueOf(0L) : links.getFirst().id();

        botNotificationClient.sendUpdate(new LinkUpdate(linkId, url, description, chatIds));
    }
}
