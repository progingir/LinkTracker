package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.domain.Tag;
import backend.academy.linktracker.scrapper.entity.TagEntity;
import backend.academy.linktracker.scrapper.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class JpaTagRepository implements TagRepository {

    private final SpringDataJpaTagRepository jpaRepository;

    @Override
    @Transactional
    public Tag save(String name) {
        jpaRepository.upsert(name);
        TagEntity entity = jpaRepository.findByName(name)
            .orElseThrow(() -> new IllegalStateException("Тег должен существовать после upsert"));

        return mapToDomain(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tag> findById(Long id) {
        return jpaRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void update(Long id, String newName) {
        jpaRepository.findById(id).ifPresent(entity -> {
            entity.setName(newName);
            jpaRepository.save(entity);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tag> findAllByChatId(Long chatId) {
        return jpaRepository.findAllByChatId(chatId).stream()
            .map(this::mapToDomain)
            .toList();
    }

    private Tag mapToDomain(TagEntity entity) {
        return new Tag(entity.getId(), entity.getName());
    }
}
