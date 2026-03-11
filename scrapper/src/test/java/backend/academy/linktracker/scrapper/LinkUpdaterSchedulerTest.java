package backend.academy.linktracker.scrapper;

import static org.mockito.Mockito.*;

import backend.academy.linktracker.scrapper.client.BotNotificationClient;
import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.GitHubResponse;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.scheduler.LinkUpdaterScheduler;
import backend.academy.linktracker.scrapper.service.LinkParser;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class LinkUpdaterSchedulerTest {

    @MockitoBean
    private LinkRepository linkRepository;

    @MockitoBean
    private GitHubClient gitHubClient;

    @MockitoBean
    private BotNotificationClient botClient;

    @MockitoBean
    private LinkParser linkParser;

    @Test
    @DisplayName("Сценарий 7: Уведомление отправляется только подписчикам")
    void shouldNotifyOnlySubscribedUsers() {
        URI url = URI.create("https://github.com/user/repo");
        OffsetDateTime now = OffsetDateTime.now();

        Link link1 = new Link(1L, 100L, url, List.of(), List.of(), now.minusDays(1));
        Link link2 = new Link(2L, 200L, url, List.of(), List.of(), now.minusDays(1));

        when(linkRepository.findAll()).thenReturn(List.of(link1, link2));
        when(linkParser.parseGithub(url)).thenReturn(new LinkParser.GithubInfo("user", "repo"));

        when(gitHubClient.fetchRepository("user", "repo"))
                .thenReturn(Optional.of(new GitHubResponse("repo", now, now)));

        LinkUpdaterScheduler scheduler = new LinkUpdaterScheduler(
                linkRepository,
                gitHubClient,
                mock(backend.academy.linktracker.scrapper.client.StackOverflowClient.class),
                linkParser,
                botClient);

        scheduler.update();

        verify(botClient)
                .sendUpdate(argThat(update -> update.tgChatIds().containsAll(List.of(100L, 200L))
                        && update.tgChatIds().size() == 2));
    }

    @Test
    @DisplayName("Сценарий 8: Обработка пустого ответа от API (не падает)")
    void handleExternalApiError() {
        URI url = URI.create("https://github.com/user/repo");
        Link link = new Link(1L, 100L, url, List.of(), List.of(), OffsetDateTime.now());

        when(linkRepository.findAll()).thenReturn(List.of(link));
        when(linkParser.parseGithub(url)).thenReturn(new LinkParser.GithubInfo("user", "repo"));

        when(gitHubClient.fetchRepository(anyString(), anyString())).thenReturn(Optional.empty());

        LinkUpdaterScheduler scheduler = new LinkUpdaterScheduler(
                linkRepository,
                gitHubClient,
                mock(backend.academy.linktracker.scrapper.client.StackOverflowClient.class),
                linkParser,
                botClient);

        scheduler.update();

        verifyNoInteractions(botClient);
    }
}
