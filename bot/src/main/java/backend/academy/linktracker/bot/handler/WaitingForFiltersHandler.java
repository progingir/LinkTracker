package backend.academy.linktracker.bot.handler;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.exception.ResourceAlreadyExistsException;
import backend.academy.linktracker.bot.exception.ResourceNotFoundException;
import backend.academy.linktracker.bot.repository.StateRepository;
import backend.academy.linktracker.bot.service.UserState;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
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

        } catch (ResourceNotFoundException e) {
            stateRepository.clear(chatId);
            log.atInfo()
                    .addKeyValue("chat_id", chatId)
                    .log("Попытка добавления ссылки незарегистрированным пользователем");
            return new SendMessage(chatId, "❌ Ошибка: вы еще не зарегистрированы. Введите /start");

        } catch (ResourceAlreadyExistsException e) {
            stateRepository.clear(chatId);
            return new SendMessage(chatId, "⚠️ Эта ссылка уже отслеживается в вашем списке.");

        } catch (Exception e) {
            stateRepository.clear(chatId);

            log.atError()
                    .setCause(e)
                    .addKeyValue("chat_id", chatId)
                    .log("Непредвиденная ошибка при добавлении ссылки в Scrapper");

            return new SendMessage(chatId, "❌ Произошла техническая ошибка на сервере. Попробуйте позже.");
        }
    }
}
