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
        String messageText = update.message().text();
        stateRepository.clear(chatId);

        try {
            ListLinksResponse response = scrapperClient.getLinks(chatId);
            if (response.links().isEmpty()) {
                return new SendMessage(
                        chatId, "Ваш список отслеживания пуст. Используйте /track, чтобы добавить ссылку.");
            }

            String[] parts = messageText.split("\\s+", 2);
            String filterTag = parts.length > 1 ? parts[1].trim() : null;

            var linksToShow = response.links();

            if (filterTag != null && !filterTag.isEmpty()) {
                linksToShow = linksToShow.stream()
                        .filter(link -> link.tags() != null && link.tags().contains(filterTag))
                        .toList();

                if (linksToShow.isEmpty()) {
                    return new SendMessage(chatId, "По тегу '" + filterTag + "' ссылок не найдено.");
                }
            }

            String listText = linksToShow.stream()
                    .map(link -> "• " + link.url()
                            + (link.tags() == null || link.tags().isEmpty()
                                    ? ""
                                    : " (теги: " + String.join(", ", link.tags()) + ")"))
                    .collect(Collectors.joining("\n", "Вы отслеживаете следующие ресурсы:\n", ""));

            return new SendMessage(chatId, listText);
        } catch (Exception e) {
            return new SendMessage(
                    chatId, "❌ Не удалось получить список. Возможно, вы еще не зарегистрированы? Введите /start");
        }
    }
}
