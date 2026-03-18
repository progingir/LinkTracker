package backend.academy.linktracker.bot;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.exception.ResourceAlreadyExistsException;
import backend.academy.linktracker.bot.handler.WaitingForFiltersHandler;
import backend.academy.linktracker.bot.handler.WaitingForLinkHandler;
import backend.academy.linktracker.bot.handler.WaitingForTagsHandler;
import backend.academy.linktracker.bot.repository.StateRepository;
import backend.academy.linktracker.bot.service.UserState;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BotStateMachineTest {

    private StateRepository stateRepository;
    private ScrapperClient scrapperClient;
    private WaitingForLinkHandler linkHandler;
    private WaitingForTagsHandler tagsHandler;
    private WaitingForFiltersHandler filtersHandler;

    @BeforeEach
    void setUp() {
        stateRepository = new StateRepository();
        scrapperClient = mock(ScrapperClient.class);
        linkHandler = new WaitingForLinkHandler(stateRepository);
        tagsHandler = new WaitingForTagsHandler(stateRepository);
        filtersHandler = new WaitingForFiltersHandler(scrapperClient, stateRepository);
    }

    @Test
    @DisplayName("Полный сценарий /track: ссылка -> теги -> фильтры")
    void fullTrackFlow() {
        long chatId = 123L;
        String url = "https://github.com/user/repo";

        Update update1 = mockUpdate(url, chatId);
        linkHandler.handle(update1, stateRepository.getContext(chatId));

        var context = stateRepository.getContext(chatId);
        assertTrue(context.getState() == UserState.WAITING_FOR_TAGS);
        assertTrue(context.getPendingLink().toString().equals(url));

        Update update2 = mockUpdate("java, spring", chatId);
        tagsHandler.handle(update2, stateRepository.getContext(chatId));

        context = stateRepository.getContext(chatId);
        assertTrue(context.getState() == UserState.WAITING_FOR_FILTERS);
        assertTrue(context.getPendingTags().containsAll(List.of("java", "spring")));

        Update update3 = mockUpdate("нет", chatId);
        SendMessage finalResponse = filtersHandler.handle(update3, stateRepository.getContext(chatId));

        assertTrue(finalResponse.getParameters().get("text").toString().contains("успешно добавлена"));
        verify(scrapperClient).addLink(eq(chatId), any(URI.class), any(), any());
        assertTrue(stateRepository.getContext(chatId).getState() == UserState.NONE);
    }

    @Test
    @DisplayName("Ошибка: ссылка уже отслеживается")
    void alreadyTrackedLink() {
        long chatId = 123L;
        stateRepository.setState(chatId, UserState.WAITING_FOR_FILTERS);
        stateRepository.setPendingLink(chatId, URI.create("https://github.com/user/repo"));

        when(scrapperClient.addLink(any(), any(), any(), any()))
                .thenThrow(new ResourceAlreadyExistsException("Ссылка уже отслеживается"));

        Update update = mockUpdate("нет", chatId);
        SendMessage response = filtersHandler.handle(update, stateRepository.getContext(chatId));

        assertTrue(response.getParameters().get("text").toString().contains("уже отслеживается"));
    }

    private Update mockUpdate(String text, long chatId) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn(text);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        return update;
    }
}
