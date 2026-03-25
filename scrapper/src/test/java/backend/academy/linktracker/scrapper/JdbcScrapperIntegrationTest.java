package backend.academy.linktracker.scrapper;

import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "app.database.access-type=jdbc")
class JdbcScrapperIntegrationTest extends ScrapperIntegrationTestBase {
}
