package backend.academy.linktracker.bot.command;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.dto.ListLinksResponse;
import backend.academy.linktracker.bot.repository.StateRepository;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListCommand implements Command {

    private final ScrapperClient scrapperClient;
    private final StateRepository stateRepository;

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

        stateRepository.clear(chatId);

        try {
            ListLinksResponse response = scrapperClient.getLinks(chatId);
            if (response.links().isEmpty()) {
                return new SendMessage(chatId, "Ваш список отслеживания пуст. Используйте /track, чтобы добавить ссылку.");
            }

            String listText = response.links().stream()
                .map(link -> "• " + link.url() + (link.tags().isEmpty() ? "" : " (теги: " + String.join(", ", link.tags()) + ")"))
                .collect(Collectors.joining("\n", "Вы отслеживаете следующие ресурсы:\n", ""));

            return new SendMessage(chatId, listText);
        } catch (Exception e) {
            return new SendMessage(chatId, "❌ Не удалось получить список. Возможно, вы еще не зарегистрированы? Введите /start");
        }
    }
}
