package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.domain.Link;
import java.net.URI;
import java.util.List;
import java.util.Optional;

public interface LinkRepository {
    List<Link> findAllByChatId(Long chatId);

    Optional<Link> save(Long chatId, URI url, List<String> tags, List<String> filters);

    Optional<Link> remove(Long chatId, URI url);

    void removeAllByChatId(Long chatId);
}
