package backend.academy.linktracker.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.command.StartCommand;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StartCommandTest {

    private ScrapperClient scrapperClient;
    private StartCommand startCommand;

    @BeforeEach
    void setUp() {
        scrapperClient = mock(ScrapperClient.class);
        startCommand = new StartCommand(scrapperClient);
    }

    @Test
    @DisplayName("Проверка текста приветствия")
    void handleReturnsCorrectMessage() {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(123L);

        SendMessage response = startCommand.handle(update);

        assertEquals(123L, response.getParameters().get("chat_id"));
        assertEquals(
                "Привет! Я LinkTracker, помогу тебе следить за обновлениями контента. Введи /help для списка команд",
                response.getParameters().get("text"));
    }

    @Test
    @DisplayName("Поддержка команды /start")
    void supportsCorrectCommand() {
        assertTrue(startCommand.supports("/start"));
        assertFalse(startCommand.supports("/help"));
    }
}
