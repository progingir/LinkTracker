package backend.academy.linktracker.bot.handler;

import backend.academy.linktracker.bot.repository.StateRepository;
import backend.academy.linktracker.bot.service.UserState;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WaitingForLinkHandler implements StateHandler {

    private final StateRepository stateRepository;

    @Override
    public UserState getHandledState() {
        return UserState.WAITING_FOR_LINK;
    }

    @Override
    public SendMessage handle(Update update, StateRepository.UserContext context) {
        long chatId = update.message().chat().id();
        String text = update.message().text().trim();

        try {
            URI uri = URI.create(text);

            if (uri.getScheme() == null || !uri.getScheme().startsWith("http")) {
                throw new IllegalArgumentException();
            }

            stateRepository.setPendingLink(chatId, uri);
            stateRepository.setState(chatId, UserState.WAITING_FOR_TAGS);

            return new SendMessage(
                    chatId, "Ссылка принята. Введите теги через запятую (или отправьте 'нет', чтобы пропустить):");

        } catch (Exception e) {
            return new SendMessage(
                    chatId,
                    "❌ Неверный формат ссылки. Пожалуйста, отправьте корректный URL (начинающийся с http/https).");
        }
    }
}
