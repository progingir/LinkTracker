package backend.academy.linktracker.bot;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CancelCommandIntegrationTest extends BotIntegrationTestBase {

    @Test
    @DisplayName("Отмена активного диалога командой /cancel")
    void shouldCancelActiveState() {
        long chatId = 104L;

        botService.process(List.of(createUpdate(chatId, "/track")));

        botService.process(List.of(createUpdate(chatId, "/cancel")));

        verify(postRequestedFor(urlMatching("/bot[^/]+/sendMessage"))
                .withRequestBody(containing(encoded("🔄 Операция отменена. Я готов к новым командам."))));
    }
}
