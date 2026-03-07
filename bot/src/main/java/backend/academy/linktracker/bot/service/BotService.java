package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.command.Command;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotService implements UpdatesListener {

    private final TelegramBot telegramBot;
    private final List<Command> commands;

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            if (update.message() == null || update.message().text() == null) {
                continue;
            }

            try {
                SendMessage response = createResponse(update);

                if (response != null) {
                    Long userId = extractUserId(update);
                    executeWithLogging(response, userId);
                }
            } catch (Exception e) {
                log.atError()
                        .setCause(e)
                        .addKeyValue("update_id", update.updateId())
                        .log("Критический сбой при обработке обновления");
            }
        }
        return CONFIRMED_UPDATES_ALL;
    }

    private SendMessage createResponse(Update update) {
        Long userId = extractUserId(update);
        String username = extractUsername(update);
        String text = update.message().text();

        Command commandToExecute =
                commands.stream().filter(c -> c.supports(text)).findFirst().orElse(null);

        if (commandToExecute != null) {
            log.atInfo()
                    .addKeyValue("command_name", commandToExecute.commandName())
                    .addKeyValue("user_id", userId)
                    .addKeyValue("username", username)
                    .log("Выполняю команду");

            return commandToExecute.handle(update);
        } else {
            log.atWarn()
                    .addKeyValue("raw_text", text)
                    .addKeyValue("user_id", userId)
                    .log("Получена неизвестная команда");

            return new SendMessage(
                    (long) update.message().chat().id(),
                    "Неизвестная команда. Воспользуйтесь /help, чтобы посмотреть список доступных команд");
        }
    }

    private void executeWithLogging(SendMessage message, Long userId) {
        try {
            SendResponse response = telegramBot.execute(message);

            if (response.isOk()) {
                log.atDebug().addKeyValue("user_id", userId).log("Сообщение успешно отправлено пользователю");
            } else {
                log.atError()
                        .addKeyValue("user_id", userId)
                        .addKeyValue("description", response.description())
                        .log("Ошибка API Телеграм для пользователя");
            }
        } catch (Throwable e) {
            log.atError()
                    .setCause(e)
                    .addKeyValue("user_id", userId)
                    .log("Непредвиденная ошибка при отправке сообщения пользователю");
        }
    }

    private Long extractUserId(Update update) {
        return (update.message().from() != null) ? update.message().from().id() : 0L;
    }

    private String extractUsername(Update update) {
        var from = update.message().from();
        if (from == null) {
            return "unknown";
        }
        if (from.username() != null) {
            return from.username();
        }
        return from.firstName() != null ? from.firstName() : "id:" + from.id();
    }
}
