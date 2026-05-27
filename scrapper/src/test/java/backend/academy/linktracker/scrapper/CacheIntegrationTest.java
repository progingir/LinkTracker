package backend.academy.linktracker.scrapper;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.TgChatRepository;
import backend.academy.linktracker.scrapper.service.LinkService;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class CacheIntegrationTest extends ScrapperIntegrationTestBase {

    @Autowired
    private LinkService linkService;

    @Autowired
    private TgChatRepository tgChatRepository;

    @Autowired
    private LinkRepository linkRepository;

    @Test
    public void testCacheGetLinks() {
        Long chatId = 1L;
        tgChatRepository.addChat(chatId);

        URI uri = URI.create("https://github.com/test/repo");
        linkService.addLink(chatId, uri, List.of("test"));

        ListLinksResponse firstResponse = linkService.getLinksResponse(chatId, 10, null);

        ListLinksResponse secondResponse = linkService.getLinksResponse(chatId, 10, null);

        assertThat(firstResponse).isEqualTo(secondResponse);
    }

    @Test
    public void testCacheEvictionOnAdd() {
        Long chatId = 2L;
        tgChatRepository.addChat(chatId);

        linkService.getLinksResponse(chatId, 10, null);

        URI uri = URI.create("https://github.com/test/repo2");
        linkService.addLink(chatId, uri, List.of("test"));

        ListLinksResponse secondResponse = linkService.getLinksResponse(chatId, 10, null);
        assertThat(secondResponse.size()).isEqualTo(1);
    }

    @Test
    public void testCacheEvictionOnRemove() {
        Long chatId = 3L;
        tgChatRepository.addChat(chatId);

        URI uri = URI.create("https://github.com/test/repo3");
        linkService.addLink(chatId, uri, List.of("test"));

        linkService.getLinksResponse(chatId, 10, null);

        linkService.removeLink(chatId, uri);

        ListLinksResponse response = linkService.getLinksResponse(chatId, 10, null);
        assertThat(response.size()).isEqualTo(0);
    }
}
