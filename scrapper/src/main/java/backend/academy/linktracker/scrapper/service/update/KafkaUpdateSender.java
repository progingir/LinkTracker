package backend.academy.linktracker.scrapper.service.update;

import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaUpdateSender {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic}")
    private String topicName;

    public void sendUpdate(LinkUpdate update) {
        try {
            kafkaTemplate.send(topicName, update.id().toString(), update).get();

            log.atDebug().addKeyValue("update_id", update.id()).log("Сообщение доставлено в Kafka");

        } catch (InterruptedException | ExecutionException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException("Ошибка доставки сообщения в Kafka", e);
        }
    }
}
