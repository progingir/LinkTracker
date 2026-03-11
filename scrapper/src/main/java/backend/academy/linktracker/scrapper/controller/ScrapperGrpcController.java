package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.grpc.*;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.service.LinkService;
import backend.academy.linktracker.scrapper.service.TgChatService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapperGrpcController extends ScrapperServiceGrpc.ScrapperServiceImplBase {

    private final LinkService linkService;
    private final TgChatService tgChatService;

    @Override
    public void registerChat(ChatRequest request, StreamObserver<Empty> responseObserver) {
        if (request.getId() <= 0) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription("ID чата должен быть положительным").asRuntimeException());
            return;
        }
        tgChatService.registerChat(request.getId());
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteChat(ChatRequest request, StreamObserver<Empty> responseObserver) {
        if (request.getId() <= 0) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("ID чата невалиден").asRuntimeException());
            return;
        }
        tgChatService.deleteChat(request.getId());
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void getLinks(ChatRequest request, StreamObserver<ListLinksResponseMsg> responseObserver) {
        if (request.getId() <= 0) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("ID чата невалиден").asRuntimeException());
            return;
        }
        List<Link> links = linkService.getLinks(request.getId());
        List<LinkResponseMsg> messages = links.stream().map(this::mapToMsg).toList();
        responseObserver.onNext(ListLinksResponseMsg.newBuilder()
            .addAllLinks(messages).setSize(messages.size()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void addLink(AddLinkRequestMsg request, StreamObserver<LinkResponseMsg> responseObserver) {
        try {
            validateLinkRequest(request.getChatId(), request.getLink());
            Link link = linkService.addLink(request.getChatId(), URI.create(request.getLink()),
                request.getTagsList(), request.getFiltersList());
            responseObserver.onNext(mapToMsg(link));
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void removeLink(RemoveLinkRequestMsg request, StreamObserver<LinkResponseMsg> responseObserver) {
        try {
            validateLinkRequest(request.getChatId(), request.getLink());
            Link link = linkService.removeLink(request.getChatId(), URI.create(request.getLink()));
            responseObserver.onNext(mapToMsg(link));
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    private void validateLinkRequest(long chatId, String url) {
        if (chatId <= 0) throw new IllegalArgumentException("ID чата должен быть положительным");
        if (url == null || url.isBlank()) throw new IllegalArgumentException("URL не может быть пустым");
        try {
            URI.create(url);
        } catch (Exception e) {
            throw new IllegalArgumentException("Некорректный формат URL");
        }
    }

    private LinkResponseMsg mapToMsg(Link link) {
        return LinkResponseMsg.newBuilder()
            .setId(link.id()).setUrl(link.url().toString())
            .addAllTags(link.tags()).addAllFilters(link.filters()).build();
    }
}
