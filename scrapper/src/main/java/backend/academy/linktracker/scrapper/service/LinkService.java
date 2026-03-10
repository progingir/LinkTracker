package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.exception.LinkNotFoundException;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.TgChatRepository;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;
    private final TgChatRepository tgChatRepository;

    public List<Link> getLinks(Long chatId) {
        checkChatExists(chatId);
        return linkRepository.findAllByChatId(chatId);
    }

    public Link addLink(Long chatId, URI uri, List<String> tags, List<String> filters) {
        checkChatExists(chatId);

        return linkRepository.save(chatId, uri, tags, filters).orElseThrow(() -> new LinkAlreadyTrackedException(uri));
    }

    public Link removeLink(Long chatId, URI uri) {
        checkChatExists(chatId);

        return linkRepository.remove(chatId, uri).orElseThrow(() -> new LinkNotFoundException(uri));
    }

    private void checkChatExists(Long chatId) {
        if (!tgChatRepository.existsChat(chatId)) {
            throw new ChatNotFoundException(chatId);
        }
    }
}
