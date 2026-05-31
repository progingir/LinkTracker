package backend.academy.linktracker.bot.configuration;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimiterRegistry rateLimiterRegistry;

    public RateLimitingFilter(@Autowired(required = false) RateLimiterRegistry rateLimiterRegistry) {
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (rateLimiterRegistry == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = request.getRemoteAddr();
        if (request.getHeader("X-Forwarded-For") != null) {
            ip = request.getHeader("X-Forwarded-For").split(",")[0].trim();
        }

        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter("ip-" + ip, "ip-rate-limit");

        try {
            RateLimiter.waitForPermission(rateLimiter);
            filterChain.doFilter(request, response);
        } catch (RequestNotPermitted e) {
            response.setStatus(429);
            response.getWriter().write("Too Many Requests");
        }
    }
}
