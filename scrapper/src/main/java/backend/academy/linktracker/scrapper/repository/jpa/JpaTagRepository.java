package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.domain.Tag;
import backend.academy.linktracker.scrapper.entity.TagEntity;
import backend.academy.linktracker.scrapper.repository.TagRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JpaTagRepository implements TagRepository {

    private final SpringDataJpaTagRepository jpaRepository;

    @Override
    public Tag save(String name) {
        jpaRepository.upsert(name);
        TagEntity entity = jpaRepository
                .findByName(name)
                .orElseThrow(() -> new IllegalStateException("Тег должен существовать после upsert"));

        return mapToDomain(entity);
    }

    @Override
    public Optional<Tag> findById(Long id) {
        return jpaRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public void delete(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void update(Long id, String newName) {
        jpaRepository.findById(id).ifPresent(entity -> {
            entity.setName(newName);
            jpaRepository.save(entity);
        });
    }

    @Override
    public List<Tag> findAllByChatId(Long chatId) {
        return jpaRepository.findAllByChatId(chatId).stream()
                .map(this::mapToDomain)
                .toList();
    }

    private Tag mapToDomain(TagEntity entity) {
        return new Tag(entity.getId(), entity.getName());
    }
}
