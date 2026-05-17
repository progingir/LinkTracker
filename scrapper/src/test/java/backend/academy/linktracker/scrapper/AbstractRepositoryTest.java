package backend.academy.linktracker.scrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.domain.Subscription;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import backend.academy.linktracker.scrapper.repository.TagRepository;
import backend.academy.linktracker.scrapper.repository.TgChatRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@Transactional
@Rollback
public abstract class AbstractRepositoryTest extends ScrapperIntegrationTestBase {

    @Autowired
    protected TgChatRepository tgChatRepository;

    @Autowired
    protected LinkRepository linkRepository;

    @Autowired
    protected SubscriptionRepository subscriptionRepository;

    @Autowired
    protected TagRepository tagRepository;

    @PersistenceContext
    protected EntityManager entityManager;

    protected void flushAndClear() {
        if (entityManager != null) {
            entityManager.flush();
            entityManager.clear();
        }
    }

    @Test
    void shouldManageChatLifecycle() {
        long chatId = 777L;
        tgChatRepository.addChat(chatId);
        flushAndClear();

        assertThat(tgChatRepository.existsChat(chatId)).isTrue();
        assertThat(tgChatRepository.findAll(10, 0)).contains(chatId);

        tgChatRepository.removeChat(chatId);
        flushAndClear();
        assertThat(tgChatRepository.existsChat(chatId)).isFalse();
    }

    @Test
    void shouldManageLinkAndItsUpdateTimes() {
        URI url = URI.create("https://github.com/test/repo");
        Link link = linkRepository.save(url);
        flushAndClear();

        OffsetDateTime updateTime = OffsetDateTime.now().minusDays(1).truncatedTo(ChronoUnit.SECONDS);

        linkRepository.updateLastUpdateTime(link.id(), updateTime);
        flushAndClear();

        Link updated = linkRepository.findById(link.id()).orElseThrow();

        assertThat(updated.lastUpdate().toInstant()).isCloseTo(updateTime.toInstant(), within(1, ChronoUnit.SECONDS));
    }

    @Test
    void shouldFindOldestLinksCorrectly() {
        Link oldLink = linkRepository.save(URI.create("https://old.com"));
        Link newLink = linkRepository.save(URI.create("https://new.com"));
        flushAndClear();

        OffsetDateTime past = OffsetDateTime.now().minusDays(10).truncatedTo(ChronoUnit.SECONDS);
        linkRepository.updateLastUpdateTime(oldLink.id(), past);

        linkRepository.updateLastUpdateTime(newLink.id(), OffsetDateTime.now());
        flushAndClear();

        List<Link> oldest = linkRepository.findOldest(1);
        assertThat(oldest).hasSize(1);
        assertThat(oldest.get(0).url()).isEqualTo(oldLink.url());
    }

    @Test
    void shouldManageSubscriptionsAndTags() {
        long chatId = 10L;
        tgChatRepository.addChat(chatId);
        Link link = linkRepository.save(URI.create("https://spring.io"));
        flushAndClear();

        subscriptionRepository.addSubscription(chatId, link.id());
        subscriptionRepository.addTagToSubscription(chatId, link.id(), "java");

        flushAndClear();

        List<Subscription> subs = subscriptionRepository.findByChatId(chatId, 10, null);
        assertThat(subs).hasSize(1);
        assertThat(subs.get(0).tags())
                .as("Tags should contain 'java' after adding")
                .contains("java");

        subscriptionRepository.removeSubscription(chatId, link.id());
        flushAndClear();
        assertThat(subscriptionRepository.exists(chatId, link.id())).isFalse();
    }
}
