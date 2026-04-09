package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.grpc.*;
import backend.academy.linktracker.scrapper.dto.LinkResponse;
import backend.academy.linktracker.scrapper.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.interceptor.ChatIdInterceptor;
import backend.academy.linktracker.scrapper.mapper.ScrapperGrpcMapper;
import backend.academy.linktracker.scrapper.service.LinkService;
import backend.academy.linktracker.scrapper.service.TgChatService;
import io.grpc.stub.StreamObserver;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class ScrapperGrpcController extends ScrapperServiceGrpc.ScrapperServiceImplBase {

    private static final int DEFAULT_LIMIT = 100;

    private final LinkService linkService;
    private final TgChatService tgChatService;
    private final ScrapperGrpcMapper mapper;

    private Long getChatId() {
        return ChatIdInterceptor.CHAT_ID_CTX.get();
    }

    @Override
    public void registerChat(ChatRequest request, StreamObserver<Empty> responseObserver) {
        Long chatId = getChatId();

        log.atInfo().addKeyValue("chat_id", chatId).log("Получен gRPC запрос на регистрацию чата");

        tgChatService.registerChat(chatId);

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteChat(ChatRequest request, StreamObserver<Empty> responseObserver) {
        Long chatId = getChatId();

        log.atInfo().addKeyValue("chat_id", chatId).log("Получен gRPC запрос на удаление чата");

        tgChatService.deleteChat(chatId);

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void getLinks(ChatRequest request, StreamObserver<ListLinksResponseMsg> responseObserver) {
        Long chatId = getChatId();

        log.atInfo()
                .addKeyValue("chat_id", chatId)
                .addKeyValue("limit", DEFAULT_LIMIT)
                .addKeyValue("last_link_id", null)
                .log("Получен gRPC запрос на получение ссылок");

        ListLinksResponse response = linkService.getLinksResponse(chatId, DEFAULT_LIMIT, null);

        responseObserver.onNext(ListLinksResponseMsg.newBuilder()
                .addAllLinks(mapper.toListMsg(response.links()))
                .setSize(response.size())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void addLink(AddLinkRequestMsg request, StreamObserver<LinkResponseMsg> responseObserver) {
        Long chatId = getChatId();
        String link = request.getLink();

        log.atInfo()
                .addKeyValue("chat_id", chatId)
                .addKeyValue("link", link)
                .log("Получен gRPC запрос на добавление ссылки");

        LinkResponse resp = linkService.addLink(chatId, URI.create(link), request.getTagsList());

        responseObserver.onNext(mapper.toMsg(resp));
        responseObserver.onCompleted();
    }

    @Override
    public void removeLink(RemoveLinkRequestMsg request, StreamObserver<LinkResponseMsg> responseObserver) {
        Long chatId = getChatId();
        String link = request.getLink();

        log.atInfo()
                .addKeyValue("chat_id", chatId)
                .addKeyValue("link", link)
                .log("Получен gRPC запрос на удаление ссылки");

        LinkResponse resp = linkService.removeLink(chatId, URI.create(link));

        responseObserver.onNext(mapper.toMsg(resp));
        responseObserver.onCompleted();
    }
}
