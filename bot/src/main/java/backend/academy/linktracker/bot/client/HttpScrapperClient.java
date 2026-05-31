package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.bot.dto.*;
import backend.academy.linktracker.bot.exception.ResourceAlreadyExistsException;
import backend.academy.linktracker.bot.exception.ResourceNotFoundException;
import backend.academy.linktracker.bot.exception.ScrapperException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "scrapper-client-type", havingValue = "http")
@Retry(name = "scrapper")
@CircuitBreaker(name = "scrapper")
public class HttpScrapperClient implements ScrapperClient {

    private final RestClient scrapperRestClient;
    private final ObjectMapper objectMapper;
    private static final String TG_CHAT_ID_HEADER = "Tg-Chat-Id";

    @Override
    public void registerChat(Long chatId) {
        log.atInfo().addKeyValue("chat_id", chatId).log("http: отправка запроса на регистрацию чата");

        scrapperRestClient
                .post()
                .uri("/tg-chat/{id}", chatId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::getHttpError)
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
                .onStatus(HttpStatusCode::isError, this::getHttpError)
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
                .onStatus(HttpStatusCode::isError, this::getHttpError)
                .body(ListLinksResponse.class);

        log.atInfo().addKeyValue("chat_id", chatId).log("Список ссылок успешно получен");
        return response;
    }

    @Override
    public LinkResponse addLink(Long chatId, URI link, List<String> tags) {
        log.atInfo()
                .addKeyValue("chat_id", chatId)
                .addKeyValue("url", link)
                .log("http: отправка запроса на добавление ссылки");

        LinkResponse response = scrapperRestClient
                .post()
                .uri("/links")
                .header(TG_CHAT_ID_HEADER, String.valueOf(chatId))
                .body(new AddLinkRequest(link, tags))
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::getHttpError)
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
                .onStatus(HttpStatusCode::isError, this::getHttpError)
                .body(LinkResponse.class);

        log.atInfo().addKeyValue("chat_id", chatId).addKeyValue("url", link).log("Ссылка успешно удалена");
        return response;
    }

    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void getHttpError(HttpRequest request, ClientHttpResponse response) throws IOException {
        HttpStatusCode status = response.getStatusCode();
        String errorMessage = response.getStatusText();

        try {
            JsonNode errorNode = objectMapper.readTree(response.getBody());
            if (errorNode.has("exceptionMessage")
                    && !errorNode.get("exceptionMessage").isNull()) {
                errorMessage = errorNode.get("exceptionMessage").asText();
            } else if (errorNode.has("description")
                    && !errorNode.get("description").isNull()) {
                errorMessage = errorNode.get("description").asText();
            }
        } catch (Exception e) {
            log.debug("Не удалось разобрать JSON ответа об ошибке, используем стандартное сообщение", e);
        }

        if (status.isSameCodeAs(HttpStatus.NOT_FOUND)) {
            throw new ResourceNotFoundException("Ресурс не найден: " + errorMessage);
        } else if (status.isSameCodeAs(HttpStatus.CONFLICT)) {
            throw new ResourceAlreadyExistsException("Ресурс уже существует: " + errorMessage);
        }
        throw new ScrapperException("Ошибка HTTP: " + status + ". " + errorMessage);
    }
}
