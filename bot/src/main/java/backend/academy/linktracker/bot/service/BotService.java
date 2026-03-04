package backend.academy.linktracker.bot.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

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
                SendMessage response = processUpdate(update);

                if (response != null) {
                    Long userId = extractUserId(update);
                    executeWithLogging(response, userId);
                }
            } catch (Exception e) {
                log.error("Критический сбой при обработке обновления {}:", update.updateId(), e);
            }
        }
        return CONFIRMED_UPDATES_ALL;
    }

    private SendMessage processUpdate(Update update) {
        Long userId = extractUserId(update);
        String username = extractUsername(update);
        String text = update.message().text();

        Command commandToExecute =
                commands.stream().filter(c -> c.supports(update)).findFirst().orElse(null);

        if (commandToExecute != null) {
            log.info(
                    "Выполняю команду {} для пользователя [id={}, name={}]",
                    kv("command_name", commandToExecute.commandName()),
                    kv("user_id", userId),
                    kv("username", username));
            return commandToExecute.handle(update);
        } else {
            log.warn("Неизвестная команда '{}' от пользователя [id={}]", kv("raw_text", text), kv("user_id", userId));

            return new SendMessage(
                    update.message().chat().id(),
                    "Неизвестная команда. Воспользуйтесь /help, чтобы посмотреть список доступных команд");
        }
    }

    private void executeWithLogging(SendMessage message, Long userId) {
        try {
            SendResponse response = telegramBot.execute(message);
            if (response != null) {
                if (response.isOk()) {
                    log.debug("Сообщение успешно отправлено пользователю {}", kv("user_id", userId));
                } else {
                    log.error(
                            "Ошибка API Телеграм для пользователя {}: {}",
                            kv("user_id", userId),
                            response.description());
                }
            }
        } catch (Throwable e) {
            log.error("Непредвиденная ошибка при отправке сообщения пользователю {}:", userId, e);
        }
    }

    private Long extractUserId(Update update) {
        return (update.message().from() != null) ? update.message().from().id() : 0L;
    }

    private String extractUsername(Update update) {
        if (update.message().from() == null) {
            return "unknown";
        }
        var from = update.message().from();
        if (from.username() != null) {
            return from.username();
        }
        return from.firstName() != null ? from.firstName() : "id:" + from.id();
    }
}
