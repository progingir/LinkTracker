package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import backend.academy.linktracker.scrapper.repository.TagRepository;
import backend.academy.linktracker.scrapper.repository.TgChatRepository;
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

@Configuration
@ConditionalOnProperty(prefix = "app.database", name = "access-type", havingValue = "jpa")
public class JpaDatabaseConfig {

    @Bean
    public LinkRepository linkRepository(SpringDataJpaLinkRepository jpaRepository) {
        return new JpaLinkRepository(jpaRepository);
    }

    @Bean
    public TgChatRepository tgChatRepository(SpringDataJpaTgChatRepository jpaRepository, EntityManager entityManager) {
        return new JpaTgChatRepository(jpaRepository, entityManager);
    }

    @Bean
    public TagRepository tagRepository(SpringDataJpaTagRepository jpaRepository) {
        return new JpaTagRepository(jpaRepository);
    }

    @Bean
    public SubscriptionRepository subscriptionRepository(
            SpringDataJpaSubscriptionRepository subscriptionRepo,
            SpringDataJpaTgChatRepository chatRepo,
            SpringDataJpaLinkRepository linkRepo,
            SpringDataJpaTagRepository tagRepo,
            EntityManager entityManager) {
        return new JpaSubscriptionRepository(subscriptionRepo, chatRepo, linkRepo, tagRepo, entityManager);
    }
}
