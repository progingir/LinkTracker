package backend.academy.linktracker.bot.handler;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.exception.ScrapperApiException;
import backend.academy.linktracker.bot.repository.StateRepository;
import backend.academy.linktracker.bot.service.UserState;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WaitingForTagsHandler implements StateHandler {
    private final ScrapperClient scrapperClient;
    private final StateRepository stateRepository;

    @Override
    public UserState getHandledState() {
        return UserState.WAITING_FOR_TAGS;
    }

    @Override
    public SendMessage handle(Update update, StateRepository.UserContext context) {
        long chatId = update.message().chat().id();
        String text = update.message().text().trim();
        URI link = context.getPendingLink();

        List<String> tags;

        if (text.equalsIgnoreCase("нет") || text.equalsIgnoreCase("пропустить") || text.equals("-")) {
            tags = List.of();
        } else {
            tags = Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        }

        try {
            scrapperClient.addLink(chatId, link, tags);
            stateRepository.clear(chatId);
            return new SendMessage(chatId, "✅ Ссылка успешно добавлена!");
        } catch (Exception e) {
            stateRepository.clear(chatId);
            return new SendMessage(chatId, "❌ Ошибка при добавлении в Scrapper API. Возможно чат не зарегистрирован.");
        }
    }
}
