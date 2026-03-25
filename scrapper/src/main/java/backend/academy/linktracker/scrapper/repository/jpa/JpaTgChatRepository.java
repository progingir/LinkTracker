package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.entity.ChatEntity;
import backend.academy.linktracker.scrapper.repository.TgChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@RequiredArgsConstructor
public class JpaTgChatRepository implements TgChatRepository {

    private final SpringDataJpaTgChatRepository jpaRepository;
    private final jakarta.persistence.EntityManager entityManager;

    @Override
    @Transactional
    public void addChat(Long chatId) {
        if (!jpaRepository.existsById(chatId)) {
            ChatEntity chat = new ChatEntity();
            chat.setId(chatId);
            jpaRepository.saveAndFlush(chat);
        }
    }

    @Override
    @Transactional
    public void removeChat(Long chatId) {
        jpaRepository.deleteById(chatId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsChat(Long chatId) {
        return jpaRepository.existsById(chatId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findAll(int limit, int offset) {
        return entityManager.createQuery(
                "SELECT c.id FROM ChatEntity c ORDER BY c.id ASC", Long.class)
            .setFirstResult(offset)
            .setMaxResults(limit)
            .getResultList();
    }
}
