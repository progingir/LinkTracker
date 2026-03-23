package backend.academy.linktracker.bot.command;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.exception.ResourceAlreadyExistsException;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartCommand implements Command {

    private final ScrapperClient scrapperClient;

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
        long chatId = update.message().chat().id();

        try {
            scrapperClient.registerChat(chatId);
            log.atInfo().addKeyValue("chat_id", chatId).log("Чат успешно зарегистрирован");
        } catch (ResourceAlreadyExistsException e) {
            log.atInfo().addKeyValue("chat_id", chatId).log("Пользователь уже был зарегистрирован ранее");
        } catch (Exception e) {
            log.atError().setCause(e).addKeyValue("chat_id", chatId).log("Ошибка при регистрации чата");
            return new SendMessage(
                    chatId,
                    "Произошла техническая ошибка при регистрации. Пожалуйста, попробуйте отправить команду /start позже");
        }

        return new SendMessage(
                chatId,
                "Привет! Я LinkTracker, помогу тебе следить за обновлениями контента. Введи /help для списка команд");
    }
}
