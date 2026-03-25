package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.repository.TgChatRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;

@RequiredArgsConstructor
public class JdbcTgChatRepository implements TgChatRepository {

    private final JdbcClient jdbcClient;

    @Override
    public void addChat(Long chatId) {
        jdbcClient.sql("INSERT INTO chat (id) VALUES (:id) ON CONFLICT DO NOTHING")
            .param("id", chatId)
            .update();
    }

    @Override
    public void removeChat(Long chatId) {
        jdbcClient.sql("DELETE FROM chat WHERE id = :id")
            .param("id", chatId)
            .update();
    }

    @Override
    public boolean existsChat(Long chatId) {
        Integer count = jdbcClient.sql("SELECT count(*) FROM chat WHERE id = :id")
            .param("id", chatId)
            .query(Integer.class)
            .single();
        return count != null && count > 0;
    }

    @Override
    public List<Long> findAll(int limit, int offset) {
        return jdbcClient.sql("SELECT id FROM chat ORDER BY id ASC LIMIT :limit OFFSET :offset")
            .param("limit", limit)
            .param("offset", offset)
            .query(Long.class)
            .list();
    }
}
