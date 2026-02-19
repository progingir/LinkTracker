package backend.academy.linktracker.bot.service;

import com.pengrad.telegrambot.TelegramBot;
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

    @Override
    public void run(ApplicationArguments args) {
        log.info("Бот начинает слушать сообщения...");
        telegramBot.setUpdatesListener(botService);
    }
}
