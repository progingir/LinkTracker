package backend.academy.linktracker.bot;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.command.ListCommand;
import backend.academy.linktracker.bot.dto.LinkResponse;
import backend.academy.linktracker.bot.dto.ListLinksResponse;
import backend.academy.linktracker.bot.repository.StateRepository;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ListCommandTest {

    @Test
    @DisplayName("Отображение сообщения, если список пуст")
    void handle_ShouldReturnEmptyMessage_WhenNoLinks() {
        ScrapperClient client = mock(ScrapperClient.class);
        ListCommand command = new ListCommand(client);

        long chatId = 1L;
        when(client.getLinks(chatId)).thenReturn(new ListLinksResponse(List.of(), 0));

        Update update = mockUpdate(chatId, "/list");

        SendMessage response = command.handle(update);

        String text = response.getParameters().get("text").toString();

        assertTrue(text.contains("список отслеживания пуст"));
    }

    @Test
    @DisplayName("Отображение списка ссылок без фильтрации")
    void handle_ShouldReturnLinksList() {
        ScrapperClient client = mock(ScrapperClient.class);
        StateRepository stateRepository = mock(StateRepository.class);
        ListCommand command = new ListCommand(client);
        long chatId = 1L;

        var links = List.of(new LinkResponse(1L, URI.create("http://test.com"), List.of("work"), List.of()));
        when(client.getLinks(chatId)).thenReturn(new ListLinksResponse(links, 1));

        Update update = mockUpdate(chatId, "/list");

        SendMessage response = command.handle(update);

        String text = response.getParameters().get("text").toString();
        assertTrue(text.contains("Вы отслеживаете следующие ресурсы:"));
        assertTrue(text.contains("http://test.com"));
        assertTrue(text.contains("work"));
    }

    @Test
    @DisplayName("Фильтрация списка по тегу (/list work)")
    void handle_ShouldFilterByTag() {
        ScrapperClient client = mock(ScrapperClient.class);
        ListCommand command = new ListCommand(client);
        long chatId = 1L;

        var links = List.of(
                new LinkResponse(1L, URI.create("http://github.com/job"), List.of("work"), List.of()),
                new LinkResponse(2L, URI.create("http://github.com/play"), List.of("fun"), List.of()));
        when(client.getLinks(chatId)).thenReturn(new ListLinksResponse(links, 2));

        Update update = mockUpdate(chatId, "/list work");

        SendMessage response = command.handle(update);

        String text = response.getParameters().get("text").toString();
        assertTrue(text.contains("job"));
        assertFalse(text.contains("play"));
    }

    private Update mockUpdate(long chatId, String text) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn(text);
        return update;
    }
}
