package backend.academy.linktracker.scrapper;

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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgresContainer() {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("scrapper-test-db")
            .withUsername("postgres")
            .withPassword("12345");

        container.start();

        runMigrations(container);

        return container;
    }

    private void runMigrations(PostgreSQLContainer<?> c) {
        try {
            Connection connection = DriverManager.getConnection(c.getJdbcUrl(), c.getUsername(), c.getPassword());
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

            File migrationsDir = new File(".").getAbsolutePath().contains("scrapper")
                ? new File("../migrations")
                : new File("migrations");

            Liquibase liquibase = new Liquibase("master.xml",
                new DirectoryResourceAccessor(migrationsDir.getAbsoluteFile()), database);

            liquibase.update(new Contexts(), new LabelExpression());

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при программном запуске миграций Liquibase", e);
        }
    }
}
