package backend.academy.linktracker.scrapper.service.cache;

import backend.academy.linktracker.scrapper.configuration.RedisConfig;
import backend.academy.linktracker.scrapper.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.properties.ValkeyProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LinkCacheService {

    private final RedisTemplate<String, ListLinksResponse> redisTemplate;
    private final ValkeyProperties properties;
    private final Cache<Long, ListLinksResponse> localCache;

    private static final String KEY_PREFIX = "links:";

    public LinkCacheService(RedisTemplate<String, ListLinksResponse> redisTemplate, ValkeyProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.localCache = Caffeine.newBuilder()
                .expireAfterWrite(properties.ttl())
                .maximumSize(properties.maxSize())
                .build();
    }

    public Optional<ListLinksResponse> getLinks(Long tgChatId) {
        ListLinksResponse response = localCache.get(tgChatId, id -> {
            String key = KEY_PREFIX + id;
            try {
                ListLinksResponse l2Response = redisTemplate.opsForValue().get(key);
                if (l2Response != null) {
                    log.atDebug().addKeyValue("chat_id", id).log("L2 Cache Hit, updating L1");
                    return l2Response;
                }
            } catch (Exception e) {
                log.error("Valkey is unavailable or error occurred while fetching links for chat {}", id, e);
            }
            return null;
        });

        return Optional.ofNullable(response);
    }

    public void putLinks(Long tgChatId, ListLinksResponse response) {
        String key = KEY_PREFIX + tgChatId;
        try {
            redisTemplate.opsForValue().set(key, response, properties.ttl());
        } catch (Exception e) {
            log.error("Failed to update Valkey for chat {}", tgChatId, e);
        }
        localCache.put(tgChatId, response);
    }

    public void evictLinks(Long tgChatId) {
        String key = KEY_PREFIX + tgChatId;
        try {
            redisTemplate.delete(key);
            redisTemplate.convertAndSend(RedisConfig.CACHE_INVALIDATION_TOPIC, tgChatId.toString());
        } catch (Exception e) {
            log.error("Failed to evict from Valkey for chat {}", tgChatId, e);
        }
        invalidateLocal(tgChatId);
    }

    public void invalidateLocal(Long tgChatId) {
        localCache.invalidate(tgChatId);
    }
}
