package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.grpc.BotServiceGrpc;
import backend.academy.linktracker.grpc.LinkUpdateMsg;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.properties.BotClientProperties;
import backend.academy.linktracker.scrapper.service.update.KafkaMessageProducer;
import backend.academy.linktracker.scrapper.service.update.UpdateSender;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.use-queue", havingValue = "false")
public class BotNotificationClient implements UpdateSender {

    private final BotServiceGrpc.BotServiceBlockingStub botServiceStub;
    private final BotClientProperties properties;
    private final KafkaMessageProducer kafkaProducer;

    @Override
    @CircuitBreaker(name = "bot", fallbackMethod = "sendToKafkaFallback")
    @Retry(name = "bot", fallbackMethod = "sendToKafkaFallback")
    public void sendUpdate(LinkUpdate update) {
        botServiceStub
                .withDeadlineAfter(properties.getDeadline().toMillis(), TimeUnit.MILLISECONDS)
                .sendUpdate(LinkUpdateMsg.newBuilder()
                        .setId(update.id())
                        .setUrl(update.url().toString())
                        .setDescription(update.description())
                        .addAllTgChatIds(update.tgChatIds())
                        .build());

        log.atInfo()
                .addKeyValue("link_id", update.id())
                .addKeyValue("url", update.url())
                .log("Уведомление успешно отправлено по gRPC");
    }

    public void sendToKafkaFallback(LinkUpdate update, Throwable e) {
        log.atWarn()
                .setCause(e)
                .addKeyValue("link_id", update.id())
                .log("Fallback: Основной транспорт недоступен. Отправка через Kafka");
        kafkaProducer.send(update);
    }
}
