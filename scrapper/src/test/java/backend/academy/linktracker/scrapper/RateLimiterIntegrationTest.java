package backend.academy.linktracker.scrapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
public class RateLimiterIntegrationTest extends ScrapperIntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testRateLimiterLimitsRequests() throws Exception {
        int limit = 50;
        for (int i = 0; i < limit; i++) {
            mockMvc.perform(get("/links").header("Tg-Chat-Id", 12345).header("X-Forwarded-For", "192.168.1.100"))
                    .andReturn();
        }
        mockMvc.perform(get("/links").header("Tg-Chat-Id", 12345).header("X-Forwarded-For", "192.168.1.100"))
                .andExpect(status().isTooManyRequests());
    }
}
