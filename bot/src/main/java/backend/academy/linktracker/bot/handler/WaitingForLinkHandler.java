package backend.academy.linktracker.bot.handler;

import backend.academy.linktracker.bot.repository.StateRepository;
import backend.academy.linktracker.bot.service.LinkValidator;
import backend.academy.linktracker.bot.service.StateService;
import backend.academy.linktracker.bot.service.TrackState;
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
public class WaitingForLinkHandler implements StateHandler {

    private final StateService stateService;
    private final LinkValidator linkValidator;

    @Override
    public UserState getHandledState() {
        return TrackState.WAITING_FOR_LINK;
    }

    @Override
    public SendMessage handle(Update update, StateRepository.UserContext context) {
        long chatId = update.message().chat().id();
        String text = update.message().text();

        try {
            URI uri = linkValidator.validate(text);

            stateService.setPendingLink(chatId, uri);
            stateService.setState(chatId, TrackState.WAITING_FOR_TAGS);

            return new SendMessage(
                    chatId, "✅ Ссылка принята. Введите теги через запятую (или отправьте 'нет', чтобы пропустить):");

        } catch (IllegalArgumentException e) {
            return new SendMessage(
                    chatId,
                    "❌ Неверный формат ссылки. Пожалуйста, отправьте корректный URL (начинающийся с http/https).");

        } catch (Exception e) {
            log.atError()
                    .setCause(e)
                    .addKeyValue("chat_id", chatId)
                    .addKeyValue("input_text", text)
                    .log("Критическая ошибка при обработке ссылки");

            stateService.clear(chatId);

            return new SendMessage(chatId, "❌ Произошла внутренняя ошибка сервера. Попробуйте позже.");
        }
    }
}
