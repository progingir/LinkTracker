package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.OutboxRepository;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import backend.academy.linktracker.scrapper.repository.TagRepository;
import backend.academy.linktracker.scrapper.repository.TgChatRepository;
import backend.academy.linktracker.scrapper.repository.jdbc.JdbcLinkRepository;
import backend.academy.linktracker.scrapper.repository.jdbc.JdbcOutboxRepository;
import backend.academy.linktracker.scrapper.repository.jdbc.JdbcSubscriptionRepository;
import backend.academy.linktracker.scrapper.repository.jdbc.JdbcTagRepository;
import backend.academy.linktracker.scrapper.repository.jdbc.JdbcTgChatRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;

@Configuration
@ConditionalOnProperty(prefix = "app.database", name = "access-type", havingValue = "jdbc", matchIfMissing = true)
public class JdbcDatabaseConfig {

    @Bean
    public LinkRepository linkRepository(JdbcClient jdbcClient, JdbcTemplate jdbcTemplate) {
        return new JdbcLinkRepository(jdbcClient, jdbcTemplate);
    }

    @Bean
    public TgChatRepository tgChatRepository(JdbcClient jdbcClient) {
        return new JdbcTgChatRepository(jdbcClient);
    }

    @Bean
    public SubscriptionRepository subscriptionRepository(JdbcClient jdbcClient) {
        return new JdbcSubscriptionRepository(jdbcClient);
    }

    @Bean
    public TagRepository tagRepository(JdbcClient jdbcClient) {
        return new JdbcTagRepository(jdbcClient);
    }

    @Bean
    public OutboxRepository outboxRepository(JdbcClient jdbcClient) {
        return new JdbcOutboxRepository(jdbcClient);
    }
}
