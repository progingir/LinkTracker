package backend.academy.linktracker.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.command.CancelCommand;
import backend.academy.linktracker.bot.repository.StateRepository;
import backend.academy.linktracker.bot.service.StateService;
import backend.academy.linktracker.bot.service.TrackState;
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
        StateService stateService = mock(StateService.class);
        CancelCommand command = new CancelCommand(stateService);
        long chatId = 789L;

        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);

        when(stateService.getContext(chatId))
                .thenReturn(new StateRepository.UserContext(TrackState.WAITING_FOR_LINK, null, null));

        SendMessage response = command.handle(update);

        assertEquals(
                "🔄 Операция отменена. Я готов к новым командам.",
                response.getParameters().get("text"));

        verify(stateService).clear(chatId);
    }
}
