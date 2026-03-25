package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.domain.Subscription;
import java.util.List;

public interface SubscriptionRepository {
    void addSubscription(Long chatId, Long linkId);

    void removeSubscription(Long chatId, Long linkId);

    boolean exists(Long chatId, Long linkId);

    List<Long> findChatIdsByLinkId(Long linkId);

    List<Subscription> findAllByChatId(Long chatId, int limit, int offset);

    void removeAllByChatId(Long chatId);

    void addTagToSubscription(Long chatId, Long linkId, String tag);

    void removeTagFromSubscription(Long chatId, Long linkId, String tag);
}
