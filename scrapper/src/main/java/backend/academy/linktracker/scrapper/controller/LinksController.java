package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.dto.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.LinkResponse;
import backend.academy.linktracker.scrapper.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.dto.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.service.LinkService;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/links")
@RequiredArgsConstructor
@RateLimiter(name = "ip-rate-limit")
public class LinksController {

    private final LinkService linkService;

    @GetMapping
    public ListLinksResponse getLinks(
            @RequestHeader("Tg-Chat-Id") Long tgChatId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Long lastLinkId) {

        log.atInfo()
                .addKeyValue("chat_id", tgChatId)
                .addKeyValue("last_link_id", lastLinkId)
                .log("Запрос на получение списка ссылок");

        return linkService.getLinksResponse(tgChatId, limit, lastLinkId);
    }

    @PostMapping
    public LinkResponse addLink(
            @RequestHeader("Tg-Chat-Id") Long tgChatId, @Valid @RequestBody AddLinkRequest request) {
        log.atInfo()
                .addKeyValue("chat_id", tgChatId)
                .addKeyValue("link", request.link())
                .log("Запрос на добавление ссылки");

        return linkService.addLink(tgChatId, request.link(), request.tags());
    }

    @DeleteMapping
    public LinkResponse removeLink(
            @RequestHeader("Tg-Chat-Id") Long tgChatId, @Valid @RequestBody RemoveLinkRequest request) {
        log.atInfo()
                .addKeyValue("chat_id", tgChatId)
                .addKeyValue("link", request.link())
                .log("Запрос на удаление ссылки");

        return linkService.removeLink(tgChatId, request.link());
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<String> handleRateLimitException(RequestNotPermitted e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Превышен лимит запросов");
    }
}
