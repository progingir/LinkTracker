package backend.academy.linktracker.bot.command;

import backend.academy.linktracker.bot.repository.StateRepository;
import backend.academy.linktracker.bot.service.UserState;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CancelCommand implements Command {

    private final StateRepository stateRepository;

    @Override
    public String commandName() { return "/cancel"; }

    @Override
    public String description() { return "Отменить текущую операцию"; }

    @Override
    public SendMessage handle(Update update) {
        long chatId = update.message().chat().id();
        var context = stateRepository.getContext(chatId);

        if (context.getState() == UserState.NONE) {
            return new SendMessage(chatId, "Сейчас нет активных операций для отмены. Введите /track, чтобы начать.");
        }

        stateRepository.clear(chatId);
        return new SendMessage(chatId, "🔄 Операция отменена. Я готов к новым командам.");
    }
}
