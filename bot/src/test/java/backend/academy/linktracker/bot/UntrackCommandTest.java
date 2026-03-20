package backend.academy.linktracker.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.command.UntrackCommand;
import backend.academy.linktracker.bot.service.StateService;
import backend.academy.linktracker.bot.service.UntrackState;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.Test;

class UntrackCommandTest {

    @Test
    void execute_ShouldSetStateAndReturnMessage() {
        StateService stateService = mock(StateService.class);
        UntrackCommand command = new UntrackCommand(stateService);

        long chatId = 456L;
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);

        SendMessage response = command.handle(update);

        assertEquals(
                "Пришлите ссылку, которую хотите перестать отслеживать:",
                response.getParameters().get("text"));
        verify(stateService).setState(chatId, UntrackState.WAITING_FOR_UNTRACK_LINK);
    }
}
