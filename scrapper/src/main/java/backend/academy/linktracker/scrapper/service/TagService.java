package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.domain.Tag;
import backend.academy.linktracker.scrapper.repository.TagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    @Transactional
    public Tag createTag(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя тега не может быть пустым");
        }
        return tagRepository.save(name);
    }

    @Transactional
    public void renameTag(Long id, String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Новое имя тега не может быть пустым");
        }
        tagRepository.update(id, newName);
    }

    @Transactional
    public void deleteTag(Long id) {
        tagRepository.delete(id);
    }

    @Transactional(readOnly = true)
    public List<Tag> getTagsByChatId(Long chatId) {
        return tagRepository.findAllByChatId(chatId);
    }
}
