package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.domain.Tag;
import backend.academy.linktracker.scrapper.dto.TagRequest;
import backend.academy.linktracker.scrapper.service.TagService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping
    public Tag createTag(@RequestBody TagRequest request) {
        return tagService.createTag(request.name());
    }

    @PutMapping("/{id}")
    public void renameTag(@PathVariable Long id, @RequestBody TagRequest request) {
        tagService.renameTag(id, request.name());
    }

    @DeleteMapping("/{id}")
    public void deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
    }

    @GetMapping("/chat/{chatId}")
    public List<Tag> getTagsByChat(@PathVariable Long chatId) {
        return tagService.getTagsByChatId(chatId);
    }
}
