package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataJpaTagRepository extends JpaRepository<TagEntity, Long> {

    Optional<TagEntity> findByName(String name);

    @Query(value = "SELECT DISTINCT t.* FROM tag t JOIN subscription_tag st ON t.id = st.tag_id WHERE st.chat_id = :chatId", nativeQuery = true)
    List<TagEntity> findAllByChatId(@Param("chatId") Long chatId);

    @Modifying
    @Query(value = """
        INSERT INTO tag (name) VALUES (:name)
        ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name
        """, nativeQuery = true)
    void upsert(@Param("name") String name);
}
