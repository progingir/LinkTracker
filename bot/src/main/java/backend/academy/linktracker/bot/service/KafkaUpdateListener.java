package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaUpdateListener {

    private final BotService botService;

    @RetryableTopic(
            attempts = "${app.kafka.retry.max-attempts:3}",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            exclude = {
                DeserializationException.class,
                MethodArgumentNotValidException.class,
                ValidationException.class,
                IllegalArgumentException.class
            })
    @KafkaListener(topics = "${app.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenUpdates(@Payload @Valid LinkUpdate update) {
        log.atInfo().addKeyValue("update_id", update.id()).log("Получено обновление из Kafka, передаем в бота");

        botService.sendNotification(update);
    }
}
