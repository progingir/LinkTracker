package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.command.Command;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotService implements UpdatesListener {

    private final TelegramBot telegramBot;

    private final List<Command> commands;

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            if (update.message() != null && update.message().text() != null) {
                processUpdate(update);
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void processUpdate(Update update) {
        Command commandToExecute = commands.stream()
            .filter(c -> c.supports(update))
            .findFirst()
            .orElse(null);

        SendMessage response;
        if (commandToExecute != null) {
            log.info("Выполняю команду {} для пользователя {}",
                commandToExecute.command(), update.message().from().username());
            response = commandToExecute.handle(update);
        } else {
            log.warn("Неизвестная команда от {}: {}",
                update.message().from().username(), update.message().text());
            response = new SendMessage(update.message().chat().id(),
                "Неизвестная команда. Воспользуйтесь /help, чтобы посмотреть список доступных команд");
        }

        telegramBot.execute(response);
    }
}
