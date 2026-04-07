package backend.academy.linktracker.scrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.repository.jdbc.JdbcLinkRepository;
import backend.academy.linktracker.scrapper.repository.jdbc.JdbcSubscriptionRepository;
import backend.academy.linktracker.scrapper.repository.jdbc.JdbcTgChatRepository;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "app.database.access-type=jdbc")
class JdbcRepositoryTest extends ScrapperIntegrationTestBase {

    @Autowired
    private JdbcTgChatRepository tgChatRepository;

    @Autowired
    private JdbcLinkRepository linkRepository;

    @Autowired
    private JdbcSubscriptionRepository subscriptionRepository;

    @Test
    @Transactional
    @Rollback
    void shouldManageChatLifecycle() {
        long chatId = 777L;

        tgChatRepository.addChat(chatId);
        assertThat(tgChatRepository.existsChat(chatId)).isTrue();

        List<Long> allChats = tgChatRepository.findAll(10, 0);
        assertThat(allChats).contains(chatId);

        tgChatRepository.removeChat(chatId);
        assertThat(tgChatRepository.existsChat(chatId)).isFalse();
    }

    @Test
    @Transactional
    @Rollback
    void shouldManageLinkAndItsUpdateTimes() {
        URI url = URI.create("https://github.com/test/repo");
        Link link = linkRepository.save(url);
        OffsetDateTime updateTime = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        linkRepository.updateLastUpdateTime(link.id(), updateTime);

        Link updated = linkRepository.findById(link.id()).orElseThrow();
        assertThat(updated.lastUpdate().toInstant()).isEqualTo(updateTime.toInstant());

        linkRepository.remove(link.id());
        assertThat(linkRepository.findByUrl(url)).isEmpty();
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindOldestLinksForScheduler() {
        linkRepository.save(URI.create("https://old.com"));
        linkRepository.save(URI.create("https://new.com"));

        List<Link> oldest = linkRepository.findOldest(1);
        assertThat(oldest).hasSize(1);
    }

    @Test
    @Transactional
    @Rollback
    void shouldManageSubscriptionsAndTagsUsingCteAndExists() {
        long chatId = 10L;
        tgChatRepository.addChat(chatId);
        Link link = linkRepository.save(URI.create("https://spring.io"));

        assertThat(subscriptionRepository.exists(chatId, link.id())).isFalse();

        subscriptionRepository.addSubscription(chatId, link.id());
        assertThat(subscriptionRepository.exists(chatId, link.id())).isTrue();

        subscriptionRepository.addTagToSubscription(chatId, link.id(), "java");
        assertDoesNotThrow(() -> subscriptionRepository.addTagToSubscription(chatId, link.id(), "java"));

        assertThat(subscriptionRepository.findChatIdsByLinkId(link.id())).contains(chatId);

        subscriptionRepository.removeTagFromSubscription(chatId, link.id(), "java");
        subscriptionRepository.removeSubscription(chatId, link.id());
        assertThat(subscriptionRepository.exists(chatId, link.id())).isFalse();
    }

    @Test
    @Transactional
    @Rollback
    void shouldRemoveAllSubscriptionsWhenChatIsDeleted() {
        long chatId = 500L;
        tgChatRepository.addChat(chatId);
        Link l1 = linkRepository.save(URI.create("https://l1.com"));
        subscriptionRepository.addSubscription(chatId, l1.id());

        subscriptionRepository.removeAllByChatId(chatId);
        assertThat(subscriptionRepository.findByChatId(chatId, 10, null)).isEmpty();
    }
}
