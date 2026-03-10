package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.bot.dto.*;
import java.net.URI;
import java.util.List;

public interface ScrapperClient {
    void registerChat(Long chatId);

    void deleteChat(Long chatId);

    ListLinksResponse getLinks(Long chatId);

    LinkResponse addLink(Long chatId, URI link, List<String> tags);

    LinkResponse removeLink(Long chatId, URI link);
}
