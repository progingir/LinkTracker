package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.grpc.BotServiceGrpc;
import backend.academy.linktracker.grpc.LinkUpdateMsg;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import io.grpc.StatusRuntimeException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotNotificationClient {

    private final BotServiceGrpc.BotServiceBlockingStub stub;

    @Value("${app.grpc.bot-deadline:5s}")
    private Duration deadline;

    public void sendUpdate(LinkUpdate update) {
        try {
            stub.withDeadlineAfter(deadline.toMillis(), TimeUnit.MILLISECONDS)
                    .sendUpdate(LinkUpdateMsg.newBuilder()
                            .setId(update.id())
                            .setUrl(update.url().toString())
                            .setDescription(update.description())
                            .addAllTgChatIds(update.tgChatIds())
                            .build());
            log.info("Уведомление успешно отправлено по gRPC");
        } catch (StatusRuntimeException e) {
            log.error(
                    "Ошибка gRPC при отправке обновления боту (код: {}): {}",
                    e.getStatus().getCode(),
                    e.getMessage());
        } catch (Exception e) {
            log.error("Непредвиденная ошибка при отправке уведомления", e);
        }
    }
}
