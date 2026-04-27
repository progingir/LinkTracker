package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.grpc.BotServiceGrpc;
import backend.academy.linktracker.grpc.LinkUpdateMsg;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.properties.BotClientProperties;
import backend.academy.linktracker.scrapper.service.update.UpdateSender;
import io.grpc.StatusRuntimeException;
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

    @Override
    public void sendUpdate(LinkUpdate update) {
        try {
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

        } catch (StatusRuntimeException e) {
            log.atError()
                    .setCause(e)
                    .addKeyValue("grpc_code", e.getStatus().getCode())
                    .addKeyValue("link_id", update.id())
                    .log("Ошибка gRPC при отправке обновления боту");

        } catch (Exception e) {
            log.atError()
                    .setCause(e)
                    .addKeyValue("link_id", update.id())
                    .log("Непредвиденная ошибка при отправке уведомления");
        }
    }
}
