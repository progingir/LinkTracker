package backend.academy.linktracker.bot;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.catchThrowable;

import backend.academy.linktracker.bot.client.HttpScrapperClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "app.scrapper-client-type=http",
            "app.telegram.url=http://localhost:8080",
            "app.telegram.token=test-token"
        })
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public class ResilienceIntegrationTest {

    @Autowired
    private HttpScrapperClient scrapperClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @RegisterExtension
    static WireMockExtension wireMockScrapper = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("app.scrapper-client.base-url", wireMockScrapper::baseUrl);
        registry.add("app.scrapper-client.timeout", () -> "1s");

        registry.add("resilience4j.circuitbreaker.configs.default.slidingWindowSize", () -> "5");
        registry.add("resilience4j.circuitbreaker.configs.default.minimumRequiredCalls", () -> "3");
        registry.add("resilience4j.circuitbreaker.configs.default.waitDurationInOpenState", () -> "2s");
        registry.add("resilience4j.circuitbreaker.configs.default.permittedCallsInHalfOpenState", () -> "2");

        registry.add("resilience4j.retry.configs.default.maxAttempts", () -> "3");
        registry.add("app.retry.initial-interval", () -> "500ms");
        registry.add("app.retry.multiplier", () -> "1.0");
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @BeforeEach
    void setUp() {
        wireMockScrapper.resetAll();
        circuitBreakerRegistry.circuitBreaker("scrapper").transitionToClosedState();
    }

    @Test
    void testTimeout_TC1_1() {
        wireMockScrapper.stubFor(get(urlPathEqualTo("/links"))
                .willReturn(aResponse().withStatus(200).withFixedDelay(2000)));

        assertThatExceptionOfType(Exception.class).isThrownBy(() -> scrapperClient.getLinks(123L));
    }

    @Test
    void testRetryOn5xx_TC2_1() {
        String scenarioName = "Retry Scenario";
        wireMockScrapper.stubFor(get(urlPathEqualTo("/links"))
                .inScenario(scenarioName)
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("Attempt 2"));

        wireMockScrapper.stubFor(get(urlPathEqualTo("/links"))
                .inScenario(scenarioName)
                .whenScenarioStateIs("Attempt 2")
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("Attempt 3"));

        wireMockScrapper.stubFor(get(urlPathEqualTo("/links"))
                .inScenario(scenarioName)
                .whenScenarioStateIs("Attempt 3")
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"links\":[], \"size\":0}")
                        .withStatus(200)));

        scrapperClient.getLinks(123L);

        wireMockScrapper.verify(3, WireMock.getRequestedFor(urlPathEqualTo("/links")));
    }

    @Test
    void testCircuitBreaker_TC4_1() {
        wireMockScrapper.stubFor(
                get(urlPathEqualTo("/links")).willReturn(aResponse().withStatus(500)));

        for (int i = 0; i < 5; i++) {
            catchThrowable(() -> scrapperClient.getLinks(123L));
        }

        Throwable thrown = catchThrowable(() -> scrapperClient.getLinks(123L));
        assertThat(thrown).isInstanceOf(CallNotPermittedException.class);
    }
}
