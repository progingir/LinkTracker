//package backend.academy.linktracker.scrapper;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import backend.academy.linktracker.scrapper.dto.LinkUpdate;
//import backend.academy.linktracker.scrapper.entity.OutboxMessageEntity;
//import backend.academy.linktracker.scrapper.repository.jpa.SpringDataJpaOutboxRepository;
//import backend.academy.linktracker.scrapper.service.update.OutboxUpdateSender;
//import java.net.URI;
//import java.util.List;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//@SpringBootTest
//public class OutboxSaveIntegrationTest extends ScrapperIntegrationTestBase {
//
//    @Autowired
//    private OutboxUpdateSender outboxUpdateSender;
//
//    @Autowired
//    private SpringDataJpaOutboxRepository outboxRepository;
//
//    @Test
//    @Transactional
//    void shouldSaveMessageToOutboxTable() {
//        LinkUpdate update = new LinkUpdate(
//                1L, URI.create("https://github.com/test/repo"), "Test description", List.of(12345L), false);
//
//        outboxUpdateSender.sendUpdate(update);
//
//        List<OutboxMessageEntity> messages = outboxRepository.findAll();
//
//        assertThat(messages).hasSize(1);
//        OutboxMessageEntity savedMessage = messages.get(0);
//
//        assertThat(savedMessage.getStatus()).isEqualTo(OutboxMessageEntity.OutboxStatus.PENDING);
//        assertThat(savedMessage.getPayload()).contains("https://github.com/test/repo");
//        assertThat(savedMessage.getPayload()).contains("12345");
//    }
//}
