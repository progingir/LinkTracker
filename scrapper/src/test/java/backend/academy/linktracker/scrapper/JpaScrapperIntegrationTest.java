package backend.academy.linktracker.scrapper;

import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "app.database.access-type=jpa")
class JpaScrapperIntegrationTest extends ScrapperIntegrationTestBase {
}
