package backend.academy.linktracker.bot;

import backend.academy.linktracker.bot.command.Command;
import backend.academy.linktracker.bot.service.BotService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BotServiceTest {

    private BotService botService;
    private Command mockCommand;

    @BeforeEach
    void setUp() {
        TelegramBot telegramBot = mock(TelegramBot.class);
        mockCommand = mock(Command.class);

        botService = new BotService(telegramBot, List.of(mockCommand));
    }

    @Test
    @DisplayName("Выбор корректной команды при совпадении")
    void shouldSelectCorrectCommand() {
        Update update = mockUpdate("/test", 123L);
        SendMessage expectedResponse = new SendMessage(123L, "OK");

        when(mockCommand.supports(update)).thenReturn(true);
        when(mockCommand.handle(update)).thenReturn(expectedResponse);

        SendMessage result = ReflectionTestUtils.invokeMethod(botService, "processUpdate", update);

        assertEquals("OK", result.getParameters().get("text"));
    }

    @Test
    @DisplayName("Возврат сообщения о неизвестной команде")
    void shouldReturnUnknownCommandMessage() {
        Update update = mockUpdate("unknown text", 123L);

        when(mockCommand.supports(any())).thenReturn(false);

        SendMessage result = ReflectionTestUtils.invokeMethod(botService, "processUpdate", update);

        assertEquals(
            "Неизвестная команда. Воспользуйтесь /help, чтобы посмотреть список доступных команд",
            result.getParameters().get("text")
        );
    }

    private Update mockUpdate(String text, Long chatId) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        User user = mock(User.class);

        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn(text);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.from()).thenReturn(user);
        when(user.id()).thenReturn(1L);

        return update;
    }
}
