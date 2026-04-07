package backend.academy.linktracker.scrapper;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "app.database.access-type=jpa")
class JpaRepositoryTest extends AbstractRepositoryTest {}
