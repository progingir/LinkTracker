package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.domain.Tag;
import backend.academy.linktracker.scrapper.repository.TagRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;

@RequiredArgsConstructor
public class JdbcTagRepository implements TagRepository {

    private final JdbcClient jdbcClient;

    @Override
    public Tag save(String name) {
        return jdbcClient.sql("""
                INSERT INTO tag (name) VALUES (:name)
                ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name
                RETURNING id, name
                """)
            .param("name", name)
            .query((rs, rowNum) -> new Tag(rs.getLong("id"), rs.getString("name")))
            .single();
    }

    @Override
    public Optional<Tag> findById(Long id) {
        return jdbcClient.sql("SELECT id, name FROM tag WHERE id = :id")
            .param("id", id)
            .query((rs, rowNum) -> new Tag(rs.getLong("id"), rs.getString("name")))
            .optional();
    }

    @Override
    public void delete(Long id) {
        jdbcClient.sql("DELETE FROM tag WHERE id = :id")
            .param("id", id)
            .update();
    }

    @Override
    public void update(Long id, String newName) {
        jdbcClient.sql("UPDATE tag SET name = :name WHERE id = :id")
            .param("name", newName)
            .param("id", id)
            .update();
    }

    @Override
    public List<Tag> findAllByChatId(Long chatId) {
        return jdbcClient.sql("""
                SELECT DISTINCT t.id, t.name
                FROM tag t
                JOIN subscription_tag st ON t.id = st.tag_id
                WHERE st.chat_id = :chatId
                """)
            .param("chatId", chatId)
            .query((rs, rowNum) -> new Tag(rs.getLong("id"), rs.getString("name")))
            .list();
    }
}
