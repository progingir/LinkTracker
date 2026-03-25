package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import backend.academy.linktracker.scrapper.repository.TagRepository;
import backend.academy.linktracker.scrapper.repository.TgChatRepository;

import backend.academy.linktracker.scrapper.repository.jdbc.JdbcLinkRepository;
import backend.academy.linktracker.scrapper.repository.jdbc.JdbcSubscriptionRepository;
import backend.academy.linktracker.scrapper.repository.jdbc.JdbcTagRepository;
import backend.academy.linktracker.scrapper.repository.jdbc.JdbcTgChatRepository;

import backend.academy.linktracker.scrapper.repository.jpa.JpaLinkRepository;
import backend.academy.linktracker.scrapper.repository.jpa.JpaSubscriptionRepository;
import backend.academy.linktracker.scrapper.repository.jpa.JpaTagRepository;
import backend.academy.linktracker.scrapper.repository.jpa.JpaTgChatRepository;

import backend.academy.linktracker.scrapper.repository.jpa.SpringDataJpaLinkRepository;
import backend.academy.linktracker.scrapper.repository.jpa.SpringDataJpaSubscriptionRepository;
import backend.academy.linktracker.scrapper.repository.jpa.SpringDataJpaTagRepository;
import backend.academy.linktracker.scrapper.repository.jpa.SpringDataJpaTgChatRepository;

import jakarta.persistence.EntityManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

@Configuration
public class DatabaseConfig {

    @Bean
    @ConditionalOnProperty(prefix = "app.database", name = "access-type", havingValue = "jdbc", matchIfMissing = true)
    public LinkRepository jdbcLinkRepository(JdbcClient jdbcClient) {
        return new JdbcLinkRepository(jdbcClient);
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.database", name = "access-type", havingValue = "jdbc", matchIfMissing = true)
    public TgChatRepository jdbcTgChatRepository(JdbcClient jdbcClient) {
        return new JdbcTgChatRepository(jdbcClient);
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.database", name = "access-type", havingValue = "jdbc", matchIfMissing = true)
    public SubscriptionRepository jdbcSubscriptionRepository(JdbcClient jdbcClient) {
        return new JdbcSubscriptionRepository(jdbcClient);
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.database", name = "access-type", havingValue = "jdbc", matchIfMissing = true)
    public TagRepository jdbcTagRepository(JdbcClient jdbcClient) {
        return new JdbcTagRepository(jdbcClient);
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.database", name = "access-type", havingValue = "jpa")
    public LinkRepository linkRepository(SpringDataJpaLinkRepository jpaRepository) {
        return new JpaLinkRepository(jpaRepository);
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.database", name = "access-type", havingValue = "jpa")
    public TgChatRepository jpaTgChatRepository(
        SpringDataJpaTgChatRepository jpaRepository,
        EntityManager entityManager) {
        return new JpaTgChatRepository(jpaRepository, entityManager);
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.database", name = "access-type", havingValue = "jpa")
    public TagRepository jpaTagRepository(SpringDataJpaTagRepository jpaRepository) {
        return new JpaTagRepository(jpaRepository);
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.database", name = "access-type", havingValue = "jpa")
    public SubscriptionRepository jpaSubscriptionRepository(
        SpringDataJpaSubscriptionRepository subscriptionRepo,
        SpringDataJpaTgChatRepository chatRepo,
        SpringDataJpaLinkRepository linkRepo,
        SpringDataJpaTagRepository tagRepo,
        EntityManager entityManager) {
        return new JpaSubscriptionRepository(subscriptionRepo, chatRepo, linkRepo, tagRepo, entityManager);
    }
}
