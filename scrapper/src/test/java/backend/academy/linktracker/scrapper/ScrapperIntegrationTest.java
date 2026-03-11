package backend.academy.linktracker.scrapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ScrapperIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    @DisplayName("3.1-3.2: Полный цикл - регистрация, добавление, получение и удаление")
    void fullLinkCycle() throws Exception {
        Long chatId = 1L;
        String link = "https://github.com/user/repo";

        mockMvc.perform(post("/tg-chat/{id}", chatId)).andExpect(status().isOk());

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("link", link))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(link));

        mockMvc.perform(get("/links").header("Tg-Chat-Id", chatId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.links[0].url").value(link));

        mockMvc.perform(delete("/links")
                        .header("Tg-Chat-Id", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("link", link))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/links").header("Tg-Chat-Id", chatId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(0));
    }

    @Test
    @DisplayName("3.3: Попытка удаления ссылки из несуществующего чата")
    void removeLinkFromNonExistentChat() throws Exception {
        Long validChatId = 10L;
        Long nonExistentChatId = 999L;
        String link = "https://github.com/user/repo";

        mockMvc.perform(post("/tg-chat/{id}", validChatId)).andExpect(status().isOk());
        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", validChatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("link", link))))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/links")
                        .header("Tg-Chat-Id", nonExistentChatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("link", link))))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/links").header("Tg-Chat-Id", validChatId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(1));
    }

    @Test
    @DisplayName("3.4: Добавление ссылки в несуществующий чат (404)")
    void addLinkToNonExistentChat() throws Exception {
        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("link", "https://google.com"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("3.5: Работа с удалённым чатом")
    void workWithDeletedChat() throws Exception {
        Long chatId = 20L;

        mockMvc.perform(post("/tg-chat/{id}", chatId)).andExpect(status().isOk());

        mockMvc.perform(delete("/tg-chat/{id}", chatId)).andExpect(status().isOk());

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("link", "https://stackoverflow.com/q/1"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("3.6: Удаление несуществующего чата (404)")
    void deleteNonExistentChat() throws Exception {
        mockMvc.perform(delete("/tg-chat/{id}", 888L)).andExpect(status().isNotFound());
    }
}
