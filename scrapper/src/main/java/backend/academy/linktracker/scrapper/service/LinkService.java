package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.domain.Subscription;
import backend.academy.linktracker.scrapper.dto.LinkResponse;
import backend.academy.linktracker.scrapper.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.exception.LinkNotFoundException;
import backend.academy.linktracker.scrapper.exception.SubscriptionNotFoundException;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.SubscriptionRepository;
import backend.academy.linktracker.scrapper.repository.TgChatRepository;
import backend.academy.linktracker.scrapper.service.cache.LinkCacheService;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;
    private final TgChatRepository tgChatRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final LinkCacheService cacheService;

    @Transactional(readOnly = true)
    public ListLinksResponse getLinksResponse(Long chatId, int limit, Long lastLinkId) {
        validateChatId(chatId);
        checkChatExists(chatId);

        boolean cacheable = limit == 10 && lastLinkId == null;
        if (cacheable) {
            Optional<ListLinksResponse> cached = cacheService.getLinks(chatId);
            if (cached.isPresent()) {
                log.atDebug().addKeyValue("chat_id", chatId).log("Возвращаем список ссылок из кэша");
                return cached.orElseThrow();
            }
        }

        List<LinkResponse> responseList = subscriptionRepository.findByChatId(chatId, limit, lastLinkId).stream()
                .map(this::mapToResponse)
                .toList();

        ListLinksResponse response = new ListLinksResponse(responseList, responseList.size());

        if (cacheable) {
            cacheService.putLinks(chatId, response);
        }

        return response;
    }

    @Transactional
    public LinkResponse addLink(Long chatId, URI uri, List<String> tags) {
        validateChatId(chatId);
        checkChatExists(chatId);
        validateUri(uri);

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

        List<String> safeTags = (tags == null) ? List.of() : tags;
        for (String tag : safeTags) {
            subscriptionRepository.addTagToSubscription(chatId, link.id(), tag);
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                cacheService.evictLinks(chatId);
            }
        });

        return new LinkResponse(link.id(), link.url(), safeTags);
    }

    @Transactional
    public LinkResponse removeLink(Long chatId, URI uri) {
        validateChatId(chatId);
        checkChatExists(chatId);

        Link link = linkRepository.findByUrl(uri).orElseThrow(() -> new LinkNotFoundException(uri));

        if (!subscriptionRepository.exists(chatId, link.id())) {
            throw new SubscriptionNotFoundException(chatId, uri);
        }

        subscriptionRepository.removeSubscription(chatId, link.id());

        List<Long> remainingSubscribers = subscriptionRepository.findChatIdsByLinkId(link.id());
        if (remainingSubscribers.isEmpty()) {
            log.atInfo().addKeyValue("url", uri).log("Ссылка больше не отслеживается. Удаляем из БД");
            linkRepository.remove(link.id());
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                cacheService.evictLinks(chatId);
            }
        });

        return new LinkResponse(link.id(), link.url(), List.of());
    }

    @Transactional(readOnly = true)
    public List<Link> findOldest(int limit) {
        return linkRepository.findOldest(limit);
    }

    @Transactional
    public void updateLastCheckTimeBatch(List<Long> ids, OffsetDateTime lastCheck) {
        linkRepository.updateLastCheckTimeBatch(ids, lastCheck);
    }

    @Transactional
    public void updateLastUpdateTime(Long linkId, OffsetDateTime lastUpdate) {
        linkRepository.updateLastUpdateTime(linkId, lastUpdate);
    }

    @Transactional
    public void incrementErrorCount(Long linkId) {
        linkRepository.incrementErrorCount(linkId);
    }

    @Transactional
    public void resetErrorCount(Long linkId) {
        linkRepository.resetErrorCount(linkId);
    }

    private void validateChatId(Long chatId) {
        if (chatId == null || chatId <= 0) {
            throw new IllegalArgumentException("ID чата должен быть положительным");
        }
    }

    private void validateUri(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("URI не может быть пустым");
        }
        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException("Поддерживаются только HTTP и HTTPS ссылки");
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalArgumentException("URL должен содержать доменное имя");
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

    @Transactional
    public void updateLastUpdateTimesBatch(Map<Long, OffsetDateTime> updates) {
        if (updates == null || updates.isEmpty()) {
            return;
        }
        linkRepository.updateLastUpdateTimesBatch(updates);
    }
}
