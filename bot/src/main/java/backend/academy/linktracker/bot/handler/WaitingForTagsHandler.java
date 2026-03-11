package backend.academy.linktracker.bot.handler;

import backend.academy.linktracker.bot.repository.StateRepository;
import backend.academy.linktracker.bot.service.UserState;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WaitingForTagsHandler implements StateHandler {
    private final StateRepository stateRepository;

    @Override
    public UserState getHandledState() {
        return UserState.WAITING_FOR_TAGS;
    }

    @Override
    public SendMessage handle(Update update, StateRepository.UserContext context) {
        long chatId = update.message().chat().id();
        String text = update.message().text().trim();

        List<String> tags = (text.equalsIgnoreCase("нет") || text.equals("-"))
                ? List.of()
                : Arrays.stream(text.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();

        stateRepository.setPendingTags(chatId, tags);
        stateRepository.setState(chatId, UserState.WAITING_FOR_FILTERS);

        return new SendMessage(
                chatId, "Теги приняты. Теперь введите фильтры через запятую (или отправьте 'нет', чтобы пропустить):");
    }
}
