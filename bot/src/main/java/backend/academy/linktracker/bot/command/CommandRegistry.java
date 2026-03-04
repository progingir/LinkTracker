package backend.academy.linktracker.bot.command;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class CommandRegistry {

    private final Map<String, String> commandsMetadata;

    public CommandRegistry(List<Command> commands) {
        this.commandsMetadata = commands.stream()
            .collect(Collectors.toMap(
                Command::commandName,
                Command::description
            ));
    }
}
