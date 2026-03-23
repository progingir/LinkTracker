package backend.academy.linktracker.bot;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.dto.LinkResponse;
import backend.academy.linktracker.bot.exception.ResourceAlreadyExistsException;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TrackCommandIntegrationTest extends BotIntegrationTestBase {

    @Test
    @DisplayName("Полный флоу добавления ссылки /track")
    void shouldCompleteTrackFlowWhenValidDataProvided() {
        long chatId = 102L;
        URI link = URI.create("https://stackoverflow.com/questions/123");

        when(scrapperClient.addLink(eq(chatId), eq(link), any()))
                .thenReturn(new LinkResponse(1L, link, List.of("java")));

        botService.process(List.of(createUpdate(chatId, "/track")));
        verify(postRequestedFor(urlMatching("/bot[^/]+/sendMessage"))
                .withRequestBody(containing(encoded("Пришлите ссылку на ресурс"))));

        botService.process(List.of(createUpdate(chatId, link.toString())));
        verify(postRequestedFor(urlMatching("/bot[^/]+/sendMessage"))
                .withRequestBody(containing(encoded("Введите теги"))));

        botService.process(List.of(createUpdate(chatId, "java")));

        verify(postRequestedFor(urlMatching("/bot[^/]+/sendMessage"))
                .withRequestBody(containing(encoded("успешно добавлена"))));
    }

    @Test
    @DisplayName("Попытка добавить уже существующую ссылку")
    void shouldSendErrorMessageWhenLinkAlreadyExists() {
        long chatId = 202L;
        URI link = URI.create("https://github.com/already/exists");

        when(scrapperClient.addLink(eq(chatId), eq(link), any()))
                .thenThrow(new ResourceAlreadyExistsException("Link exists"));

        botService.process(List.of(createUpdate(chatId, "/track")));
        botService.process(List.of(createUpdate(chatId, link.toString())));

        botService.process(List.of(createUpdate(chatId, "нет")));

        verify(postRequestedFor(urlMatching("/bot[^/]+/sendMessage"))
                .withRequestBody(containing(encoded("уже отслеживается в вашем списке"))));
    }

    @Test
    @DisplayName("Ввод некорректного URL при добавлении")
    void shouldSendErrorMessageWhenUrlIsInvalid() {
        long chatId = 203L;

        botService.process(List.of(createUpdate(chatId, "/track")));
        botService.process(List.of(createUpdate(chatId, "not-a-valid-url")));

        verify(postRequestedFor(urlMatching("/bot[^/]+/sendMessage"))
                .withRequestBody(containing(encoded("Неверный формат ссылки"))));
    }
}
