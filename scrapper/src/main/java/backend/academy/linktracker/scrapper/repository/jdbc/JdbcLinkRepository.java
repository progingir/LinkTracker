package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;

@RequiredArgsConstructor
public class JdbcLinkRepository implements LinkRepository {

    private static final OffsetDateTime NEVER_CHECKED = OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);

    private static final String SELECT_FIELDS = "id, url, last_update, last_check_at, error_count";

    private static final String BASE_SELECT = "SELECT " + SELECT_FIELDS + " FROM link";

    private static final RowMapper<Link> LINK_MAPPER = (rs, rowNum) -> new Link(
            rs.getLong("id"),
            URI.create(rs.getString("url")),
            rs.getObject("last_update", OffsetDateTime.class),
            rs.getObject("last_check_at", OffsetDateTime.class),
            rs.getInt("error_count"));

    private final JdbcClient jdbcClient;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Link save(URI url) {
        String sql = "INSERT INTO link (url, last_update, last_check_at, error_count) "
                + "VALUES (:url, :lastUpdate, :lastCheckAt, 0) "
                + "RETURNING "
                + SELECT_FIELDS;

        return jdbcClient
                .sql(sql)
                .param("url", url.toString())
                .param("lastUpdate", OffsetDateTime.now())
                .param("lastCheckAt", NEVER_CHECKED)
                .query(LINK_MAPPER)
                .single();
    }

    @Override
    public Optional<Link> findByUrl(URI url) {
        return jdbcClient
                .sql(BASE_SELECT + " WHERE url = :url")
                .param("url", url.toString())
                .query(LINK_MAPPER)
                .optional();
    }

    @Override
    public Optional<Link> findById(Long id) {
        return jdbcClient
                .sql(BASE_SELECT + " WHERE id = :id")
                .param("id", id)
                .query(LINK_MAPPER)
                .optional();
    }

    @Override
    public List<Link> findOldest(int limit) {
        String sql = BASE_SELECT + " ORDER BY last_check_at ASC LIMIT :limit";
        return jdbcClient.sql(sql).param("limit", limit).query(LINK_MAPPER).list();
    }

    @Override
    public void remove(Long id) {
        jdbcClient.sql("DELETE FROM link WHERE id = :id").param("id", id).update();
    }

    @Override
    public void updateLastCheckTime(Long linkId, OffsetDateTime lastCheck) {
        jdbcClient
                .sql("UPDATE link SET last_check_at = :lastCheck WHERE id = :id")
                .param("lastCheck", lastCheck)
                .param("id", linkId)
                .update();
    }

    @Override
    public void updateLastUpdateTime(Long linkId, OffsetDateTime lastUpdate) {
        jdbcClient
                .sql("UPDATE link SET last_update = :lastUpdate WHERE id = :id")
                .param("lastUpdate", lastUpdate)
                .param("id", linkId)
                .update();
    }

    @Override
    public void updateLastUpdateTimesBatch(Map<Long, OffsetDateTime> updates) {
        if (updates.isEmpty()) {
            return;
        }

        String sql = "UPDATE link SET last_update = ? WHERE id = ?";

        List<Object[]> batchArgs = updates.entrySet().stream()
                .map(entry -> new Object[] {entry.getValue(), entry.getKey()})
                .toList();

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    @Override
    public void incrementErrorCount(Long linkId) {
        jdbcClient
                .sql("UPDATE link SET error_count = error_count + 1 WHERE id = :id")
                .param("id", linkId)
                .update();
    }

    @Override
    public void resetErrorCount(Long linkId) {
        jdbcClient
                .sql("UPDATE link SET error_count = 0 WHERE id = :id")
                .param("id", linkId)
                .update();
    }

    @Override
    public void updateLastCheckTimeBatch(List<Long> ids, OffsetDateTime lastCheck) {
        jdbcClient
                .sql("UPDATE link SET last_check_at = :lastCheck WHERE id IN (:ids)")
                .param("lastCheck", lastCheck)
                .param("ids", ids)
                .update();
    }
}
