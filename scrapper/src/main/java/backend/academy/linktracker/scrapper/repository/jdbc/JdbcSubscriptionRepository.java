package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.domain.Subscription;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;

@RequiredArgsConstructor
public class JdbcSubscriptionRepository implements SubscriptionRepository {

    private final JdbcClient jdbcClient;

    @Override
    public void addSubscription(Long chatId, Long linkId) {
        jdbcClient.sql("INSERT INTO subscription (chat_id, link_id) VALUES (:chatId, :linkId) ON CONFLICT DO NOTHING")
            .param("chatId", chatId)
            .param("linkId", linkId)
            .update();
    }

    @Override
    public void removeSubscription(Long chatId, Long linkId) {
        jdbcClient.sql("DELETE FROM subscription WHERE chat_id = :chatId AND link_id = :linkId")
            .param("chatId", chatId)
            .param("linkId", linkId)
            .update();
    }


    @Override
    public List<Subscription> findAllByChatId(Long chatId, int limit, int offset) {
        String sql = """
                SELECT s.chat_id, s.link_id, l.url, array_remove(array_agg(t.name), NULL) as tags
                FROM subscription s
                JOIN link l ON s.link_id = l.id
                LEFT JOIN subscription_tag st ON s.chat_id = st.chat_id AND s.link_id = st.link_id
                LEFT JOIN tag t ON st.tag_id = t.id
                WHERE s.chat_id = :chatId
                GROUP BY s.chat_id, s.link_id, l.url
                ORDER BY s.link_id ASC
                LIMIT :limit OFFSET :offset
            """;

        return jdbcClient.sql(sql)
            .param("chatId", chatId)
            .param("limit", limit)
            .param("offset", offset)
            .query((rs, rowNum) -> {
                String[] tagsArray = (String[]) rs.getArray("tags").getArray();
                List<String> tags = tagsArray.length > 0 ? Arrays.asList(tagsArray) : Collections.emptyList();

                return new Subscription(
                    rs.getLong("chat_id"),
                    rs.getLong("link_id"),
                    URI.create(rs.getString("url")),
                    tags
                );
            }).list();
    }

    @Override
    public boolean exists(Long chatId, Long linkId) {
        Integer count = jdbcClient.sql("SELECT count(*) FROM subscription WHERE chat_id = :chatId AND link_id = :linkId")
            .param("chatId", chatId)
            .param("linkId", linkId)
            .query(Integer.class)
            .single();
        return count != null && count > 0;
    }

    @Override
    public void removeAllByChatId(Long chatId) {
        jdbcClient.sql("DELETE FROM subscription WHERE chat_id = :chatId")
            .param("chatId", chatId)
            .update();
    }

    @Override
    public void addTagToSubscription(Long chatId, Long linkId, String tag) {
        jdbcClient.sql("INSERT INTO tag (name) VALUES (:name) ON CONFLICT DO NOTHING")
            .param("name", tag)
            .update();

        jdbcClient.sql("""
                    INSERT INTO subscription_tag (chat_id, link_id, tag_id)
                    SELECT :chatId, :linkId, id FROM tag WHERE name = :name
                    ON CONFLICT DO NOTHING
                """)
            .param("chatId", chatId)
            .param("linkId", linkId)
            .param("name", tag)
            .update();
    }

    @Override
    public void removeTagFromSubscription(Long chatId, Long linkId, String tag) {
        jdbcClient.sql("""
                    DELETE FROM subscription_tag
                    WHERE chat_id = :chatId AND link_id = :linkId
                    AND tag_id = (SELECT id FROM tag WHERE name = :name)
                """)
            .param("chatId", chatId)
            .param("linkId", linkId)
            .param("name", tag)
            .update();
    }

    @Override
    public List<Long> findChatIdsByLinkId(Long linkId) {
        return jdbcClient.sql("SELECT chat_id FROM subscription WHERE link_id = :linkId")
            .param("linkId", linkId)
            .query(Long.class)
            .list();
    }
}
