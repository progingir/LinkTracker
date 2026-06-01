package backend.academy.linktracker.scrapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.grpc.BotServiceGrpc;
import backend.academy.linktracker.scrapper.client.BotNotificationClient;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.service.update.KafkaMessageProducer;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@TestPropertySource(properties = "app.use-queue=false")
public class FallbackIntegrationTest extends ScrapperIntegrationTestBase {

    @Autowired
    private BotNotificationClient botNotificationClient;

    @MockitoBean
    private BotServiceGrpc.BotServiceBlockingStub botServiceBlockingStub;

    @MockitoBean
    private KafkaMessageProducer kafkaMessageProducer;

    @Test
    public void testFallbackToKafka() {
        when(botServiceBlockingStub.withDeadlineAfter(anyLong(), any())).thenReturn(botServiceBlockingStub);
        doThrow(new StatusRuntimeException(Status.UNAVAILABLE))
                .when(botServiceBlockingStub)
                .sendUpdate(any());

        LinkUpdate update = new LinkUpdate(1L, URI.create("http://example.com"), "description", List.of(123L), false);
        botNotificationClient.sendUpdate(update);

        verify(kafkaMessageProducer).send(update);
    }
}
