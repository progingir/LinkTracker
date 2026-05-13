package backend.academy.linktracker.bot;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

public class ScrapperToBotIntegrationTest extends BotIntegrationTestBase {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Проверка всей цепочки: Kafka -> Обработка -> HTTP-запрос в Telegram")
    void shouldProcessMessageFromKafkaAndSendToTelegram() throws Exception {
        stubFor(post(urlPathMatching("/bot[^/]+/sendMessage"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"ok\": true}")));

        Long chatId = 12345L;
        LinkUpdate update = new LinkUpdate(
                100L, URI.create("https://github.com/user/repo"), "Обновление обнаружено", List.of(chatId), false);

        String updateJson = objectMapper.writeValueAsString(update);

        kafkaTemplate.send("link_updates", update.id().toString(), updateJson);

        Thread.sleep(3000);

        verify(
                1,
                postRequestedFor(urlPathMatching("/bot[^/]+/sendMessage"))
                        .withRequestBody(containing("chat_id=" + chatId))
                        .withRequestBody(containing("github.com")));
    }
}
