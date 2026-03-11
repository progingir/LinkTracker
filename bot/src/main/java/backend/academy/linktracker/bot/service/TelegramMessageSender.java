package backend.academy.linktracker.bot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramMessageSender {

    private final TelegramBot telegramBot;

    public void sendMessage(SendMessage message, Long userId) {
        try {
            SendResponse response = telegramBot.execute(message);

            if (response.isOk()) {
                log.atDebug().addKeyValue("user_id", userId).log("Сообщение успешно отправлено пользователю");
            } else {
                log.atError()
                        .addKeyValue("user_id", userId)
                        .addKeyValue("description", response.description())
                        .log("Ошибка API Телеграм для пользователя");
            }
        } catch (Throwable e) {
            log.atError()
                    .setCause(e)
                    .addKeyValue("user_id", userId)
                    .log("Непредвиденная ошибка при отправке сообщения пользователю");
        }
    }
}
