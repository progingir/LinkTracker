package backend.academy.linktracker.bot;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.dto.LinkResponse;
import backend.academy.linktracker.bot.exception.ResourceNotFoundException;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UntrackCommandIntegrationTest extends BotIntegrationTestBase {

    @Test
    @DisplayName("Успешное прекращение отслеживания ссылки /untrack")
    void shouldRemoveLinkSuccessfullyWhenValidLinkProvided() {
        long chatId = 103L;
        URI link = URI.create("https://github.com/test/repo");

        when(scrapperClient.removeLink(eq(chatId), eq(link))).thenReturn(new LinkResponse(1L, link, List.of()));

        botService.process(List.of(createUpdate(chatId, "/untrack")));
        verify(postRequestedFor(urlMatching("/bot[^/]+/sendMessage"))
                .withRequestBody(containing(encoded("Пришлите ссылку"))));

        botService.process(List.of(createUpdate(chatId, link.toString())));
        verify(postRequestedFor(urlMatching("/bot[^/]+/sendMessage")).withRequestBody(containing(encoded("успешно"))));
    }

    @Test
    @DisplayName("Ошибка при попытке удалить несуществующую ссылку")
    void shouldSendErrorMessageWhenLinkNotFound() {
        long chatId = 204L;
        URI link = URI.create("https://github.com/not/found");

        when(scrapperClient.removeLink(eq(chatId), eq(link)))
                .thenThrow(new ResourceNotFoundException("Ссылка не найдена"));

        botService.process(List.of(createUpdate(chatId, "/untrack")));
        botService.process(List.of(createUpdate(chatId, link.toString())));

        verify(postRequestedFor(urlMatching("/bot[^/]+/sendMessage"))
                .withRequestBody(containing(encoded("не найдена в вашем списке"))));
    }
}
