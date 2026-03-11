package backend.academy.linktracker.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.command.CancelCommand;
import backend.academy.linktracker.bot.repository.StateRepository;
import backend.academy.linktracker.bot.service.UserState;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CancelCommandTest {

    @Test
    @DisplayName("Отмена активной операции")
    void execute_ShouldResetState() {
        StateRepository stateRepository = mock(StateRepository.class);
        CancelCommand command = new CancelCommand(stateRepository);
        long chatId = 789L;

        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);

        when(stateRepository.getContext(chatId))
                .thenReturn(new StateRepository.UserContext(UserState.WAITING_FOR_LINK, null, null));

        SendMessage response = command.handle(update);

        assertEquals(
                "🔄 Операция отменена. Я готов к новым командам.",
                response.getParameters().get("text"));

        verify(stateRepository).clear(chatId);
    }
}
