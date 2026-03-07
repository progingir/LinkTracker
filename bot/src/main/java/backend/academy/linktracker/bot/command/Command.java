package backend.academy.linktracker.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

public interface Command {

    String commandName();

    String description();

    SendMessage handle(Update update);

    default boolean supports(String text) {
        return text.equals(commandName());
    }
}
