package backend.academy.linktracker.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;

@Component
public class StartCommand implements Command {

    @Override
    public String commandName() {
        return "/start";
    }

    @Override
    public String description() {
        return "Начать работу";
    }

    @Override
    public SendMessage handle(Update update) {
        return new SendMessage(
            update.message().chat().id(),
            "Привет! Я LinkTracker, помогу тебе следить за обновлениями контента. Введи /help для списка команд"
        );
    }
}
