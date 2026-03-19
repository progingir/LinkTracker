package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.LinkResponse;
import backend.academy.linktracker.scrapper.dto.ListLinksResponse;
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

    public LinkResponse addLinkFromExternal(Long chatId, String urlStr, List<String> tags, List<String> filters) {
        validateUrl(urlStr);
        return addLinkAndMap(chatId, URI.create(urlStr), tags, filters);
    }

    public LinkResponse removeLinkFromExternal(Long chatId, String urlStr) {
        validateUrl(urlStr);
        return removeLinkAndMap(chatId, URI.create(urlStr));
    }

    public ListLinksResponse getLinksResponse(Long chatId) {
        validateChatId(chatId);
        checkChatExists(chatId);

        List<LinkResponse> responseList = linkRepository.findAllByChatId(chatId).stream()
                .map(this::mapToResponse)
                .toList();

        return new ListLinksResponse(responseList, responseList.size());
    }

    public LinkResponse addLinkAndMap(Long chatId, URI uri, List<String> tags, List<String> filters) {
        validateChatId(chatId);
        checkChatExists(chatId);

        Link savedLink =
                linkRepository.save(chatId, uri, tags, filters).orElseThrow(() -> new LinkAlreadyTrackedException(uri));

        return mapToResponse(savedLink);
    }

    public LinkResponse removeLinkAndMap(Long chatId, URI uri) {
        validateChatId(chatId);
        checkChatExists(chatId);

        Link removedLink = linkRepository.remove(chatId, uri).orElseThrow(() -> new LinkNotFoundException(uri));

        return mapToResponse(removedLink);
    }

    private void validateChatId(Long chatId) {
        if (chatId == null || chatId <= 0) {
            throw new IllegalArgumentException("ID чата должен быть положительным");
        }
    }

    private void validateUrl(String urlStr) {
        if (urlStr == null || urlStr.isBlank()) {
            throw new IllegalArgumentException("URL не может быть пустым");
        }
        try {
            URI.create(urlStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Некорректный формат URL");
        }
    }

    private void checkChatExists(Long chatId) {
        if (!tgChatRepository.existsChat(chatId)) {
            throw new ChatNotFoundException(chatId);
        }
    }

    private LinkResponse mapToResponse(Link link) {
        return new LinkResponse(link.id(), link.url(), link.tags(), link.filters());
    }
}
