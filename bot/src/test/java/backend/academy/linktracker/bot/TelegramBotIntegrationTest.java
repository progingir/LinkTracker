package backend.academy.linktracker.bot;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import backend.academy.linktracker.bot.properties.TelegramProperties;
import backend.academy.linktracker.bot.service.BotService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.GetUpdates;
import java.time.Duration;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@EnableWireMock
class TelegramBotIntegrationTest implements WithAssertions {

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private TelegramProperties telegramProperties;

    @Autowired
    private BotService botService;

    @AfterEach
    void clearUpdatesListener() {
        telegramBot.removeGetUpdatesListener();
    }

    @Test
    @DisplayName("Полный сценарий: получение команды /start и отправка ответа ботом")
    void fullCommandExecutionFlow() {
        stubFor(post(urlMatching("/bot[^/]+/getUpdates"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                  "ok": true,
                                  "result": [
                                    {
                                      "update_id": 111,
                                      "message": {
                                        "message_id": 1,
                                        "from": { "id": 12345, "first_name": "Valery" },
                                        "chat": { "id": 12345, "type": "private" },
                                        "text": "/start"
                                      }
                                    }
                                  ]
                                }
                                """)));

        stubFor(post(urlMatching("/bot[^/]+/sendMessage"))
                .willReturn(aResponse().withStatus(200).withBody("{\"ok\": true}")));

        telegramBot.setUpdatesListener(botService);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(postRequestedFor(urlMatching("/bot[^/]+/sendMessage"))
                    .withRequestBody(containing("chat_id=12345"))
                    .withRequestBody(containing("LinkTracker")));
        });
    }

    @Test
    void nonExistingTokenRequest() {
        stubFor(post(urlMatching("/bot[^/]+/getUpdates"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("{\"ok\":false,\"error_code\":404,\"description\":\"Not Found\"}")));

        var getUpdatesRequest = new GetUpdates();
        var getUpdatesResponse = telegramBot.execute(getUpdatesRequest);

        assertFalse(getUpdatesResponse.isOk());
        assertEquals(404, getUpdatesResponse.errorCode());
    }
}
