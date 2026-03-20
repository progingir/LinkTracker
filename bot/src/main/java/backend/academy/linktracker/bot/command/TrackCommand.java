package backend.academy.linktracker.bot.command;

import backend.academy.linktracker.bot.service.StateService;
import backend.academy.linktracker.bot.service.TrackState;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrackCommand implements Command {
    private final StateService stateService;

    @Override
    public String commandName() {
        return "/track";
    }

    @Override
    public String description() {
        return "Начать отслеживание ссылки";
    }

    @Override
    public SendMessage handle(Update update) {
        long chatId = update.message().chat().id();
        stateService.setState(chatId, TrackState.WAITING_FOR_LINK);
        return new SendMessage(chatId, "Пришлите ссылку на ресурс, который хотите отслеживать:");
    }
}
