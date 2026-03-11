package backend.academy.linktracker.bot.command;

import backend.academy.linktracker.bot.repository.StateRepository;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class HelpCommand implements Command {

    private final CommandRegistry commandRegistry;
    private final StateRepository stateRepository;

    public HelpCommand(@Lazy CommandRegistry commandRegistry, StateRepository stateRepository) {
        this.commandRegistry = commandRegistry;
        this.stateRepository = stateRepository;
    }

    @Override
    public String commandName() {
        return "/help";
    }

    @Override
    public String description() {
        return "Вывести список команд";
    }

    @Override
    public SendMessage handle(Update update) {
        long chatId = update.message().chat().id();
        stateRepository.clear(chatId);

        String helpText = commandRegistry.getCommandsMetadata().entrySet().stream()
                .map(entry -> entry.getKey() + " - " + entry.getValue())
                .collect(Collectors.joining("\n", "Доступные команды:\n", ""));

        return new SendMessage(chatId, helpText);
    }
}
