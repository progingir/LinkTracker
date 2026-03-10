package backend.academy.linktracker.bot;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.handler.WaitingForLinkHandler;
import backend.academy.linktracker.bot.repository.StateRepository;
import backend.academy.linktracker.bot.service.UserState;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BotFunctionalTest {

    @Test
    @DisplayName("Валидация ссылки в /track (корректная vs некорректная)")
    void trackLinkValidation() {
        StateRepository stateRepository = mock(StateRepository.class);
        WaitingForLinkHandler handler = new WaitingForLinkHandler(stateRepository);

        long chatId = 123L;

        Update invalidUpdate = mockUpdate("tbank://invalid", chatId);
        SendMessage failResponse = handler.handle(invalidUpdate, null);
        assertTrue(((String) failResponse.getParameters().get("text")).contains("Неверный формат"));

        Update validUpdate = mockUpdate("https://github.com/user/repo", chatId);
        SendMessage successResponse = handler.handle(validUpdate, null);
        assertTrue(((String) successResponse.getParameters().get("text")).contains("Введите теги"));
        verify(stateRepository).setState(chatId, UserState.WAITING_FOR_TAGS);
    }

    private Update mockUpdate(String text, long chatId) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        com.pengrad.telegrambot.model.Chat chat = mock(com.pengrad.telegrambot.model.Chat.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn(text);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        return update;
    }
}
