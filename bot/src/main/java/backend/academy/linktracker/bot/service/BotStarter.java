package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.command.Command;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class BotStarter implements ApplicationRunner {

    private final TelegramBot telegramBot;
    private final BotService botService;
    private final List<Command> commands;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Бот начинает слушать сообщения...");
        telegramBot.setUpdatesListener(botService);

        BotCommand[] botCommands = new BotCommand[commands.size()];

        for (int i = 0; i < commands.size(); i++) {
            Command cmd = commands.get(i);
            botCommands[i] = new BotCommand(cmd.commandName(), cmd.description());
        }

        SetMyCommands setMyCommands = new SetMyCommands(botCommands);
        var response = telegramBot.execute(setMyCommands);

        if (response.isOk()) {
            log.atInfo().addKeyValue("commands_count", commands.size()).log("Меню команд успешно зарегистрировано");
        } else {
            log.atError()
                    .addKeyValue("error_code", response.errorCode())
                    .addKeyValue("description", response.description())
                    .log("Ошибка при регистрации меню команд");
        }
    }
}
