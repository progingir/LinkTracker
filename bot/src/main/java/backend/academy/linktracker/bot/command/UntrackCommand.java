package backend.academy.linktracker.bot.command;

import backend.academy.linktracker.bot.repository.StateRepository;
import backend.academy.linktracker.bot.service.UserState;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UntrackCommand implements Command {
    private final StateRepository stateRepository;

    @Override
    public String commandName() {
        return "/untrack";
    }

    @Override
    public String description() {
        return "Прекратить отслеживание ссылки";
    }

    @Override
    public SendMessage handle(Update update) {
        long chatId = update.message().chat().id();
        stateRepository.setState(chatId, UserState.WAITING_FOR_UNTRACK_LINK);
        return new SendMessage(chatId, "Пришлите ссылку, которую хотите перестать отслеживать:");
    }
}
