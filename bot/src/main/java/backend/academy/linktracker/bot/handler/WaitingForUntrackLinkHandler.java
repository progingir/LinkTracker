package backend.academy.linktracker.bot.handler;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.exception.ScrapperApiException;
import backend.academy.linktracker.bot.repository.StateRepository;
import backend.academy.linktracker.bot.service.UserState;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WaitingForUntrackLinkHandler implements StateHandler {
    private final ScrapperClient scrapperClient;
    private final StateRepository stateRepository;

    @Override
    public UserState getHandledState() {
        return UserState.WAITING_FOR_UNTRACK_LINK;
    }

    @Override
    public SendMessage handle(Update update, StateRepository.UserContext context) {
        long chatId = update.message().chat().id();
        String text = update.message().text();

        try {
            URI uri = new URI(text);
            scrapperClient.removeLink(chatId, uri);
            stateRepository.clear(chatId);
            return new SendMessage(chatId, "✅ Ссылка удалена из списка.");
        } catch (ScrapperApiException e) {
            stateRepository.clear(chatId);
            if (e.getStatusCode() == 404) {
                return new SendMessage(chatId, "❌ Ошибка: ссылка не найдена.");
            }
            return new SendMessage(chatId, "❌ Ошибка при удалении ссылки.");
        } catch (Exception e) {
            stateRepository.clear(chatId);
            return new SendMessage(chatId, "❌ Ошибка: неверный формат ссылки.");
        }
    }
}
