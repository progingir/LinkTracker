package backend.academy.linktracker.bot;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer(DockerImageName.parse("mirror.gcr.io/library/postgres:18-alpine").asCompatibleSubstituteFor("postgres"));
    }

    RedisContainer redisContainer() {
        return new RedisContainer(DockerImageName.parse("mirror.gcr.io/library/redis:8.2-alpine").asCompatibleSubstituteFor("redis"));
    }

    @Bean
    @ServiceConnection
    public KafkaContainer kafkaContainer() {
        return new KafkaContainer(DockerImageName.parse("mirror.gcr.io/apache/kafka:3.7.0").asCompatibleSubstituteFor("apache/kafka"));
    }
}



