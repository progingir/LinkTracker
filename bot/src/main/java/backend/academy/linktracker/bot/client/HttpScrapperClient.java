package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.bot.dto.*;
import backend.academy.linktracker.bot.exception.ScrapperApiException;
import backend.academy.linktracker.bot.properties.TelegramProperties;
import java.net.URI;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class HttpScrapperClient implements ScrapperClient {

    private final RestClient restClient;
    private static final String TG_CHAT_ID_HEADER = "Tg-Chat-Id";

    public HttpScrapperClient(TelegramProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.getScrapperUrl())
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    throw new ScrapperApiException(
                            response.getStatusCode().value(), "Ошибка API Scrapper: " + response.getStatusCode());
                })
                .build();
    }

    @Override
    public void registerChat(Long chatId) {
        log.atInfo().addKeyValue("chat_id", chatId).log("Отправка запроса на регистрацию чата в Scrapper");

        restClient
                .post()
                .uri("/tg-chat/{id}", chatId)
                .retrieve()
                .onStatus(status -> status == HttpStatus.CONFLICT, (request, response) -> {
                    log.atInfo().addKeyValue("chat_id", chatId).log("Чат уже был зарегистрирован ранее");
                })
                .toBodilessEntity();
    }

    @Override
    public void deleteChat(Long chatId) {
        log.atInfo().addKeyValue("chat_id", chatId).log("Отправка запроса на удаление чата из Scrapper");
        restClient.delete().uri("/tg-chat/{id}", chatId).retrieve().toBodilessEntity();
    }

    @Override
    public ListLinksResponse getLinks(Long chatId) {
        log.atInfo().addKeyValue("chat_id", chatId).log("Запрос списка ссылок из Scrapper");
        return restClient
                .get()
                .uri("/links")
                .header(TG_CHAT_ID_HEADER, String.valueOf(chatId))
                .retrieve()
                .body(ListLinksResponse.class);
    }

    @Override
    public LinkResponse addLink(Long chatId, URI link, List<String> tags, List<String> filters) {
        log.atInfo()
                .addKeyValue("chat_id", chatId)
                .addKeyValue("link", link)
                .log("Отправка запроса на добавление ссылки");
        return restClient
                .post()
                .uri("/links")
                .header(TG_CHAT_ID_HEADER, String.valueOf(chatId))
                .body(new AddLinkRequest(link, tags, filters))
                .retrieve()
                .body(LinkResponse.class);
    }

    @Override
    public LinkResponse removeLink(Long chatId, URI link) {
        log.atInfo()
                .addKeyValue("chat_id", chatId)
                .addKeyValue("link", link)
                .log("Отправка запроса на удаление ссылки из Scrapper");

        return restClient
                .method(HttpMethod.DELETE)
                .uri("/links")
                .header(TG_CHAT_ID_HEADER, String.valueOf(chatId))
                .body(new RemoveLinkRequest(link))
                .retrieve()
                .body(LinkResponse.class);
    }
}
