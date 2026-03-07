package backend.academy.linktracker.bot;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

class BotServiceTest {

    private BotService botService;
    private Command mockCommand;
    private TelegramBot telegramBot;

    @BeforeEach
    void setUp() {
        telegramBot = mock(TelegramBot.class);
        mockCommand = mock(Command.class);
        botService = new BotService(telegramBot, List.of(mockCommand));
    }

    @Test
    @DisplayName("Выбор корректной команды при совпадении")
    void shouldSelectCorrectCommand() {
        String commandText = "/test";
        Update update = mockUpdate(commandText, 123L);
        SendMessage expectedResponse = new SendMessage(123L, "OK");

        when(mockCommand.supports(commandText)).thenReturn(true);
        when(mockCommand.handle(update)).thenReturn(expectedResponse);

        botService.process(List.of(update));

        verify(mockCommand).handle(update);
    }

    @Test
    @DisplayName("Возврат сообщения о неизвестной команде")
    void shouldReturnUnknownCommandMessage() {
        Update update = mockUpdate("unknown text", 123L);
        when(mockCommand.supports(anyString())).thenReturn(false);

        botService.process(List.of(update));

        verify(telegramBot)
                .execute(argThat(request -> request != null
                        && request.getParameters().get("text").toString().contains("Неизвестная команда")));
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
