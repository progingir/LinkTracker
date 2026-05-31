package backend.academy.linktracker.scrapper;

import backend.academy.linktracker.scrapper.client.GitHubClient;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final GitHubClient gitHubClient;

    @GetMapping("/test-rate-limit")
    @RateLimiter(name = "ip-rate-limit")
    public String testRateLimit() {
        return "OK";
    }
}
