package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.bot.dto.*;
import backend.academy.linktracker.bot.exception.*;
import backend.academy.linktracker.grpc.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Primary
@Component
@RequiredArgsConstructor
public class GrpcScrapperClient implements ScrapperClient {

    private final ScrapperServiceGrpc.ScrapperServiceBlockingStub stub;

    @Value("${app.grpc.scrapper-deadline:5s}")
    private Duration deadline;

    @Override
    public void registerChat(Long chatId) {
        try {
            stub.withDeadlineAfter(deadline.toMillis(), TimeUnit.MILLISECONDS)
                .registerChat(ChatRequest.newBuilder().setId(chatId).build());
        } catch (StatusRuntimeException e) {
            handleGrpcError(e);
        }
    }

    @Override
    public void deleteChat(Long chatId) {
        try {
            stub.withDeadlineAfter(deadline.toMillis(), TimeUnit.MILLISECONDS)
                .deleteChat(ChatRequest.newBuilder().setId(chatId).build());
        } catch (StatusRuntimeException e) {
            handleGrpcError(e);
        }
    }

    @Override
    public ListLinksResponse getLinks(Long chatId) {
        try {
            ListLinksResponseMsg res = stub.withDeadlineAfter(deadline.toMillis(), TimeUnit.MILLISECONDS)
                .getLinks(ChatRequest.newBuilder().setId(chatId).build());

            List<LinkResponse> links = res.getLinksList().stream()
                .map(l -> new LinkResponse(l.getId(), URI.create(l.getUrl()), l.getTagsList(), l.getFiltersList()))
                .toList();
            return new ListLinksResponse(links, res.getSize());
        } catch (StatusRuntimeException e) {
            handleGrpcError(e);
            return null;
        }
    }

    @Override
    public LinkResponse addLink(Long chatId, URI link, List<String> tags, List<String> filters) {
        try {
            LinkResponseMsg res = stub.withDeadlineAfter(deadline.toMillis(), TimeUnit.MILLISECONDS)
                .addLink(AddLinkRequestMsg.newBuilder()
                    .setChatId(chatId)
                    .setLink(link.toString())
                    .addAllTags(tags)
                    .addAllFilters(filters)
                    .build());
            return new LinkResponse(res.getId(), URI.create(res.getUrl()), res.getTagsList(), res.getFiltersList());
        } catch (StatusRuntimeException e) {
            handleGrpcError(e);
            return null;
        }
    }

    @Override
    public LinkResponse removeLink(Long chatId, URI link) {
        try {
            LinkResponseMsg res = stub.withDeadlineAfter(deadline.toMillis(), TimeUnit.MILLISECONDS)
                .removeLink(RemoveLinkRequestMsg.newBuilder()
                    .setChatId(chatId)
                    .setLink(link.toString())
                    .build());
            return new LinkResponse(res.getId(), URI.create(res.getUrl()), res.getTagsList(), res.getFiltersList());
        } catch (StatusRuntimeException e) {
            handleGrpcError(e);
            return null;
        }
    }

    private void handleGrpcError(StatusRuntimeException e) {
        Status.Code code = e.getStatus().getCode();
        if (code == Status.Code.NOT_FOUND) {
            throw new ResourceNotFoundException("Ресурс не найден: " + e.getMessage());
        } else if (code == Status.Code.ALREADY_EXISTS) {
            throw new ResourceAlreadyExistsException("Ресурс уже существует: " + e.getMessage());
        }
        throw new ScrapperException("Ошибка gRPC: " + code + ". " + e.getMessage());
    }
}
