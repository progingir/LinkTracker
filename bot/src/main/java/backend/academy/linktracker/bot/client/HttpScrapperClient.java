package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.bot.dto.*;
import backend.academy.linktracker.bot.exception.ResourceAlreadyExistsException;
import backend.academy.linktracker.bot.exception.ResourceNotFoundException;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "scrapper-client-type", havingValue = "http")
public class HttpScrapperClient implements ScrapperClient {

    private final RestClient scrapperRestClient;
    private static final String TG_CHAT_ID_HEADER = "Tg-Chat-Id";

    @Override
    public void registerChat(Long chatId) {
        log.atInfo().addKeyValue("chat_id", chatId).log("http: отправка запроса на регистрацию чата");

        scrapperRestClient
                .post()
                .uri("/tg-chat/{id}", chatId)
                .retrieve()
                .onStatus(status -> status == HttpStatus.CONFLICT, (request, response) -> {
                    throw new ResourceAlreadyExistsException("Чат " + chatId + " уже зарегистрирован");
                })
                .toBodilessEntity();
        log.atInfo().addKeyValue("chat_id", chatId).log("Чат успешно зарегистрирован");
    }

    @Override
    public void deleteChat(Long chatId) {
        log.atInfo().addKeyValue("chat_id", chatId).log("http: отправка запроса на удаление чата");
        scrapperRestClient
                .delete()
                .uri("/tg-chat/{id}", chatId)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND, (req, res) -> {
                    throw new ResourceNotFoundException("Чат не найден: " + chatId);
                })
                .toBodilessEntity();

        log.atInfo().addKeyValue("chat_id", chatId).log("Чат успешно удален");
    }

    @Override
    public ListLinksResponse getLinks(Long chatId) {
        log.atInfo().addKeyValue("chat_id", chatId).log("http: отправка запроса на получение ссылок");

        ListLinksResponse response = scrapperRestClient
                .get()
                .uri("/links")
                .header(TG_CHAT_ID_HEADER, String.valueOf(chatId))
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND, (req, res) -> {
                    throw new ResourceNotFoundException("Чат не найден: " + chatId);
                })
                .body(ListLinksResponse.class);

        log.atInfo().addKeyValue("chat_id", chatId).log("Список ссылок успешно получен");
        return response;
    }

    @Override
    public LinkResponse addLink(Long chatId, URI link, List<String> tags, List<String> filters) {
        log.atInfo()
                .addKeyValue("chat_id", chatId)
                .addKeyValue("url", link)
                .log("http: отправка запроса на добавление ссылки");

        LinkResponse response = scrapperRestClient
                .post()
                .uri("/links")
                .header(TG_CHAT_ID_HEADER, String.valueOf(chatId))
                .body(new AddLinkRequest(link, tags, filters))
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND, (req, res) -> {
                    throw new ResourceNotFoundException("Чат не найден: " + chatId);
                })
                .onStatus(status -> status == HttpStatus.CONFLICT, (req, res) -> {
                    throw new ResourceAlreadyExistsException("Ссылка уже отслеживается");
                })
                .body(LinkResponse.class);

        log.atInfo().addKeyValue("chat_id", chatId).addKeyValue("url", link).log("Ссылка успешно добавлена");
        return response;
    }

    @Override
    public LinkResponse removeLink(Long chatId, URI link) {
        log.atInfo()
                .addKeyValue("chat_id", chatId)
                .addKeyValue("url", link)
                .log("http: отправка запроса на удаление ссылки");

        LinkResponse response = scrapperRestClient
                .method(HttpMethod.DELETE)
                .uri("/links")
                .header(TG_CHAT_ID_HEADER, String.valueOf(chatId))
                .body(new RemoveLinkRequest(link))
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND, (req, res) -> {
                    throw new ResourceNotFoundException("Ссылка или чат не найдены");
                })
                .body(LinkResponse.class);

        log.atInfo().addKeyValue("chat_id", chatId).addKeyValue("url", link).log("Ссылка успешно удалена");
        return response;
    }
}
