package backend.academy.linktracker.scrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.scheduler.LinkUpdaterScheduler;
import backend.academy.linktracker.scrapper.service.LinkService;
import backend.academy.linktracker.scrapper.service.LinkUpdateManager;
import backend.academy.linktracker.scrapper.service.NotificationFormatter;
import backend.academy.linktracker.scrapper.service.SubscriptionService;
import backend.academy.linktracker.scrapper.service.UpdateSender;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class LinkUpdaterSchedulerTest {

    private static final int BATCH_SIZE = 10;

    @Mock
    private LinkService linkService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private LinkUpdateManager updateManager;

    @Mock
    private NotificationFormatter formatter;

    @Mock
    private UpdateSender updateSender;

    @Mock
    private Executor executor;

    private LinkUpdaterScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new LinkUpdaterScheduler(
                linkService, subscriptionService, updateManager, formatter, updateSender, executor);

        ReflectionTestUtils.setField(scheduler, "batchSize", BATCH_SIZE);
        ReflectionTestUtils.setField(scheduler, "threadsCount", 1);

        lenient()
                .doAnswer(invocation -> {
                    ((Runnable) invocation.getArgument(0)).run();
                    return null;
                })
                .when(executor)
                .execute(any(Runnable.class));
    }

    @Test
    @DisplayName("Тест 1: Успешная обработка. Должен вызвать менеджер для каждой ссылки")
    void shouldInvokeManagerForEachLink() {
        Link link1 = new Link(1L, URI.create("https://link1.com"), OffsetDateTime.now(), OffsetDateTime.now(), 0);
        Link link2 = new Link(2L, URI.create("https://link2.com"), OffsetDateTime.now(), OffsetDateTime.now(), 0);

        when(linkService.findOldest(BATCH_SIZE)).thenReturn(List.of(link1, link2));
        when(updateManager.processLinkUpdate(any())).thenReturn(Optional.empty());

        scheduler.update();

        verify(updateManager, times(2)).processLinkUpdate(any());
        verify(linkService).updateLastCheckTimeBatch(any(), any());
    }

    @Test
    @DisplayName("Тест 2: Изоляция ошибок. Если одна ссылка упала с Exception, вторая должна обработаться")
    void shouldContinueProcessingIfOneLinkThrowsException() {
        Link brokenLink = new Link(1L, URI.create("https://broken.com"), OffsetDateTime.now(), OffsetDateTime.now(), 0);
        Link normalLink = new Link(2L, URI.create("https://normal.com"), OffsetDateTime.now(), OffsetDateTime.now(), 0);

        when(linkService.findOldest(BATCH_SIZE)).thenReturn(List.of(brokenLink, normalLink));

        when(updateManager.processLinkUpdate(brokenLink)).thenThrow(new RuntimeException("API CRASH"));
        when(updateManager.processLinkUpdate(normalLink)).thenReturn(Optional.empty());

        scheduler.update();

        verify(updateManager).processLinkUpdate(brokenLink);
        verify(updateManager).processLinkUpdate(normalLink);
        verify(linkService).updateLastCheckTimeBatch(any(), any());
    }

    @Test
    @DisplayName("Тест 3: Сбор отчета. Если менеджер вернул URL (порог достигнут), должен уйти системный отчет")
    void shouldCollectFailedLinksAndSendSystemReport() {
        URI badUrl = URI.create("https://bad.com");
        Link link = new Link(1L, badUrl, OffsetDateTime.now(), OffsetDateTime.now(), 4);

        when(linkService.findOldest(BATCH_SIZE)).thenReturn(List.of(link));
        when(updateManager.processLinkUpdate(link)).thenReturn(Optional.of(badUrl.toString()));
        when(subscriptionService.getChatIdsByLinkId(1L)).thenReturn(List.of(100L));
        when(formatter.formatErrorReport(any())).thenReturn("Aggregated Error Message");

        scheduler.update();

        ArgumentCaptor<LinkUpdate> captor = ArgumentCaptor.forClass(LinkUpdate.class);
        verify(updateSender).sendUpdate(captor.capture());

        LinkUpdate report = captor.getValue();
        assertThat(report.isSystemReport()).isTrue();
        assertThat(report.description()).isEqualTo("Aggregated Error Message");
    }

    @Test
    @DisplayName("Тест 4: Отсутствие обновлений. Если база пуста, ничего не делать")
    void shouldDoNothingIfNoLinksFound() {
        when(linkService.findOldest(BATCH_SIZE)).thenReturn(List.of());

        scheduler.update();

        verify(updateManager, never()).processLinkUpdate(any());
        verify(updateSender, never()).sendUpdate(any());
    }

    @Test
    @DisplayName("Тест 5: Порог не достигнут. Если менеджер вернул Empty, отчет не шлется")
    void shouldNotSendReportIfThresholdNotReached() {
        Link link = new Link(1L, URI.create("https://maybe-bad.com"), OffsetDateTime.now(), OffsetDateTime.now(), 1);

        when(linkService.findOldest(BATCH_SIZE)).thenReturn(List.of(link));
        when(updateManager.processLinkUpdate(link)).thenReturn(Optional.empty());

        scheduler.update();

        verify(updateSender, never()).sendUpdate(any());
        verify(linkService, atLeastOnce()).updateLastCheckTimeBatch(any(), any());
    }
}
