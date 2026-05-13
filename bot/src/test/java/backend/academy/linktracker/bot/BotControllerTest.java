package backend.academy.linktracker.bot;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.bot.controller.BotController;
import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.bot.service.BotService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BotController.class)
@ActiveProfiles("test")
class BotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private BotService botService;

    @Test
    @DisplayName("Тест 1: Корректный запрос /updates (обычное обновление)")
    void updatesCorrectRequest() throws Exception {
        LinkUpdate update = new LinkUpdate(
                1L, URI.create("http://github.com/user/repo"), "Update detected", List.of(12345L), false);

        doNothing().when(botService).sendNotification(any(LinkUpdate.class));

        mockMvc.perform(post("/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Тест 2: Корректный запрос /updates (системный отчет)")
    void systemReportRequest() throws Exception {
        LinkUpdate update = LinkUpdate.systemReport("System report text", List.of(12345L));

        doNothing().when(botService).sendNotification(any(LinkUpdate.class));

        mockMvc.perform(post("/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Тест 3: Некорректный запрос /updates (400 Bad Request)")
    void updatesIncorrectRequest() throws Exception {
        String invalidBody = "{\"id\": 1}";

        mockMvc.perform(post("/updates").contentType(MediaType.APPLICATION_JSON).content(invalidBody))
                .andExpect(status().isBadRequest());
    }
}
