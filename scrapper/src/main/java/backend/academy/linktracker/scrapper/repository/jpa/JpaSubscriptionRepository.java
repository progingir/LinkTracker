package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.domain.Subscription;
import backend.academy.linktracker.scrapper.entity.SubscriptionEntity;
import backend.academy.linktracker.scrapper.entity.SubscriptionId;
import backend.academy.linktracker.scrapper.entity.TagEntity;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class JpaSubscriptionRepository implements SubscriptionRepository {

    private final SpringDataJpaSubscriptionRepository subscriptionRepo;
    private final SpringDataJpaTgChatRepository chatRepo;
    private final SpringDataJpaLinkRepository linkRepo;
    private final SpringDataJpaTagRepository tagRepo;
    private final jakarta.persistence.EntityManager entityManager;

    @Override
    @Transactional
    public void addSubscription(Long chatId, Long linkId) {
        SubscriptionId id = new SubscriptionId(chatId, linkId);
        if (subscriptionRepo.existsById(id)) {
            return;
        }

        SubscriptionEntity sub = new SubscriptionEntity();
        sub.setId(id);
        sub.setChat(chatRepo.getReferenceById(chatId));
        sub.setLink(linkRepo.getReferenceById(linkId));

        subscriptionRepo.saveAndFlush(sub);
    }

    @Override
    @Transactional
    public void removeSubscription(Long chatId, Long linkId) {
        subscriptionRepo.deleteById(new SubscriptionId(chatId, linkId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subscription> findAllByChatId(Long chatId, int limit, int offset) {
        List<SubscriptionEntity> subscriptions = entityManager
                .createQuery(
                        "SELECT s FROM SubscriptionEntity s JOIN FETCH s.link WHERE s.id.chatId = :chatId ORDER BY s.link.id ASC",
                        SubscriptionEntity.class)
                .setParameter("chatId", chatId)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();

        if (subscriptions.isEmpty()) {
            return List.of();
        }

        List<SubscriptionId> subIds =
                subscriptions.stream().map(SubscriptionEntity::getId).toList();

        entityManager
                .createQuery(
                        "SELECT DISTINCT s FROM SubscriptionEntity s LEFT JOIN FETCH s.tags WHERE s.id IN :ids",
                        SubscriptionEntity.class)
                .setParameter("ids", subIds)
                .getResultList();

        return subscriptions.stream().map(this::mapToDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(Long chatId, Long linkId) {
        return subscriptionRepo.existsById(new SubscriptionId(chatId, linkId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findChatIdsByLinkId(Long linkId) {
        return subscriptionRepo.findChatIdsByLinkId(linkId);
    }

    @Override
    @Transactional
    public void removeAllByChatId(Long chatId) {
        subscriptionRepo.deleteByIdChatId(chatId);
    }

    @Override
    @Transactional
    public void addTagToSubscription(Long chatId, Long linkId, String tagName) {
        subscriptionRepo.findById(new SubscriptionId(chatId, linkId)).ifPresent(sub -> {
            boolean hasTag = sub.getTags().stream().anyMatch(t -> t.getName().equals(tagName));
            if (!hasTag) {
                tagRepo.upsert(tagName);

                TagEntity tag = tagRepo.findByName(tagName)
                        .orElseThrow(() -> new IllegalStateException("Тег должен существовать"));

                sub.getTags().add(tag);
                subscriptionRepo.save(sub);
            }
        });
    }

    @Override
    @Transactional
    public void removeTagFromSubscription(Long chatId, Long linkId, String tagName) {
        subscriptionRepo.findById(new SubscriptionId(chatId, linkId)).ifPresent(sub -> {
            sub.getTags().removeIf(t -> t.getName().equals(tagName));
            subscriptionRepo.save(sub);
        });
    }

    private Subscription mapToDomain(SubscriptionEntity entity) {
        return new Subscription(
                entity.getId().getChatId(),
                entity.getLink().getId(),
                URI.create(entity.getLink().getUrl()),
                entity.getTags().stream().map(TagEntity::getName).toList());
    }
}
