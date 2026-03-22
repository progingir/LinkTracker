package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.domain.Link;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryLinkRepository implements LinkRepository {

    private final Map<Long, List<Link>> chatLinks = new ConcurrentHashMap<>();
    private final AtomicLong linkIdGenerator = new AtomicLong(1);

    @Override
    public List<Link> findAllByChatId(Long chatId) {
        return chatLinks.getOrDefault(chatId, List.of());
    }

    @Override
    public Optional<Link> save(Long chatId, URI url, List<String> tags) {
        AtomicReference<Optional<Link>> result = new AtomicReference<>(Optional.empty());

        chatLinks.compute(chatId, (key, links) -> {
            if (links == null) {
                links = new CopyOnWriteArrayList<>();
            }

            for (Link link : links) {
                if (link.url().equals(url)) {
                    return links;
                }
            }

            Link newLink = new Link(
                linkIdGenerator.getAndIncrement(),
                chatId,
                url,
                tags != null ? tags : List.of(),
                OffsetDateTime.now());

            links.add(newLink);
            result.set(Optional.of(newLink));

            return links;
        });

        return result.get();
    }

    @Override
    public Optional<Link> remove(Long chatId, URI url) {
        AtomicReference<Optional<Link>> result = new AtomicReference<>(Optional.empty());

        chatLinks.computeIfPresent(chatId, (key, links) -> {
            Link targetLink = null;

            for (Link link : links) {
                if (link.url().equals(url)) {
                    targetLink = link;
                    break;
                }
            }

            if (targetLink != null) {
                links.remove(targetLink);
                result.set(Optional.of(targetLink));
            }

            return links;
        });

        return result.get();
    }

    @Override
    public void removeAllByChatId(Long chatId) {
        chatLinks.remove(chatId);
    }

    @Override
    public List<Link> findAll() {
        return chatLinks.values().stream().flatMap(List::stream).toList();
    }

    @Override
    public void updateLastUpdate(Long linkId, OffsetDateTime updatedAt) {
        chatLinks.values().forEach(links -> {
            for (int i = 0; i < links.size(); i++) {
                Link l = links.get(i);
                if (l.id().equals(linkId)) {
                    links.set(i, new Link(l.id(), l.chatId(), l.url(), l.tags(), updatedAt));
                }
            }
        });
    }
}
