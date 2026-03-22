package backend.academy.linktracker.bot.command;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.dto.ListLinksResponse;
import backend.academy.linktracker.bot.exception.ResourceNotFoundException;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ListCommand implements Command {

    private final ScrapperClient scrapperClient;

    @Override
    public String commandName() {
        return "/list";
    }

    @Override
    public String description() {
        return "Показать список отслеживаемых ссылок";
    }

    @Override
    public SendMessage handle(Update update) {
        long chatId = update.message().chat().id();

        try {
            ListLinksResponse response = scrapperClient.getLinks(chatId);
            if (response.links().isEmpty()) {
                return new SendMessage(
                        chatId, "Ваш список отслеживания пуст. Используйте /track, чтобы добавить ссылку.");
            }

            String listText = response.links().stream()
                    .map(link -> "• " + link.url()
                            + (link.tags().isEmpty() ? "" : " (теги: " + String.join(", ", link.tags()) + ")"))
                    .collect(Collectors.joining("\n", "Вы отслеживаете следующие ресурсы:\n", ""));

            return new SendMessage(chatId, listText);
        } catch (ResourceNotFoundException e) {
            log.info("Чат {} не найден в системе при вызове /list", chatId);
            return new SendMessage(chatId, "❌ Вы еще не зарегистрированы. Введите /start, чтобы начать работу.");

        } catch (Exception e) {
            log.error("Ошибка при получении списка ссылок для чата {}", chatId, e);

            return new SendMessage(
                    chatId, "⚠️ Не удалось получить список из-за технической ошибки на сервере. Попробуйте позже.");
        }
    }
}
