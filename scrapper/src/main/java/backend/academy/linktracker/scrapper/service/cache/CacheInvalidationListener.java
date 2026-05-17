package backend.academy.linktracker.scrapper.service.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheInvalidationListener {

    private final LinkCacheService cacheService;

    public void handleInvalidation(String message) {
        try {
            Long chatId = Long.parseLong(message);
            cacheService.invalidateLocal(chatId);
            log.atDebug().addKeyValue("chat_id", chatId).log("L1 Cache invalidated via Pub/Sub");
        } catch (NumberFormatException e) {
            log.warn("Received invalid cache invalidation message: {}", message);
        }
    }
}
