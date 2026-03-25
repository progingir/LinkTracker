package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;


@RequiredArgsConstructor
public class JdbcLinkRepository implements LinkRepository {

    private static final OffsetDateTime NEVER_CHECKED = OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);

    private final JdbcClient jdbcClient;

    @Override
    public Link save(URI url) {
        String sql = """
            INSERT INTO link (url, last_update, last_check_at)
            VALUES (:url, :lastUpdate, :lastCheckAt)
            RETURNING id, url, last_update, last_check_at
            """;

        return jdbcClient.sql(sql)
            .param("url", url.toString())
            .param("lastUpdate", OffsetDateTime.now())
            .param("lastCheckAt", NEVER_CHECKED)
            .query((rs, rowNum) -> new Link(
                rs.getLong("id"),
                URI.create(rs.getString("url")),
                rs.getObject("last_update", OffsetDateTime.class),
                rs.getObject("last_check_at", OffsetDateTime.class)
            )).single();
    }

    @Override
    public Optional<Link> findByUrl(URI url) {
        return jdbcClient.sql("SELECT id, url, last_update, last_check_at FROM link WHERE url = :url")
            .param("url", url.toString())
            .query((rs, rowNum) -> new Link(
                rs.getLong("id"),
                URI.create(rs.getString("url")),
                rs.getObject("last_update", OffsetDateTime.class),
                rs.getObject("last_check_at", OffsetDateTime.class)
            )).optional();
    }

    @Override
    public Optional<Link> findById(Long id) {
        return jdbcClient.sql("SELECT id, url, last_update, last_check_at FROM link WHERE id = :id")
            .param("id", id)
            .query((rs, rowNum) -> new Link(
                rs.getLong("id"),
                URI.create(rs.getString("url")),
                rs.getObject("last_update", OffsetDateTime.class),
                rs.getObject("last_check_at", OffsetDateTime.class)
            )).optional();
    }

    @Override
    public void remove(Long id) {
        jdbcClient.sql("DELETE FROM link WHERE id = :id")
            .param("id", id)
            .update();
    }

    @Override
    public List<Link> findOldest(int limit) {
        String sql = "SELECT id, url, last_update, last_check_at FROM link ORDER BY last_check_at ASC LIMIT :limit";
        return jdbcClient.sql(sql)
            .param("limit", limit)
            .query((rs, rowNum) -> new Link(
                rs.getLong("id"),
                URI.create(rs.getString("url")),
                rs.getObject("last_update", OffsetDateTime.class),
                rs.getObject("last_check_at", OffsetDateTime.class)
            )).list();
    }

    @Override
    public void updateLastCheckTime(Long linkId, OffsetDateTime lastCheck) {
        jdbcClient.sql("UPDATE link SET last_check_at = :lastCheck WHERE id = :id")
            .param("lastCheck", lastCheck)
            .param("id", linkId)
            .update();
    }

    @Override
    public void updateLastUpdateTime(Long linkId, OffsetDateTime lastUpdate) {
        jdbcClient.sql("UPDATE link SET last_update = :lastUpdate WHERE id = :id")
            .param("lastUpdate", lastUpdate)
            .param("id", linkId)
            .update();
    }
}
