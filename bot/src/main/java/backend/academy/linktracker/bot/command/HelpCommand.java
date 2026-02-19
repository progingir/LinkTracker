package backend.academy.linktracker.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;

@Component
public class HelpCommand implements Command {
    @Override
    public String command() { return "/help"; }

    @Override
    public String description() { return "Вывести список команд"; }

    @Override
    public SendMessage handle(Update update) {
        return new SendMessage(update.message().chat().id(), """
                Доступные команды:
                /start - начать работу
                /help - показать это сообщение
                /track - начать отслеживание (пока не разработано)
                /untrack - прекратить отслеживание (пока не разработано)
                /list - список ссылок (пока не разработано)
                """);
    }
}
