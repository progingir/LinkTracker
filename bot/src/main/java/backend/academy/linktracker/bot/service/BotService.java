package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.command.Command;
import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.bot.handler.StateHandler;
import backend.academy.linktracker.bot.state.UserState;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BotService implements UpdatesListener {

    private final TelegramMessageSender messageSender;
    private final List<Command> commands;
    private final StateService stateService;
    private final Map<UserState, StateHandler> stateHandlers;

    public BotService(
            TelegramMessageSender messageSender,
            List<Command> commands,
            StateService stateService,
            List<StateHandler> handlers) {
        this.messageSender = messageSender;
        this.commands = commands;
        this.stateService = stateService;
        this.stateHandlers = handlers.stream().collect(Collectors.toMap(StateHandler::getHandledState, h -> h));
    }

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            if (update.message() == null || update.message().text() == null) {
                continue;
            }

            Long chatId = update.message().chat().id();

            try {
                SendMessage response = createResponse(update);

                if (response != null) {
                    messageSender.sendMessage(response, chatId);
                }
            } catch (Exception e) {
                log.atError()
                        .setCause(e)
                        .addKeyValue("chat_id", chatId)
                        .addKeyValue("update_id", update.updateId())
                        .log("Критический сбой при обработке обновления");
            }
        }
        return CONFIRMED_UPDATES_ALL;
    }

    public void sendNotification(LinkUpdate update) {
        String messageText = update.isSystemReport()
                ? update.description()
                : "🔔 *Обновление по ссылке:* " + update.url() + "\n\n" + update.description();

        for (Long chatId : update.tgChatIds()) {
            try {
                SendMessage message = new SendMessage(chatId, messageText);
                messageSender.sendMessage(message, chatId);
            } catch (Exception e) {
                log.atError().setCause(e).addKeyValue("chat_id", chatId).log("Не удалось отправить уведомление");
            }
        }
    }

    private SendMessage createResponse(Update update) {
        String text = update.message().text();
        long chatId = update.message().chat().id();

        var context = stateService.getContext(chatId);

        if (text.startsWith("/")) {
            stateService.clear(chatId);

            Command commandToExecute =
                    commands.stream().filter(c -> c.supports(text)).findFirst().orElse(null);

            if (commandToExecute != null) {
                log.atInfo()
                        .addKeyValue("command", commandToExecute.commandName())
                        .addKeyValue("chat_id", chatId)
                        .log("Выполняю команду");
                return commandToExecute.handle(update);
            } else {
                return new SendMessage(chatId, "Неизвестная команда. Воспользуйтесь /help.");
            }
        }

        StateHandler handler = stateHandlers.get(context.getState());
        if (handler != null) {
            return handler.handle(update, context);
        } else {
            log.atWarn()
                    .addKeyValue("chat_id", chatId)
                    .addKeyValue("text", text)
                    .log("Получен текст вне контекста диалога");
            return new SendMessage(chatId, "Неизвестная команда. Воспользуйтесь /help.");
        }
    }
}
