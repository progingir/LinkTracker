package backend.academy.linktracker.bot.handler;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.exception.ResourceNotFoundException;
import backend.academy.linktracker.bot.repository.StateRepository;
import backend.academy.linktracker.bot.service.LinkValidator;
import backend.academy.linktracker.bot.service.StateService;
import backend.academy.linktracker.bot.service.UntrackState;
import backend.academy.linktracker.bot.service.UserState;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaitingForUntrackLinkHandler implements StateHandler {

    private final ScrapperClient scrapperClient;
    private final StateService stateService;
    private final LinkValidator linkValidator;

    @Override
    public UserState getHandledState() {
        return UntrackState.WAITING_FOR_UNTRACK_LINK;
    }

    @Override
    public SendMessage handle(Update update, StateRepository.UserContext context) {
        long chatId = update.message().chat().id();
        String text = update.message().text();

        try {
            URI uri = linkValidator.validate(text);

            scrapperClient.removeLink(chatId, uri);
            stateService.clear(chatId);
            return new SendMessage(chatId, "✅ Ссылка успешно удалена из вашего списка.");

        } catch (IllegalArgumentException e) {
            return new SendMessage(chatId, "❌ Неверный формат ссылки. Пожалуйста, пришлите корректный URL.");

        } catch (ResourceNotFoundException e) {
            stateService.clear(chatId);
            return new SendMessage(chatId, "❌ Ошибка: данная ссылка не найдена в вашем списке отслеживания.");

        } catch (Exception e) {
            stateService.clear(chatId);
            log.atError().setCause(e).addKeyValue("chat_id", chatId).log("Критическая ошибка при удалении ссылки");

            return new SendMessage(chatId, "❌ Произошла техническая ошибка. Попробуйте позже.");
        }
    }
}
