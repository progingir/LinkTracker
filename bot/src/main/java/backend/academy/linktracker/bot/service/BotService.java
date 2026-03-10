package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.command.Command;
import backend.academy.linktracker.bot.handler.StateHandler;
import backend.academy.linktracker.bot.repository.StateRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BotService implements UpdatesListener {

    private final TelegramBot telegramBot;
    private final List<Command> commands;
    private final StateRepository stateRepository;
    private final Map<UserState, StateHandler> stateHandlers;

    public BotService(
        TelegramBot telegramBot,
        List<Command> commands,
        StateRepository stateRepository,
        List<StateHandler> handlers) {
        this.telegramBot = telegramBot;
        this.commands = commands;
        this.stateRepository = stateRepository;
        this.stateHandlers = handlers.stream()
            .collect(Collectors.toMap(StateHandler::getHandledState, h -> h));
    }

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

    public void sendNotification(backend.academy.linktracker.bot.dto.LinkUpdate update) {
        String messageText = "🔔 Обновление по ссылке: " + update.url() + "\n" + update.description();

        for (Long chatId : update.tgChatIds()) {
            try {
                executeWithLogging(new SendMessage(chatId, messageText), chatId);
            } catch (Exception e) {
                log.atError()
                    .setCause(e)
                    .addKeyValue("chat_id", chatId)
                    .log("Не удалось отправить уведомление об обновлении");
            }
        }
    }

    private SendMessage createResponse(Update update) {
        Long userId = extractUserId(update);
        String text = update.message().text();
        long chatId = update.message().chat().id();

        var context = stateRepository.getContext(chatId);

        if (text.startsWith("/")) {
            stateRepository.clear(chatId);

            Command commandToExecute = commands.stream()
                .filter(c -> c.supports(text))
                .findFirst()
                .orElse(null);

            if (commandToExecute != null) {
                log.atInfo().addKeyValue("command", commandToExecute.commandName()).log("Выполняю команду");
                return commandToExecute.handle(update);
            } else {
                return new SendMessage(chatId, "Неизвестная команда. Воспользуйтесь /help, чтобы посмотреть список.");
            }
        }

        StateHandler handler = stateHandlers.get(context.getState());
        if (handler != null) {
            return handler.handle(update, context);
        } else {
            log.atWarn().addKeyValue("user_id", userId).addKeyValue("text", text).log("Текст вне контекста");
            return new SendMessage(chatId, "Неизвестная команда. Воспользуйтесь /help, чтобы посмотреть список доступных команд");
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
