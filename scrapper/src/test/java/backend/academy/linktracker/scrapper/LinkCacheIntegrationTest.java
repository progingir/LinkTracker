package backend.academy.linktracker.scrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.scrapper.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.repository.TgChatRepository;
import backend.academy.linktracker.scrapper.service.LinkService;
import backend.academy.linktracker.scrapper.service.cache.LinkCacheService;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

public class LinkCacheIntegrationTest extends ScrapperIntegrationTestBase {

    @Autowired
    private LinkService linkService;

    @Autowired
    private TgChatRepository tgChatRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @MockitoSpyBean
    private LinkCacheService cacheService;

    @Test
    void shouldCacheLinksAndInvalidateOnAddRemove() {
        Long chatId = 123456L;
        tgChatRepository.addChat(chatId);
        URI uri = URI.create("https://github.com/test/test");

        ListLinksResponse response1 = linkService.getLinksResponse(chatId, 10, null);
        assertThat(response1.links()).isEmpty();
        verify(cacheService, atLeastOnce()).putLinks(eq(chatId), any());

        ListLinksResponse response2 = linkService.getLinksResponse(chatId, 10, null);
        assertThat(response2).isEqualTo(response1);
        verify(cacheService, atLeastOnce()).getLinks(chatId);

        linkService.addLink(chatId, uri, List.of("tag1"));
        verify(cacheService, atLeastOnce()).evictLinks(chatId);

        String cacheKey = "links:" + chatId;
        assertThat(redisTemplate.hasKey(cacheKey)).isFalse();

        ListLinksResponse response3 = linkService.getLinksResponse(chatId, 10, null);
        assertThat(response3.links()).hasSize(1);
        assertThat(response3.links().get(0).url()).isEqualTo(uri);

        linkService.removeLink(chatId, uri);
        verify(cacheService, atLeastOnce()).evictLinks(chatId);
        assertThat(redisTemplate.hasKey(cacheKey)).isFalse();
    }
}
