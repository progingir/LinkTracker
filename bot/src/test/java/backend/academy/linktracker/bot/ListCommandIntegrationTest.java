package backend.academy.linktracker.bot;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.dto.LinkResponse;
import backend.academy.linktracker.bot.dto.ListLinksResponse;
import backend.academy.linktracker.bot.exception.ResourceNotFoundException;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ListCommandIntegrationTest extends BotIntegrationTestBase {

    @Test
    @DisplayName("Успешное получение списка ссылок /list")
    void shouldReturnLinksListWhenUserIsRegistered() {
        long chatId = 101L;
        when(scrapperClient.getLinks(chatId))
                .thenReturn(new ListLinksResponse(
                        List.of(new LinkResponse(1L, URI.create("https://github.com/test"), List.of("dev"))), 1));

        botService.process(List.of(createUpdate(chatId, "/list")));

        verify(postRequestedFor(urlMatching("/bot[^/]+/sendMessage"))
                .withRequestBody(containing("chat_id=101"))
                .withRequestBody(containing(encoded("https://github.com/test"))));
    }

    @Test
    @DisplayName("Ошибка получения списка ссылок незарегистрированным пользователем")
    void shouldSendRegistrationPromptWhenUserNotRegistered() {
        long chatId = 201L;
        when(scrapperClient.getLinks(chatId)).thenThrow(new ResourceNotFoundException("Chat not found"));

        botService.process(List.of(createUpdate(chatId, "/list")));

        verify(postRequestedFor(urlMatching("/bot[^/]+/sendMessage"))
                .withRequestBody(containing(encoded("Вы еще не зарегистрированы"))));
    }
}
