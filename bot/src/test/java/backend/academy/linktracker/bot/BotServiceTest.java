package backend.academy.linktracker.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.command.Command;
import backend.academy.linktracker.bot.command.HelpCommand;
import backend.academy.linktracker.bot.command.StartCommand;
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
import org.mockito.ArgumentCaptor;

class BotServiceTest {

    private TelegramBot telegramBot;
    private BotService botService;

    @BeforeEach
    void setUp() {
        telegramBot = mock(TelegramBot.class);
        List<Command> commands = List.of(new StartCommand(), new HelpCommand());
        botService = new BotService(telegramBot, commands);
    }

    @Test
    @DisplayName("проверка работы команды /start")
    void testStartCommand() {
        Update update = mockUpdate("/start", "testuser", 123L);

        botService.process(List.of(update));

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(captor.capture());

        SendMessage sentMessage = captor.getValue();
        assertEquals(
                "Привет! Я LinkTracker, помогу тебе следить за обновлениями контента. Введи /help для списка команд",
                sentMessage.getParameters().get("text"));
    }

    @Test
    @DisplayName("проверка работы команды /help")
    void testHelpCommand() {
        Update update = mockUpdate("/help", "testuser", 123L);

        botService.process(List.of(update));

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(captor.capture());

        String text = (String) captor.getValue().getParameters().get("text");
        assertTrue(text.contains("Доступные команды:"));
    }

    @Test
    @DisplayName("проверка работы при вводе неизвестной команды")
    void testUnknownCommand() {
        Update update = mockUpdate("какой-то текст", "testuser", 123L);

        botService.process(List.of(update));

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(captor.capture());

        assertEquals(
                "Неизвестная команда. Воспользуйтесь /help, чтобы посмотреть список доступных команд",
                captor.getValue().getParameters().get("text"));
    }

    private Update mockUpdate(String text, String username, Long chatId) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        User user = mock(User.class);

        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn(text);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.from()).thenReturn(user);
        when(user.username()).thenReturn(username);

        return update;
    }
}
