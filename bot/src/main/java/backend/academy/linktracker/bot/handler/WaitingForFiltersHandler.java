package backend.academy.linktracker.bot.handler;

import backend.academy.linktracker.bot.client.ScrapperClient;
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
public class WaitingForFiltersHandler implements StateHandler {
    private final ScrapperClient scrapperClient;
    private final StateRepository stateRepository;

    @Override
    public UserState getHandledState() {
        return UserState.WAITING_FOR_FILTERS;
    }

    @Override
    public SendMessage handle(Update update, StateRepository.UserContext context) {
        long chatId = update.message().chat().id();
        String text = update.message().text().trim();
        URI link = context.getPendingLink();
        List<String> tags = context.getPendingTags();

        List<String> filters = (text.equalsIgnoreCase("нет") || text.equals("-"))
                ? List.of()
                : Arrays.stream(text.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();

        try {
            scrapperClient.addLink(chatId, link, tags, filters);
            stateRepository.clear(chatId);
            return new SendMessage(chatId, "✅ Ссылка успешно добавлена со всеми настройками!");
        } catch (Exception e) {
            stateRepository.clear(chatId);

            if (e.getMessage() != null && e.getMessage().contains("404")) {
                return new SendMessage(chatId, "❌ Ошибка: вы еще не зарегистрированы. Введите /start");
            }

            return new SendMessage(chatId, "Ссылка уже отслеживается");
        }
    }
}
