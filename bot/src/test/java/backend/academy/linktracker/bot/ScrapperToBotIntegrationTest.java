package backend.academy.linktracker.bot;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.kafka.admin.auto-create=false",
            "logging.level.org.apache.kafka=ERROR"
        })
@Testcontainers
public class ScrapperToBotIntegrationTest {

    @Container
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.2"));

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("app.kafka.topic", () -> "link_updates");
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoBean
    private TelegramBot telegramBot;

    @BeforeEach
    void setUp() {
        BaseResponse successResponse = Mockito.mock(BaseResponse.class);
        when(successResponse.isOk()).thenReturn(true);
        when(telegramBot.execute(any())).thenReturn(successResponse);
    }

    @Test
    void shouldProcessMessageFromKafkaAndSendToTelegram() {
        LinkUpdate update = new LinkUpdate(
                100L, URI.create("https://github.com/user/repo"), "Update detected", List.of(12345L), false);

        kafkaTemplate.send("link_updates", update.id().toString(), update);

        Mockito.verify(telegramBot, timeout(15000).atLeastOnce()).execute(any(SendMessage.class));
    }
}
