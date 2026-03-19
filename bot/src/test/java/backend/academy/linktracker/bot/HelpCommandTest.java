package backend.academy.linktracker.bot;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.command.CommandRegistry;
import backend.academy.linktracker.bot.command.HelpCommand;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HelpCommandTest {

    @Test
    @DisplayName("Проверка формирования списка команд в /help")
    void handleReturnsAllCommandsFromRegistry() {

        CommandRegistry registry = mock(CommandRegistry.class);

        when(registry.getCommandsMetadata()).thenReturn(Map.of("/test1", "desc1"));

        HelpCommand helpCommand = new HelpCommand(registry);

        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);

        SendMessage response = helpCommand.handle(update);
        String text = (String) response.getParameters().get("text");

        assertTrue(text.contains("Доступные команды:"));
        assertTrue(text.contains("/test1 - desc1"));
    }
}
