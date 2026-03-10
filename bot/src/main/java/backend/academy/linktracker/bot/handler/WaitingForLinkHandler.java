package backend.academy.linktracker.bot.handler;

import backend.academy.linktracker.bot.client.ScrapperClient;
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
    private final ScrapperClient scrapperClient;
    private final StateRepository stateRepository;

    @Override
    public UserState getHandledState() {
        return UserState.WAITING_FOR_LINK;
    }

    @Override
    public SendMessage handle(Update update, StateRepository.UserContext context) {
        long chatId = update.message().chat().id();
        String text = update.message().text();

        try {
            URI uri = new URI(text);
            String scheme = uri.getScheme();

            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) || uri.getHost() == null) {
                return new SendMessage(chatId, "❌ Это не похоже на ссылку. Убедитесь, что ссылка начинается с http:// или https://. Попробуйте еще раз или введите /cancel для отмены:");
            }

            var response = scrapperClient.getLinks(chatId);
            boolean alreadyExists = response.links().stream().anyMatch(l -> l.url().equals(uri));

            if (alreadyExists) {
                stateRepository.clear(chatId);
                return new SendMessage(chatId, "Ссылка уже отслеживается");
            }

            stateRepository.setPendingLink(chatId, uri);
            stateRepository.setState(chatId, UserState.WAITING_FOR_TAGS);
            return new SendMessage(chatId, "Ссылка принята. Теперь введите теги через запятую (или отправьте 'нет', чтобы пропустить):");
        } catch (Exception e) {
            return new SendMessage(chatId, "❌ Это не похоже на ссылку. Попробуйте еще раз или введите /cancel для отмены:");
        }
    }
}
