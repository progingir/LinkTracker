package backend.academy.linktracker.bot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramMessageSender {

    private final TelegramBot telegramBot;

    public void sendMessage(SendMessage message, Long userId) {
        BaseResponse response;
        try {
            response = telegramBot.execute(message);
        } catch (Exception e) {
            log.atError()
                .setCause(e)
                .addKeyValue("user_id", userId)
                .log("Ошибка сети при отправке в Telegram");
            throw new RuntimeException("Unexpected error sending message to user " + userId, e);
        }

        if (response != null && response.isOk()) {
            log.atDebug().addKeyValue("user_id", userId).log("Сообщение успешно отправлено пользователю");
        } else {
            String errorDescription = (response != null) ? response.description() : "Response is null";
            log.atError()
                .addKeyValue("user_id", userId)
                .addKeyValue("description", errorDescription)
                .log("Ошибка API Телеграм");

            throw new RuntimeException("Telegram API error for user " + userId + ": " + errorDescription);
        }
    }
}
