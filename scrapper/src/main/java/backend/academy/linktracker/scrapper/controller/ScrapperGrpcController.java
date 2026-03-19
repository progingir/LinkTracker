package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.grpc.*;
import backend.academy.linktracker.scrapper.dto.LinkResponse;
import backend.academy.linktracker.scrapper.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.interceptor.ChatIdInterceptor;
import backend.academy.linktracker.scrapper.mapper.ScrapperGrpcMapper;
import backend.academy.linktracker.scrapper.service.LinkService;
import backend.academy.linktracker.scrapper.service.TgChatService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class ScrapperGrpcController extends ScrapperServiceGrpc.ScrapperServiceImplBase {

    private final LinkService linkService;
    private final TgChatService tgChatService;
    private final ScrapperGrpcMapper mapper;

    private Long getChatId() {
        return ChatIdInterceptor.CHAT_ID_CTX.get();
    }

    @Override
    public void registerChat(ChatRequest request, StreamObserver<Empty> responseObserver) {
        tgChatService.registerChat(getChatId());
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteChat(ChatRequest request, StreamObserver<Empty> responseObserver) {
        tgChatService.deleteChat(getChatId());
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void getLinks(ChatRequest request, StreamObserver<ListLinksResponseMsg> responseObserver) {
        ListLinksResponse response = linkService.getLinksResponse(getChatId());

        responseObserver.onNext(ListLinksResponseMsg.newBuilder()
                .addAllLinks(mapper.toListMsg(response.links()))
                .setSize(response.size())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void addLink(AddLinkRequestMsg request, StreamObserver<LinkResponseMsg> responseObserver) {
        LinkResponse resp = linkService.addLinkFromExternal(
                getChatId(), request.getLink(), request.getTagsList(), request.getFiltersList());

        responseObserver.onNext(mapper.toMsg(resp));
        responseObserver.onCompleted();
    }

    @Override
    public void removeLink(RemoveLinkRequestMsg request, StreamObserver<LinkResponseMsg> responseObserver) {
        LinkResponse resp = linkService.removeLinkFromExternal(getChatId(), request.getLink());

        responseObserver.onNext(mapper.toMsg(resp));
        responseObserver.onCompleted();
    }
}
