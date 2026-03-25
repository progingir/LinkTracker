package backend.academy.linktracker.scrapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.client.BotNotificationClient;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import backend.academy.linktracker.scrapper.scheduler.LinkUpdaterScheduler;
import backend.academy.linktracker.scrapper.service.LinkUpdateService;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@TestPropertySource(properties = "app.database.access-type=jdbc")
@Import(TestcontainersConfiguration.class)
class LinkUpdaterSchedulerTest {

    @Autowired
    private LinkUpdaterScheduler scheduler;

    @MockitoBean
    private LinkRepository linkRepository;

    @MockitoBean
    private SubscriptionRepository subscriptionRepository;

    @MockitoBean
    private BotNotificationClient botClient;

    @MockitoBean(name = "gitHubLinkUpdateService")
    private LinkUpdateService githubUpdateService;

    @Test
    @DisplayName("Сценарий 7: Уведомление приходит подписчикам конкретной ссылки, у которых устарели данные")
    void shouldNotifyOnlySubscribedUsersWithOutdatedLinks() {
        URI githubUrl = URI.create("https://github.com/user/repo");
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime externalUpdate = now.minusHours(5);

        Link githubLink = new Link(1L, githubUrl, now.minusDays(1), now.minusMinutes(30));

        when(linkRepository.findOldest(anyInt())).thenReturn(List.of(githubLink));

        when(subscriptionRepository.findChatIdsByLinkId(1L)).thenReturn(List.of(100L));

        when(githubUpdateService.supports(githubUrl)).thenReturn(true);
        when(githubUpdateService.fetchUpdateDate(githubUrl)).thenReturn(Optional.of(externalUpdate));
        when(githubUpdateService.getUpdateDescription(githubUrl, externalUpdate))
            .thenReturn("GitHub update!");

        scheduler.update();

        verify(botClient, times(1))
            .sendUpdate(argThat(update -> update.url().equals(githubUrl)
                && update.tgChatIds().contains(100L)
                && update.tgChatIds().size() == 1));
    }

    @Test
    @DisplayName("Сценарий 8: Обработка пустого ответа")
    void handleEmptyResponse() {
        URI url = URI.create("https://github.com/user/repo");
        OffsetDateTime now = OffsetDateTime.now();

        Link link = new Link(1L, url, now.minusDays(1), now);

        when(linkRepository.findOldest(anyInt())).thenReturn(List.of(link));

        when(githubUpdateService.supports(url)).thenReturn(true);
        when(githubUpdateService.fetchUpdateDate(url)).thenReturn(Optional.empty());

        scheduler.update();

        verifyNoInteractions(botClient);
    }

    @Test
    @DisplayName("Сценарий 9: Обработка критической ошибки API")
    void handleApiError() {
        URI url = URI.create("https://github.com/user/repo");
        OffsetDateTime now = OffsetDateTime.now();

        Link link = new Link(1L, url, now, now);

        when(linkRepository.findOldest(anyInt())).thenReturn(List.of(link));

        when(githubUpdateService.supports(url)).thenReturn(true);
        when(githubUpdateService.fetchUpdateDate(url)).thenThrow(new RuntimeException("API Down"));

        scheduler.update();

        verifyNoInteractions(botClient);
        verify(linkRepository, never()).updateLastUpdateTime(anyLong(), any());
    }
}
