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
}
