package backend.academy.linktracker.bot.service;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class BotService implements UpdatesListener {

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            if (update.message() != null && update.message().text() != null) {
                String messageText = update.message().text();
                String username = update.message().from().username();

                log.info("Получили сообщение от {}: {}", username, messageText);
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
