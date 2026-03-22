package backend.academy.linktracker.bot.handler;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.exception.ResourceAlreadyExistsException;
import backend.academy.linktracker.bot.exception.ResourceNotFoundException;
import backend.academy.linktracker.bot.repository.StateRepository;
import backend.academy.linktracker.bot.service.StateService;
import backend.academy.linktracker.bot.state.TrackState;
import backend.academy.linktracker.bot.state.UserState;
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
public class WaitingForTagsHandler implements StateHandler {

    private final StateService stateService;
    private final ScrapperClient scrapperClient;

    @Override
    public UserState getHandledState() {
        return TrackState.WAITING_FOR_TAGS;
    }

    @Override
    public SendMessage handle(Update update, StateRepository.UserContext context) {
        long chatId = update.message().chat().id();
        String text = update.message().text().trim();

        URI link = context.getPendingLink();

        List<String> tags = (text.equalsIgnoreCase("нет") || text.equals("-"))
                ? List.of()
                : Arrays.stream(text.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();

        try {
            scrapperClient.addLink(chatId, link, tags);

            stateService.clear(chatId);
            return new SendMessage(chatId, "✅ Ссылка успешно добавлена в список отслеживания!");

        } catch (ResourceNotFoundException e) {
            stateService.clear(chatId);
            log.atInfo()
                    .addKeyValue("chat_id", chatId)
                    .log("Попытка добавления ссылки незарегистрированным пользователем");
            return new SendMessage(
                    chatId, "❌ Ошибка: вы еще не зарегистрированы. Введите /start, чтобы начать работу.");

        } catch (ResourceAlreadyExistsException e) {
            stateService.clear(chatId);
            return new SendMessage(chatId, "⚠️ Эта ссылка уже отслеживается в вашем списке.");

        } catch (Exception e) {
            stateService.clear(chatId);
            log.atError()
                    .setCause(e)
                    .addKeyValue("chat_id", chatId)
                    .log("Непредвиденная ошибка при добавлении ссылки в Scrapper");
            return new SendMessage(chatId, "❌ Произошла техническая ошибка на сервере. Попробуйте позже.");
        }
    }
}
