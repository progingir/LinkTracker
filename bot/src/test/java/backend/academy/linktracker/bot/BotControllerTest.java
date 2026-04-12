package backend.academy.linktracker.bot;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"app.telegram.url=http://localhost:8080/", "app.telegram.token=test_token"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Тест 1: Корректный запрос /updates (обычное обновление)")
    void updatesCorrectRequest() throws Exception {
        var body = Map.of(
                "id",
                1,
                "url",
                "http://github.com/user/repo",
                "description",
                "Update detected",
                "tgChatIds",
                List.of(12345L),
                "isSystemReport",
                false);

        mockMvc.perform(post("/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Тест 2: Корректный запрос /updates (системный отчет)")
    void systemReportRequest() throws Exception {
        var body = Map.of(
                "description",
                "System error report: resources unavailable",
                "tgChatIds",
                List.of(12345L),
                "isSystemReport",
                true);

        mockMvc.perform(post("/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Тест 3: Некорректный запрос /updates (400 Bad Request)")
    void updatesIncorrectRequest() throws Exception {
        var invalidBody = Map.of("id", 1);

        mockMvc.perform(post("/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBody)))
                .andExpect(status().isBadRequest());
    }
}
