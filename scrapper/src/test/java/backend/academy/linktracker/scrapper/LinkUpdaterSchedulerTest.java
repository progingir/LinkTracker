package backend.academy.linktracker.scrapper;

import static org.mockito.Mockito.*;

import backend.academy.linktracker.scrapper.client.BotNotificationClient;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class LinkUpdaterSchedulerTest {

    @Autowired
    private LinkUpdaterScheduler scheduler;

    @MockitoBean
    private LinkRepository linkRepository;

    @MockitoBean
    private BotNotificationClient botClient;

    @MockitoBean(name = "gitHubLinkUpdateService")
    private LinkUpdateService githubUpdateService;

    @Test
    @DisplayName("Сценарий 7: Уведомление приходит подписчикам конкретной ссылки, у которых устарели данные")
    void shouldNotifyOnlySubscribedUsersWithOutdatedLinks() {
        URI githubUrl = URI.create("https://github.com/user/repo");
        URI otherUrl = URI.create("https://stackoverflow.com/questions/1");
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime externalUpdate = now.minusHours(5);

        Link githubOld = new Link(1L, 100L, githubUrl, List.of(), now.minusDays(1));
        Link githubNew = new Link(2L, 200L, githubUrl, List.of(), now.minusHours(1));
        Link otherUser = new Link(3L, 300L, otherUrl, List.of(), now.minusDays(1));

        when(linkRepository.findAll()).thenReturn(List.of(githubOld, githubNew, otherUser));

        when(githubUpdateService.supports(githubUrl)).thenReturn(true);
        when(githubUpdateService.fetchUpdateDate(githubUrl)).thenReturn(Optional.of(externalUpdate));
        when(githubUpdateService.getUpdateDescription(githubUrl, externalUpdate))
            .thenReturn("GitHub update!");

        scheduler.update();

        verify(botClient, times(1))
            .sendUpdate(argThat(update -> update.url().equals(githubUrl)
                && update.tgChatIds().contains(100L)
                && !update.tgChatIds().contains(200L)
                && !update.tgChatIds().contains(300L)
                && update.tgChatIds().size() == 1));
    }

    @Test
    @DisplayName("Сценарий 8: Обработка пустого ответа")
    void handleEmptyResponse() {
        URI url = URI.create("https://github.com/user/repo");
        Link link = new Link(1L, 100L, url, List.of(), OffsetDateTime.now());

        when(linkRepository.findAll()).thenReturn(List.of(link));
        when(githubUpdateService.supports(url)).thenReturn(true);

        when(githubUpdateService.fetchUpdateDate(url)).thenReturn(Optional.empty());

        scheduler.update();

        verifyNoInteractions(botClient);
    }

    @Test
    @DisplayName("Сценарий 9: Обработка критической ошибки API")
    void handleApiError() {
        URI url = URI.create("https://github.com/user/repo");
        Link link = new Link(1L, 100L, url, List.of(), OffsetDateTime.now());

        when(linkRepository.findAll()).thenReturn(List.of(link));
        when(githubUpdateService.supports(url)).thenReturn(true);

        when(githubUpdateService.fetchUpdateDate(url)).thenThrow(new RuntimeException("API Down"));

        scheduler.update();

        verifyNoInteractions(botClient);

        verify(linkRepository, never()).updateLastUpdate(anyLong(), any());
    }
}
