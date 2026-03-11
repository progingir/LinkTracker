package backend.academy.linktracker.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.command.TrackCommand;
import backend.academy.linktracker.bot.repository.StateRepository;
import backend.academy.linktracker.bot.service.UserState;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.Test;

class TrackCommandTest {

    @Test
    void execute_ShouldSetStateAndReturnMessage() {
        StateRepository stateRepository = mock(StateRepository.class);
        TrackCommand command = new TrackCommand(stateRepository);

        long chatId = 123L;
        Update update = mockUpdate(chatId);

        SendMessage response = command.handle(update);

        assertEquals(
                "Пришлите ссылку на ресурс, который хотите отслеживать:",
                response.getParameters().get("text"));
        verify(stateRepository).setState(chatId, UserState.WAITING_FOR_LINK);
    }

    private Update mockUpdate(long chatId) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        return update;
    }
}
