package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.bot.dto.*;
import backend.academy.linktracker.bot.exception.*;
import backend.academy.linktracker.bot.properties.GrpcScrapperProperties;
import backend.academy.linktracker.grpc.*;
import io.grpc.ClientInterceptor;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "scrapper-client-type", havingValue = "grpc", matchIfMissing = true)
public class GrpcScrapperClient implements ScrapperClient {

    private final ScrapperServiceGrpc.ScrapperServiceBlockingStub stub;
    private final GrpcScrapperProperties grpcProperties;

    private static final Metadata.Key<String> TG_CHAT_ID_KEY =
        Metadata.Key.of("tg-chat-id", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public void registerChat(Long chatId) {
        log.atInfo().addKeyValue("chat_id", chatId).log("grpc: отправка запроса на регистрацию чата");
        try {
            getStubWithHeaders(chatId).registerChat(ChatRequest.newBuilder().build());
        } catch (StatusRuntimeException e) {
            throw getGrpcError(e);
        }
        log.atInfo().addKeyValue("chat_id", chatId).log("Чат успешно зарегистрирован");
    }

    @Override
    public void deleteChat(Long chatId) {
        log.atInfo().addKeyValue("chat_id", chatId).log("grpc: отправка запроса на удаление чата");
        try {
            getStubWithHeaders(chatId).deleteChat(ChatRequest.newBuilder().build());
        } catch (StatusRuntimeException e) {
            throw getGrpcError(e);
        }
        log.atInfo().addKeyValue("chat_id", chatId).log("Чат успешно удален");
    }

    @Override
    public ListLinksResponse getLinks(Long chatId) {
        log.atInfo().addKeyValue("chat_id", chatId).log("grpc: отправка запроса на получение ссылок");
        ListLinksResponse response;
        try {
            ListLinksResponseMsg res =
                getStubWithHeaders(chatId).getLinks(ChatRequest.newBuilder().build());

            List<LinkResponse> links = res.getLinksList().stream()
                .map(l -> new LinkResponse(l.getId(), URI.create(l.getUrl()), l.getTagsList()))
                .toList();
            response = new ListLinksResponse(links, res.getSize());
        } catch (StatusRuntimeException e) {
            throw getGrpcError(e);
        }
        log.atInfo().addKeyValue("chat_id", chatId).log("Список ссылок успешно получен");
        return response;
    }

    @Override
    public LinkResponse addLink(Long chatId, URI link, List<String> tags) {
        log.atInfo()
            .addKeyValue("chat_id", chatId)
            .addKeyValue("url", link)
            .log("grpc: отправка запроса на добавление ссылки");
        LinkResponse response;
        try {
            LinkResponseMsg res = getStubWithHeaders(chatId)
                .addLink(AddLinkRequestMsg.newBuilder()
                    .setLink(link.toString())
                    .addAllTags(tags)
                    .build());

            response = new LinkResponse(res.getId(), URI.create(res.getUrl()), res.getTagsList());
        } catch (StatusRuntimeException e) {
            throw getGrpcError(e);
        }
        log.atInfo().addKeyValue("chat_id", chatId).addKeyValue("url", link).log("Ссылка успешно добавлена");
        return response;
    }

    @Override
    public LinkResponse removeLink(Long chatId, URI link) {
        log.atInfo()
            .addKeyValue("chat_id", chatId)
            .addKeyValue("url", link)
            .log("grpc: отправка запроса на удаление ссылки");
        LinkResponse response;
        try {
            LinkResponseMsg res = getStubWithHeaders(chatId)
                .removeLink(RemoveLinkRequestMsg.newBuilder()
                    .setLink(link.toString())
                    .build());

            response = new LinkResponse(res.getId(), URI.create(res.getUrl()), res.getTagsList());
        } catch (StatusRuntimeException e) {
            throw getGrpcError(e);
        }
        log.atInfo().addKeyValue("chat_id", chatId).addKeyValue("url", link).log("Ссылка успешно удалена");
        return response;
    }

    private ScrapperServiceGrpc.ScrapperServiceBlockingStub getStubWithHeaders(Long chatId) {
        Metadata metadata = new Metadata();
        metadata.put(TG_CHAT_ID_KEY, String.valueOf(chatId));

        ClientInterceptor interceptor = MetadataUtils.newAttachHeadersInterceptor(metadata);

        return stub.withInterceptors(interceptor)
            .withDeadlineAfter(grpcProperties.getScrapperDeadline().toMillis(), TimeUnit.MILLISECONDS);
    }

    private RuntimeException getGrpcError(StatusRuntimeException e) {
        Status.Code code = e.getStatus().getCode();
        if (code == Status.Code.NOT_FOUND) {
            return new ResourceNotFoundException("Ресурс не найден: " + e.getMessage());
        } else if (code == Status.Code.ALREADY_EXISTS) {
            return new ResourceAlreadyExistsException("Ресурс уже существует: " + e.getMessage());
        }
        return new ScrapperException("Ошибка gRPC: " + code + ". " + e.getMessage());
    }
}
