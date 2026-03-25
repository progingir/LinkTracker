package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.domain.Tag;
import java.util.List;
import java.util.Optional;

public interface TagRepository {
    Tag save(String name);
    Optional<Tag> findById(Long id);
    void delete(Long id);
    void update(Long id, String newName);

    List<Tag> findAllByChatId(Long chatId);
}
