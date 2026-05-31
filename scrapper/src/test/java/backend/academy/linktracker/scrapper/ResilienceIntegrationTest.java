package backend.academy.linktracker.scrapper;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.awaitility.Awaitility.await;

import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.dto.GitHubResponse;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ResilienceIntegrationTest extends ScrapperIntegrationTestBase {

    @Autowired
    private GitHubClient gitHubClient;

    private RestClient restTemplate = RestClient.create();

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @LocalServerPort
    private int port;

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("app.github.url", wireMockServer::baseUrl);
        registry.add("app.github.timeout", () -> "1s");
        registry.add("app.stackoverflow.url", wireMockServer::baseUrl);
        registry.add("app.stackoverflow.timeout", () -> "1s");

        registry.add("resilience4j.circuitbreaker.configs.default.slidingWindowSize", () -> "5");
        registry.add("resilience4j.circuitbreaker.configs.default.minimumRequiredCalls", () -> "3");
        registry.add("resilience4j.circuitbreaker.configs.default.waitDurationInOpenState", () -> "2s");
        registry.add("resilience4j.circuitbreaker.configs.default.permittedCallsInHalfOpenState", () -> "2");

        registry.add("resilience4j.retry.configs.default.maxAttempts", () -> "3");
        registry.add("resilience4j.retry.configs.default.waitDuration", () -> "500ms");

        registry.add("resilience4j.ratelimiter.configs.ip-rate-limit.limitForPeriod", () -> "2");
        registry.add("resilience4j.ratelimiter.configs.ip-rate-limit.limitRefreshPeriod", () -> "5s");
        registry.add("resilience4j.ratelimiter.configs.ip-rate-limit.timeoutDuration", () -> "0s");
    }

    @BeforeEach
    void setUp() {
        wireMockServer.resetAll();
        circuitBreakerRegistry.circuitBreaker("github").transitionToClosedState();
        circuitBreakerRegistry.circuitBreaker("stackoverflow").transitionToClosedState();
    }

    @Test
    void testTimeout_TC1_1() {
        wireMockServer.stubFor(get(urlPathEqualTo("/repos/owner/repo"))
                .willReturn(aResponse().withStatus(200).withFixedDelay(2000)));

        assertThatExceptionOfType(RestClientException.class)
                .isThrownBy(() -> gitHubClient.fetchRepository("owner", "repo"));
    }

    @Test
    void testRetryOn5xx_TC2_1() {
        String scenarioName = "Retry Scenario";
        wireMockServer.stubFor(get(urlPathEqualTo("/repos/owner/retry"))
                .inScenario(scenarioName)
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("Attempt 2"));

        wireMockServer.stubFor(get(urlPathEqualTo("/repos/owner/retry"))
                .inScenario(scenarioName)
                .whenScenarioStateIs("Attempt 2")
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("Attempt 3"));

        wireMockServer.stubFor(get(urlPathEqualTo("/repos/owner/retry"))
                .inScenario(scenarioName)
                .whenScenarioStateIs("Attempt 3")
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "id": 1,
                                  "name": "retry",
                                  "html_url": "http://example.com",
                                  "pushed_at": "2023-01-01T00:00:00Z"
                                }
                                """)
                        .withStatus(200)));

        long startTime = System.currentTimeMillis();
        Optional<GitHubResponse> response = gitHubClient.fetchRepository("owner", "retry");
        long elapsedTime = System.currentTimeMillis() - startTime;

        assertThat(response).isPresent();
        assertThat(response.get().name()).isEqualTo("retry");
        wireMockServer.verify(3, WireMock.getRequestedFor(urlPathEqualTo("/repos/owner/retry")));

        assertThat(elapsedTime).isGreaterThanOrEqualTo(1000L);
    }

    @Test
    void testExponentialBackoff_TC2_4() {
        String scenarioName = "Exponential Backoff Scenario";
        wireMockServer.stubFor(get(urlPathEqualTo("/repos/owner/exp"))
                .inScenario(scenarioName)
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("Attempt 2"));

        wireMockServer.stubFor(get(urlPathEqualTo("/repos/owner/exp"))
                .inScenario(scenarioName)
                .whenScenarioStateIs("Attempt 2")
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("Attempt 3"));

        wireMockServer.stubFor(get(urlPathEqualTo("/repos/owner/exp"))
                .inScenario(scenarioName)
                .whenScenarioStateIs("Attempt 3")
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "id": 1,
                                  "name": "exp",
                                  "html_url": "http://example.com",
                                  "pushed_at": "2023-01-01T00:00:00Z"
                                }
                                """)
                        .withStatus(200)));

        long startTime = System.currentTimeMillis();
        Optional<GitHubResponse> response = gitHubClient.fetchRepository("owner", "exp");
        long elapsedTime = System.currentTimeMillis() - startTime;

        assertThat(response).isPresent();
        wireMockServer.verify(3, WireMock.getRequestedFor(urlPathEqualTo("/repos/owner/exp")));

        assertThat(elapsedTime).isGreaterThanOrEqualTo(1300L);
    }

    @Test
    void testNoRetryOn4xx_TC2_2() {
        wireMockServer.stubFor(get(urlPathEqualTo("/repos/owner/noretry"))
                .willReturn(aResponse().withStatus(400)));

        Throwable thrown = catchThrowable(() -> gitHubClient.fetchRepository("owner", "noretry"));

        assertThat(thrown).isInstanceOf(RestClientException.class);
        wireMockServer.verify(1, WireMock.getRequestedFor(urlPathEqualTo("/repos/owner/noretry")));
    }

    @Test
    void testCircuitBreaker_TC4_1_to_4_3() throws InterruptedException {
        wireMockServer.stubFor(
                get(urlPathEqualTo("/repos/owner/cb")).willReturn(aResponse().withStatus(500)));

        for (int i = 0; i < 5; i++) {
            catchThrowable(() -> gitHubClient.fetchRepository("owner", "cb"));
        }

        Throwable thrown = catchThrowable(() -> gitHubClient.fetchRepository("owner", "cb"));
        assertThat(thrown).isInstanceOf(CallNotPermittedException.class);

        Thread.sleep(2500);

        wireMockServer.stubFor(
                get(urlPathEqualTo("/repos/owner/cb")).willReturn(aResponse().withStatus(500)));
        catchThrowable(() -> gitHubClient.fetchRepository("owner", "cb"));

        wireMockServer.stubFor(get(urlPathEqualTo("/repos/owner/cb"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "id": 1,
                                  "name": "cb",
                                  "html_url": "http://example.com",
                                  "pushed_at": "2023-01-01T00:00:00Z"
                                }
                                """)
                        .withStatus(200)));

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            Optional<GitHubResponse> response = gitHubClient.fetchRepository("owner", "cb");
            assertThat(response).isPresent();
        });
    }

    @Test
    void testRateLimiter_TC3_1() {
        String url = "http://localhost:" + port + "/test-rate-limit";

        ResponseEntity<String> response1 =
                restTemplate.get().uri(url).retrieve().toEntity(String.class);
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> response2 =
                restTemplate.get().uri(url).retrieve().toEntity(String.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);

        HttpClientErrorException thrown = (HttpClientErrorException)
                catchThrowable(() -> restTemplate.get().uri(url).retrieve().toEntity(String.class));
        assertThat(thrown.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }
}
