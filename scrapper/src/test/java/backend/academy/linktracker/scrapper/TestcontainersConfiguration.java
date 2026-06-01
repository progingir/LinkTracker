package backend.academy.linktracker.scrapper;

import com.redis.testcontainers.RedisContainer;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.DirectoryResourceAccessor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgresContainer() {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse("mirror.gcr.io/library/postgres:16-alpine").asCompatibleSubstituteFor("postgres"))
                .withDatabaseName("scrapper-test-db")
                .withUsername("postgres")
                .withPassword("12345");

        container.start();

        runMigrations(container);

        return container;
    }

    @Bean
    @ServiceConnection
    public KafkaContainer kafkaContainer() {
        return new KafkaContainer(DockerImageName.parse("mirror.gcr.io/apache/kafka:3.7.0").asCompatibleSubstituteFor("apache/kafka"));
    }

    @Bean
    @ServiceConnection
    public RedisContainer redisContainer() {
        return new RedisContainer(DockerImageName.parse("mirror.gcr.io/valkey/valkey:8.0").asCompatibleSubstituteFor("valkey/valkey"));
    }

    @Bean
    @Primary
    public LettuceConnectionFactory redisConnectionFactory(RedisContainer redisContainer) {
        RedisStandaloneConfiguration config =
                new RedisStandaloneConfiguration(redisContainer.getHost(), redisContainer.getFirstMappedPort());
        return new LettuceConnectionFactory(config);
    }

    private void runMigrations(PostgreSQLContainer<?> c) {
        try {
            Connection connection = DriverManager.getConnection(c.getJdbcUrl(), c.getUsername(), c.getPassword());
            Database database =
                    DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

            File migrationsDir = new File(".").getAbsolutePath().contains("scrapper")
                    ? new File("../migrations")
                    : new File("migrations");

            Liquibase liquibase = new Liquibase(
                    "master.xml", new DirectoryResourceAccessor(migrationsDir.getAbsoluteFile()), database);

            liquibase.update(new Contexts(), new LabelExpression());

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при программном запуске миграций Liquibase", e);
        }
    }
}


