package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.domain.Subscription;
import backend.academy.linktracker.scrapper.dto.LinkResponse;
import backend.academy.linktracker.scrapper.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.exception.LinkNotFoundException;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import backend.academy.linktracker.scrapper.repository.TgChatRepository;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;
    private final TgChatRepository tgChatRepository;
    private final SubscriptionRepository subscriptionRepository;

    public LinkResponse addLinkFromExternal(Long chatId, String urlStr, List<String> tags) {
        validateUrl(urlStr);
        return addLinkAndMap(chatId, URI.create(urlStr), tags);
    }

    public LinkResponse removeLinkFromExternal(Long chatId, String urlStr) {
        validateUrl(urlStr);
        return removeLinkAndMap(chatId, URI.create(urlStr));
    }

    @Transactional(readOnly = true)
    public ListLinksResponse getLinksResponse(Long chatId, int limit, Long lastLinkId) {
        validateChatId(chatId);
        checkChatExists(chatId);

        List<LinkResponse> responseList = subscriptionRepository.findByChatId(chatId, limit, lastLinkId).stream()
                .map(this::mapToResponse)
                .toList();

        return new ListLinksResponse(responseList, responseList.size());
    }

    @Transactional
    public LinkResponse addLinkAndMap(Long chatId, URI uri, List<String> tags) {
        validateChatId(chatId);
        checkChatExists(chatId);

        Link link;
        try {
            link = linkRepository.findByUrl(uri).orElseGet(() -> linkRepository.save(uri));
        } catch (DataIntegrityViolationException e) {
            log.atInfo()
                    .addKeyValue("url", uri)
                    .log("Обнаружено состояние гонки для URL. Извлекаем существующую ссылку");

            link = linkRepository
                    .findByUrl(uri)
                    .orElseThrow(() -> new IllegalStateException("Ссылка должна существовать, но не найдена", e));
        }

        if (subscriptionRepository.exists(chatId, link.id())) {
            throw new LinkAlreadyTrackedException(uri);
        }

        subscriptionRepository.addSubscription(chatId, link.id());

        List<String> safeTags = tags == null ? List.of() : tags;
        for (String tag : safeTags) {
            subscriptionRepository.addTagToSubscription(chatId, link.id(), tag);
        }

        return new LinkResponse(link.id(), link.url(), safeTags);
    }

    @Transactional
    public LinkResponse removeLinkAndMap(Long chatId, URI uri) {
        validateChatId(chatId);
        checkChatExists(chatId);

        Link link = linkRepository.findByUrl(uri).orElseThrow(() -> new LinkNotFoundException(uri));

        if (!subscriptionRepository.exists(chatId, link.id())) {
            throw new LinkNotFoundException(uri);
        }

        subscriptionRepository.removeSubscription(chatId, link.id());

        List<Long> remainingSubscribers = subscriptionRepository.findChatIdsByLinkId(link.id());

        if (remainingSubscribers.isEmpty()) {
            log.atInfo().addKeyValue("url", uri).log("Ссылка больше не отслеживается ни одним чатом. Удаляем из БД");

            linkRepository.remove(link.id());
        }

        return new LinkResponse(link.id(), link.url(), List.of());
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
            URI uri = URI.create(urlStr);
            String scheme = uri.getScheme();

            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("Поддерживаются только HTTP и HTTPS ссылки");
            }

            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new IllegalArgumentException("URL должен содержать доменное имя (например, github.com)");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.atWarn().setCause(e).addKeyValue("url", urlStr).log("Некорректный синтаксис URL");
            throw new IllegalArgumentException("Некорректный синтаксис URL: " + urlStr);
        }
    }

    private void checkChatExists(Long chatId) {
        if (!tgChatRepository.existsChat(chatId)) {
            throw new ChatNotFoundException(chatId);
        }
    }

    private LinkResponse mapToResponse(Subscription subscription) {
        return new LinkResponse(subscription.linkId(), subscription.url(), subscription.tags());
    }
}
