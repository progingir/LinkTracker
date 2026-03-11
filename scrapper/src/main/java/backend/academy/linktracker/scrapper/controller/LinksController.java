package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.LinkResponse;
import backend.academy.linktracker.scrapper.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.dto.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.service.LinkService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/links")
@RequiredArgsConstructor
public class LinksController {

    private final LinkService linkService;

    @GetMapping
    public ListLinksResponse getLinks(@RequestHeader("Tg-Chat-Id") Long tgChatId) {
        log.atInfo()
            .addKeyValue("chat_id", tgChatId)
            .log("Запрос на получение списка отслеживаемых ссылок");

        List<Link> links = linkService.getLinks(tgChatId);

        List<LinkResponse> responseList = links.stream()
                .map(link -> new LinkResponse(link.id(), link.url(), link.tags(), link.filters()))
                .toList();

        log.atInfo()
                .addKeyValue("chat_id", tgChatId)
                .addKeyValue("links_count", responseList.size())
                .log("Список ссылок успешно сформирован");
        return new ListLinksResponse(responseList, responseList.size());
    }

    @PostMapping
    public LinkResponse addLink(
            @RequestHeader("Tg-Chat-Id") Long tgChatId, @Valid @RequestBody AddLinkRequest request) {

        log.atInfo()
                .addKeyValue("chat_id", tgChatId)
                .addKeyValue("link", request.link())
                .log("Запрос на добавление ссылки");

        Link savedLink = linkService.addLink(tgChatId, request.link(), request.tags(), request.filters());

        log.atInfo()
                .addKeyValue("chat_id", tgChatId)
                .addKeyValue("link_id", savedLink.id())
                .log("Ссылка успешно добавлена");

        return new LinkResponse(savedLink.id(), savedLink.url(), savedLink.tags(), savedLink.filters());
    }

    @DeleteMapping
    public LinkResponse removeLink(
            @RequestHeader("Tg-Chat-Id") Long tgChatId, @Valid @RequestBody RemoveLinkRequest request) {

        log.atInfo()
                .addKeyValue("chat_id", tgChatId)
                .addKeyValue("link", request.link())
                .log("Запрос на удаление ссылки");

        Link removedLink = linkService.removeLink(tgChatId, request.link());

        log.atInfo()
                .addKeyValue("chat_id", tgChatId)
                .addKeyValue("link_id", removedLink.id())
                .log("Ссылка успешно удалена");

        return new LinkResponse(removedLink.id(), removedLink.url(), removedLink.tags(), removedLink.filters());
    }
}
