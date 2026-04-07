package backend.academy.linktracker.scrapper;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.domain.Subscription;
import backend.academy.linktracker.scrapper.domain.Tag;
import backend.academy.linktracker.scrapper.repository.jpa.JpaLinkRepository;
import backend.academy.linktracker.scrapper.repository.jpa.JpaSubscriptionRepository;
import backend.academy.linktracker.scrapper.repository.jpa.JpaTagRepository;
import backend.academy.linktracker.scrapper.repository.jpa.JpaTgChatRepository;
import jakarta.persistence.EntityManager;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "app.database.access-type=jpa")
class JpaRepositoryTest extends ScrapperIntegrationTestBase {

    @Autowired
    private JpaTgChatRepository tgChatRepository;

    @Autowired
    private JpaLinkRepository linkRepository;

    @Autowired
    private JpaSubscriptionRepository subscriptionRepository;

    @Autowired
    private JpaTagRepository tagRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    @Rollback
    void shouldCorrectlyPerformBasicCrudOperationsInJpaMode() {
        long chatId = 888L;
        URI url = URI.create("https://jpa-test.com");

        tgChatRepository.addChat(chatId);
        Link link = linkRepository.save(url);

        assertThat(tgChatRepository.existsChat(chatId)).isTrue();
        assertThat(linkRepository.findByUrl(url)).isPresent();
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindOnlySpecifiedNumberOfOldestLinksBasedOnLimit() {
        linkRepository.save(URI.create("https://old1.com"));
        linkRepository.save(URI.create("https://old2.com"));

        List<Link> oldest = linkRepository.findOldest(1);
        assertThat(oldest).hasSize(1);
    }

    @Test
    @Transactional
    @Rollback
    void shouldManageSubscriptionsAndTagsCorrectly() {
        long chatId = 100L;
        URI url = URI.create("https://github.com/test");
        tgChatRepository.addChat(chatId);
        Link link = linkRepository.save(url);

        subscriptionRepository.addSubscription(chatId, link.id());

        subscriptionRepository.addTagToSubscription(chatId, link.id(), "test-tag");

        entityManager.flush();
        entityManager.clear();

        List<Subscription> subs = subscriptionRepository.findByChatId(chatId, 10, null);
        assertThat(subs).hasSize(1);
        assertThat(subs.get(0).tags()).contains("test-tag");

        List<Long> chatIds = subscriptionRepository.findChatIdsByLinkId(link.id());
        assertThat(chatIds).contains(chatId);

        subscriptionRepository.removeSubscription(chatId, link.id());

        entityManager.flush();
        entityManager.clear();

        assertThat(subscriptionRepository.exists(chatId, link.id())).isFalse();
    }

    @Test
    @Transactional
    @Rollback
    void shouldPerformTagOperationsIndependently() {
        String tagName = "jpa-exclusive";

        Tag savedTag = tagRepository.save(tagName);
        assertThat(savedTag.name()).isEqualTo(tagName);

        tagRepository.update(savedTag.id(), "new-tag-name");

        entityManager.flush();
        entityManager.clear();

        Tag updated = tagRepository.findById(savedTag.id()).get();
        assertThat(updated.name()).isEqualTo("new-tag-name");

        tagRepository.delete(savedTag.id());

        entityManager.flush();
        entityManager.clear();

        assertThat(tagRepository.findById(savedTag.id())).isEmpty();
    }
}
