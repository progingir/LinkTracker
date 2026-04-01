package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.entity.SubscriptionEntity;
import backend.academy.linktracker.scrapper.entity.SubscriptionId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataJpaSubscriptionRepository extends JpaRepository<SubscriptionEntity, SubscriptionId> {

    @Query("SELECT s.id.chatId FROM SubscriptionEntity s WHERE s.id.linkId = :linkId")
    List<Long> findChatIdsByLinkId(@Param("linkId") Long linkId);

    @Modifying
    @Query("DELETE FROM SubscriptionEntity s WHERE s.id.chatId = :chatId")
    void deleteByIdChatId(@Param("chatId") Long chatId);

    @Modifying
    @Query(value = """
    WITH inserted_tag AS (
        INSERT INTO tag (name) VALUES (:tagName)
        ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name
        RETURNING id
    )
    INSERT INTO subscription_tag (chat_id, link_id, tag_id)
    SELECT :chatId, :linkId, id FROM inserted_tag
    ON CONFLICT DO NOTHING
    """, nativeQuery = true)
    void addTagNative(@Param("chatId") Long chatId, @Param("linkId") Long linkId, @Param("tagName") String tagName);

    @Modifying
    @Query(value = """
    DELETE FROM subscription_tag
    WHERE chat_id = :chatId AND link_id = :linkId
    AND tag_id = (SELECT id FROM tag WHERE name = :tagName)
    """, nativeQuery = true)
    void removeTagNative(@Param("chatId") Long chatId, @Param("linkId") Long linkId, @Param("tagName") String tagName);
}
